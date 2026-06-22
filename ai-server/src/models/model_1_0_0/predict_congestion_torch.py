"""
혼잡도(여유/보통/혼잡) 실시간 추론 스크립트 - PyTorch 버전
==========================================================

predict_congestion.py(LightGBM)와 완전히 동일한 구조다. 날짜/공휴일/위치 피처는
API 없이 바로 계산되고, 날씨·축제·최근방문이력 세 곳만 스텁(임시값)으로 남겨뒀다.
apidog의 실제 엔드포인트를 알게 되면 그 세 함수만 교체하면 된다.

사용법:
    python predict_congestion_torch.py --dong 경포동 --date 2026-06-20

GPU: 이 머신에 CUDA GPU가 있으면 자동으로 사용한다.
"""

import argparse
import datetime
import json
import math
import os

import holidays
import joblib
import numpy as np
import pandas as pd
import torch
import torch.nn as nn

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
gpu_name = torch.cuda.get_device_name(0) if torch.cuda.is_available() else "없음 (CPU로 진행)"
print(f"📡 연산 장치 스탠바이: {device} / GPU: {gpu_name}")

# 파일을 루트와 '데이터/master table 사용 csv/' 양쪽에서 찾는다
## 상대경로로 변경
SEARCH_DIRS = "../datas"


def find_path(name: str) -> str:
    """주어진 파일명을 SEARCH_DIRS에서 찾아 실제 경로를 반환. 못 찾으면 원래 이름 그대로."""
    for d in SEARCH_DIRS:
        p = os.path.join(d, name)
        if os.path.exists(p):
            return p
    return name


MODEL_PATH = "congestion_model_torch.pt"
CAT_ENCODERS_PATH = "torch_cat_encoders.pkl"
SCALER_PATH = "torch_num_scaler.pkl"
MEDIANS_PATH = "torch_num_medians.pkl"
SIGUNGU_MAP_PATH = "dong_to_sigungu_map.pkl"
STATION_MAP_PATH = "dong_to_station_map.pkl"
VISITOR_HISTORY_PATH = "visitor_history.csv"
FESTIVAL_LOOKUP_PATH = "festival_lookup.csv"  # build_festival_lookup.py로 생성
LEVEL_AVG_PATH = "dong_level_avg_visitors.csv"  # 동·등급별 과거 평균 방문자수
COORDS_PATH = "festival_with_coords.csv"  # 좌표 보강 (geocode_kakao.py로 생성)
WEATHER_DAILY_PATH = "chungbuk_weather_daily.csv"  # 충북 일별 날씨 (build_chungbuk_weather.py로 생성)


# 학습 스크립트(train_congestion_model_torch.py)의 CongestionNet과 완전히 동일한 구조여야
# 저장된 가중치를 제대로 불러올 수 있다.
class CongestionNet(nn.Module):
    def __init__(self, cat_cardinalities, num_numerical, hidden=(128, 64), n_classes=3, dropout=0.3):
        super().__init__()
        self.embeddings = nn.ModuleList([
            nn.Embedding(card + 1, min(32, (card + 2) // 2)) for card in cat_cardinalities
        ])
        embed_total = sum(e.embedding_dim for e in self.embeddings)
        prev = embed_total + num_numerical
        layers = []
        for h in hidden:
            layers += [nn.Linear(prev, h), nn.BatchNorm1d(h), nn.ReLU(), nn.Dropout(dropout)]
            prev = h
        layers.append(nn.Linear(prev, n_classes))
        self.mlp = nn.Sequential(*layers)

    def forward(self, cat_x, num_x):
        embeds = [emb(cat_x[:, i]) for i, emb in enumerate(self.embeddings)]
        x = torch.cat(embeds + [num_x], dim=1)
        return self.mlp(x)


# ----------------------------------------------------------------------
# 완성된 부분: API 없이 바로 계산 가능 (predict_congestion.py와 동일 로직)
# ----------------------------------------------------------------------

def get_date_features(target_date: datetime.date) -> dict:
    return {
        "year": target_date.year,
        "month": target_date.month,
        "day": target_date.day,
        "day_of_week": target_date.weekday(),
        "is_weekend": int(target_date.weekday() >= 5),
    }


def get_holiday_features(target_date: datetime.date) -> dict:
    kr_holidays = holidays.KR(years=target_date.year)
    is_holiday = target_date in kr_holidays
    return {
        "is_public_holiday": int(is_holiday),
        "is_final_holiday": int(is_holiday),  # TODO: 원본 정의 확인 필요 (predict_congestion.py와 동일 가정)
    }


def get_location_features(dong_name: str) -> dict:
    sigungu_map = joblib.load(find_path(SIGUNGU_MAP_PATH))
    station_map = joblib.load(find_path(STATION_MAP_PATH))
    sigungu_name = sigungu_map.get(dong_name, "데이터없음")
    if sigungu_name == "데이터없음":
        print(f"[경고] '{dong_name}'의 시군구를 매핑표에서 찾지 못했습니다. "
              f"동명이 여러 도시에 겹치는 동일 수 있습니다 (예: 강남동/교동/중앙동).")
    return {
        "dong_name": dong_name,
        "sigungu_name": sigungu_name,
        "weather_station_id": station_map.get(dong_name),
    }


# ----------------------------------------------------------------------
# TODO: 실제 API로 교체해야 하는 부분 (지금은 스텁) - predict_congestion.py와 동일
# ----------------------------------------------------------------------

_weather_cache = {"daily": None, "station_map": None, "loaded": False}


def _load_weather():
    if _weather_cache["loaded"]:
        return
    _weather_cache["loaded"] = True
    try:
        w = pd.read_csv(find_path(WEATHER_DAILY_PATH), encoding="utf-8-sig")
        w["datetime"] = pd.to_datetime(w["datetime"])
        _weather_cache["daily"] = w
    except FileNotFoundError:
        _weather_cache["daily"] = None
    try:
        _weather_cache["station_map"] = joblib.load(find_path("chungbuk_dong_station_map.pkl"))
    except FileNotFoundError:
        _weather_cache["station_map"] = None


def fetch_weather(target_date: datetime.date, weather_station_id, dong_name: str = None) -> dict:
    """충북 일별 날씨(chungbuk_weather_daily.csv)에서 실제 날씨를 가져온다.
    - 해당 날짜 데이터가 있으면 그대로 사용
    - 미래 날짜 등 데이터가 없으면 같은 동·관측소의 과거 '같은 월·일' 평균으로 대체
    - 그래도 없으면 임시값(15/25도) 반환
    반환 키: min_temperature, max_temperature, rain_duration_active_hours, total_rainfall_active_hours"""
    _load_weather()
    w = _weather_cache["daily"]
    smap = _weather_cache["station_map"]
    cols = ["min_temperature", "max_temperature", "rain_duration_active_hours", "total_rainfall_active_hours"]

    if w is not None:
        stn = weather_station_id
        if (stn is None or pd.isna(stn)) and smap and dong_name in smap:
            stn = smap[dong_name]
        if stn is not None and not pd.isna(stn):
            sub = w[w["weather_station_id"] == stn]
            exact = sub[sub["datetime"] == pd.Timestamp(target_date)]
            if not exact.empty:
                return {c: float(exact.iloc[0][c]) for c in cols}
            # 같은 월·일의 과거 평균 (미래 날짜 대체)
            same = sub[(sub["datetime"].dt.month == target_date.month) &
                       (sub["datetime"].dt.day == target_date.day)]
            if not same.empty:
                return {c: float(same[c].mean()) for c in cols}

    return {"min_temperature": 15.0, "max_temperature": 25.0,
            "rain_duration_active_hours": 0.0, "total_rainfall_active_hours": 0.0}


def fetch_festival_info(target_date: datetime.date, dong_name: str) -> dict:
    """festival_lookup.csv(festival.csv+cb_festival.xlsx 통합) 조회.
    새 축제 일정이 생기면 build_festival_lookup.py를 다시 돌려 festival_lookup.csv를 갱신해야 한다.
    반환 키: is_festival_active, festival_type, has_celebrity_performance, festival_duration"""
    try:
        lookup = pd.read_csv(find_path(FESTIVAL_LOOKUP_PATH), parse_dates=["festival_date"], encoding="utf-8-sig")
        match = lookup[(lookup["festival_date"] == pd.Timestamp(target_date)) &
                        (lookup["dong_name"] == dong_name)]
        if not match.empty:
            row = match.iloc[0]
            return {
                "is_festival_active": 1,
                "festival_type": row["festival_type"],
                "has_celebrity_performance": 0,  # 원본 데이터에 이 정보가 없어 항상 0 (가정)
                "festival_duration": int(row["festival_duration"]),
            }
    except FileNotFoundError:
        print(f"[경고] '{find_path(FESTIVAL_LOOKUP_PATH)}'를 찾지 못했습니다. build_festival_lookup.py를 먼저 실행하세요.")
    return {
        "is_festival_active": 0,
        "festival_type": "없음",
        "has_celebrity_performance": 0,
        "festival_duration": 0,
    }


def fetch_recent_visitor_history(dong_name: str, target_date: datetime.date, days: int = 7) -> dict:
    """최근 방문자수 이력 조회 (TODO/로컬 이력 파일 권장). 반환 키:
    lag1_visitors, roll7_visitors, lag1_level"""
    try:
        hist = pd.read_csv(find_path(VISITOR_HISTORY_PATH), parse_dates=["date"])
        hist = hist[hist["dong_name"] == dong_name].sort_values("date")
        hist = hist[hist["date"] < pd.Timestamp(target_date)]
        if hist.empty:
            raise FileNotFoundError
        lag1 = hist.iloc[-1]
        roll7 = hist["visitors"].tail(days).mean()
        return {"lag1_visitors": lag1["visitors"], "roll7_visitors": roll7, "lag1_level": lag1["level"]}
    except (FileNotFoundError, KeyError):
        print(f"[경고] '{find_path(VISITOR_HISTORY_PATH)}'에서 '{dong_name}' 이력을 찾지 못했습니다. "
              f"lag 피처를 결측으로 둡니다.")
        return {"lag1_visitors": float("nan"), "roll7_visitors": float("nan"), "lag1_level": None}


def search_festival_by_name(query: str) -> pd.DataFrame:
    """festival_lookup.csv에서 festival_name에 query가 포함되는 행을 찾는다."""
    lookup = pd.read_csv(find_path(FESTIVAL_LOOKUP_PATH), parse_dates=["festival_date"], encoding="utf-8-sig")
    return lookup[lookup["festival_name"].str.contains(query, case=False, na=False)]


def project_to_upcoming(start: pd.Timestamp, end: pd.Timestamp, today: datetime.date) -> tuple:
    """가장 최근 축제 날짜를 기준으로, 오늘 이후가 되도록 연도를 밀어 추정 날짜를 만든다."""
    def shift(d: pd.Timestamp, years: int) -> pd.Timestamp:
        try:
            return d.replace(year=d.year + years)
        except ValueError:  # 2/29 같은 경우 대비
            return d.replace(year=d.year + years, day=28)

    years_to_add = today.year - start.year
    if shift(start, years_to_add).date() < today:
        years_to_add += 1
    return shift(start, years_to_add), shift(end, years_to_add)


def predict_for_festival(query: str):
    """축제 이름으로 검색해서, 해당 축제가 열리는 동·기간을 찾아 그 기간 전체를 예측한다.
    같은 축제가 매년 반복되면 연도별로 별도 구간으로 나눠서 보여주고, 가장 최근 기록을
    올해(또는 다음 발생 시점) 이후로 투영한 추정 예측도 추가로 보여준다."""
    matches = search_festival_by_name(query)
    if matches.empty:
        print(f"'{query}'가 포함된 축제를 찾지 못했습니다 (festival_lookup.csv 기준, 2025년까지만 있음).")
        return

    today = datetime.date.today()

    # 좌표 보강 파일이 있으면 로드 (없어도 동작)
    try:
        coords_df = pd.read_csv(find_path(COORDS_PATH), encoding="utf-8-sig")
    except FileNotFoundError:
        coords_df = None

    def get_coords(fname, dong):
        if coords_df is None:
            return None
        m = coords_df[(coords_df["festival_name"] == fname) & (coords_df["dong_name"] == dong)]
        if m.empty or pd.isna(m.iloc[0]["lat"]):
            return None
        r = m.iloc[0]
        return f"좌표 ({r['lat']}, {r['lng']}) / {r['address']}"

    for (fname, dong), group in matches.groupby(["festival_name", "dong_name"]):
        dates = sorted(pd.Timestamp(d) for d in group["festival_date"].unique())
        segments, seg_start, prev = [], dates[0], dates[0]
        for d in dates[1:]:
            if (d - prev).days > 1:
                segments.append((seg_start, prev))
                seg_start = d
            prev = d
        segments.append((seg_start, prev))

        # 가장 최근 기록을 올해 이후로 투영해서 추정 예측만 보여준다 (과거 기록은 출력 안 함)
        latest_start, latest_end = max(segments, key=lambda s: s[0])
        proj_start, proj_end = project_to_upcoming(latest_start, latest_end, today)
        coord_str = get_coords(fname, dong)
        print(f"\n=== {fname} ({dong}, {proj_start.date()} ~ {proj_end.date()}) "
              f"[{latest_start.year}년 기록 기준 추정 - 실제 {proj_start.year}년 일정 확정 전임] ===")
        if coord_str:
            print(coord_str)
        for d in pd.date_range(proj_start, proj_end):
            predict_congestion(dong, d.date())


# ----------------------------------------------------------------------
# 피처 조립 + 인코딩 + 추론
# ----------------------------------------------------------------------

def assemble_row(dong_name: str, target_date: datetime.date) -> dict:
    row = {}
    row.update(get_date_features(target_date))
    row.update(get_holiday_features(target_date))
    row.update(get_location_features(dong_name))
    row.update(fetch_weather(target_date, row["weather_station_id"], dong_name))
    row.update(fetch_festival_info(target_date, dong_name))
    row.update(fetch_recent_visitor_history(dong_name, target_date))
    return row


def encode_for_model(row: dict, checkpoint: dict, cat_encoders: dict,
                      scaler, medians: dict):
    """row(dict)를 학습 시 사용한 것과 동일한 (범주형 인덱스, 표준화된 수치형) 텐서로 변환.
    학습 스크립트의 fit_categorical_encoders / build_numerical_matrix와 동일한 규칙을 따른다:
    범주형은 0=결측/미확인, 수치형은 결측 시 학습 데이터 median으로 채우고 결측 표시 피처를 둔다."""
    cat_cols = checkpoint["categorical_cols"]
    cat_idx = []
    for c in cat_cols:
        mapping = cat_encoders[c]
        val = row.get(c)
        cat_idx.append(mapping.get(str(val), 0) if val is not None else 0)
    cat_arr = np.array([cat_idx], dtype=np.int64)

    num_vals = []
    for c in checkpoint["numerical_cols"]:
        if c.endswith("_missing"):
            base_c = c.replace("_missing", "")
            v = row.get(base_c)
            is_missing = v is None or (isinstance(v, float) and np.isnan(v))
            num_vals.append(1.0 if is_missing else 0.0)
        else:
            v = row.get(c)
            if v is None or (isinstance(v, float) and np.isnan(v)):
                v = medians.get(c, 0.0)
            num_vals.append(float(v))
    num_arr = scaler.transform(pd.DataFrame([num_vals], columns=checkpoint["numerical_cols"])).astype(np.float32)

    return (torch.tensor(cat_arr, dtype=torch.long),
            torch.tensor(num_arr, dtype=torch.float32))


def estimate_visitors(dong_name: str, level: str):
    """dong_level_avg_visitors.csv에서 해당 동·등급의 과거 평균 방문자수를 찾아
    100명 단위로 올림해서 반환한다. 데이터가 없으면 None."""
    try:
        avg = pd.read_csv(find_path(LEVEL_AVG_PATH))
        match = avg[(avg["dong_name"] == dong_name) & (avg["level"] == level)]
        if match.empty:
            return None
        value = float(match.iloc[0]["visitors"])
        return int(math.ceil(value / 100.0) * 100)
    except FileNotFoundError:
        return None


def _load_model():
    """모델·인코더를 한 번만 로드해서 재사용 (반복 호출 시 속도 개선)."""
    checkpoint = torch.load(find_path(MODEL_PATH), map_location=device, weights_only=False)
    cat_encoders = joblib.load(find_path(CAT_ENCODERS_PATH))
    scaler = joblib.load(find_path(SCALER_PATH))
    medians = joblib.load(find_path(MEDIANS_PATH))
    model = CongestionNet(checkpoint["cat_cardinalities"], checkpoint["num_numerical"]).to(device)
    model.load_state_dict(checkpoint["model_state"])
    model.eval()
    return model, checkpoint, cat_encoders, scaler, medians


def predict_one(dong_name, target_date, bundle):
    """등급·확률·혼잡weight(혼잡 클래스 확률)를 dict로 반환. 출력은 안 함."""
    model, checkpoint, cat_encoders, scaler, medians = bundle
    row = assemble_row(dong_name, target_date)
    cat_x, num_x = encode_for_model(row, checkpoint, cat_encoders, scaler, medians)
    cat_x, num_x = cat_x.to(device), num_x.to(device)
    with torch.no_grad():
        logits = model(cat_x, num_x)
        probs = torch.softmax(logits, dim=1).cpu().numpy()[0]
    idx_to_label = {v: k for k, v in checkpoint["label_to_idx"].items()}
    proba = {idx_to_label[i]: round(float(p), 3) for i, p in enumerate(probs)}
    pred_label = idx_to_label[int(probs.argmax())]
    return {"level": pred_label, "proba": proba,
            "weight": round(float(proba.get("혼잡", 0.0)), 3),  # 혼잡 확률을 혼잡도 weight로
            "est_visitors": estimate_visitors(dong_name, pred_label)}


def export_festival_json(query: str, out_path: str = "festival_predictions.json"):
    """축제명으로 검색해 처음 받은 JSON 형식(id, name, lat, lng, address, stDate, enDate,
    weights[날짜별 혼잡도])으로 파일을 만든다.
    주의: 시간대별(traffics)은 이 모델이 예측하지 않으므로 포함하지 않는다(하루 단위 예측)."""
    matches = search_festival_by_name(query)
    if matches.empty:
        print(f"'{query}' 검색 결과 없음")
        return

    try:
        coords_df = pd.read_csv(find_path(COORDS_PATH), encoding="utf-8-sig")
    except FileNotFoundError:
        coords_df = None

    def coords_of(fname, dong):
        if coords_df is None:
            return None, None, None
        m = coords_df[(coords_df["festival_name"] == fname) & (coords_df["dong_name"] == dong)]
        if m.empty or pd.isna(m.iloc[0]["lat"]):
            return None, None, None
        r = m.iloc[0]
        return float(r["lat"]), float(r["lng"]), r.get("address")

    bundle = _load_model()
    today = datetime.date.today()
    result = []
    fid = 1

    for (fname, dong), group in matches.groupby(["festival_name", "dong_name"]):
        dates = sorted(pd.Timestamp(d) for d in group["festival_date"].unique())
        segments, seg_start, prev = [], dates[0], dates[0]
        for d in dates[1:]:
            if (d - prev).days > 1:
                segments.append((seg_start, prev))
                seg_start = d
            prev = d
        segments.append((seg_start, prev))

        latest_start, latest_end = max(segments, key=lambda s: s[0])
        proj_start, proj_end = project_to_upcoming(latest_start, latest_end, today)
        lat, lng, addr = coords_of(fname, dong)

        weights = []
        for d in pd.date_range(proj_start, proj_end):
            p = predict_one(dong, d.date(), bundle)
            weights.append({"date": d.strftime("%Y-%m-%d"), "weight": p["weight"],
                            "level": p["level"], "est_visitors": p["est_visitors"]})

        result.append({
            "id": fid, "name": fname, "dong": dong,
            "lat": lat, "lng": lng, "address": addr,
            "stDate": proj_start.strftime("%Y-%m-%dT00:00:00"),
            "enDate": proj_end.strftime("%Y-%m-%dT23:59:59"),
            "weights": weights,
        })
        fid += 1

    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
    print(f"JSON 저장: {out_path} ({len(result)}건)")


def predict_congestion(dong_name: str, target_date: datetime.date) -> str:
    checkpoint = torch.load(find_path(MODEL_PATH), map_location=device, weights_only=False)
    cat_encoders = joblib.load(find_path(CAT_ENCODERS_PATH))
    scaler = joblib.load(find_path(SCALER_PATH))
    medians = joblib.load(find_path(MEDIANS_PATH))

    model = CongestionNet(checkpoint["cat_cardinalities"], checkpoint["num_numerical"]).to(device)
    model.load_state_dict(checkpoint["model_state"])
    model.eval()

    row = assemble_row(dong_name, target_date)
    cat_x, num_x = encode_for_model(row, checkpoint, cat_encoders, scaler, medians)
    cat_x, num_x = cat_x.to(device), num_x.to(device)

    with torch.no_grad():
        logits = model(cat_x, num_x)
        probs = torch.softmax(logits, dim=1).cpu().numpy()[0]

    idx_to_label = {v: k for k, v in checkpoint["label_to_idx"].items()}
    pred_idx = int(probs.argmax())
    pred_label = idx_to_label[pred_idx]
    proba_dict = {idx_to_label[i]: round(float(p), 3) for i, p in enumerate(probs)}

    est_visitors = estimate_visitors(dong_name, pred_label)
    print(f"\n예측: {dong_name} / {target_date} -> {pred_label}", end="")
    if est_visitors is not None:
        print(f" (예상 방문자 약 {est_visitors:,}명)")
    else:
        print(" (방문자 평균 데이터 없음)")
    print(f"클래스별 확률: {proba_dict}")
    return pred_label


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--dong", required=False, help="행정동명 (예: 경포동)")
    parser.add_argument("--date", required=False, help="예측할 날짜 YYYY-MM-DD")
    parser.add_argument("--festival", required=False, help="축제 이름으로 검색 (예: 막국수)")
    parser.add_argument("--json", action="store_true", help="결과를 JSON 파일로 저장 (--festival과 함께)")
    args = parser.parse_args()

    if args.festival:
        if args.json:
            export_festival_json(args.festival)
        else:
            predict_for_festival(args.festival)
    elif args.dong and args.date:
        predict_congestion(args.dong, datetime.date.fromisoformat(args.date))
    else:
        mode = input("1) 동+날짜로 조회  2) 축제 이름으로 조회  선택: ").strip()
        if mode == "2":
            query = input("축제 이름(일부만 입력해도 됨): ").strip()
            predict_for_festival(query)
        else:
            dong_name = input("동 이름: ").strip()
            date_str = input("날짜 (YYYY-MM-DD): ").strip()
            predict_congestion(dong_name, datetime.date.fromisoformat(date_str))
