"""
혼잡도(여유/보통/혼잡) 예측 모델 - PyTorch 신경망 버전
=====================================================

"""

import joblib
import numpy as np
import pandas as pd
import torch
import torch.nn as nn
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
    f1_score,
)
from sklearn.preprocessing import StandardScaler
from torch.utils.data import DataLoader, Dataset

# ----------------------------------------------------------------------
# 연산 장치 설정
# ----------------------------------------------------------------------


device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
gpu_name = torch.cuda.get_device_name(0) if torch.cuda.is_available() else "없음 (CPU로 진행)"
print(f"📡 연산 장치 스탠바이: {device} / GPU: {gpu_name}")

DATA_PATH = "../datas/final_master_table_english.csv"
LEVEL_LABELS = ["여유", "보통", "혼잡"]

LEAK_COLS = [
    "total_visitors_daily", "local_visitors_daily", "foreign_visitors_daily",
    "daily_festival_attendance_y", "level",
]
DROP_COLS = ["festival_date", "festival_name"]
CATEGORICAL_COLS = ["dong_name", "sigungu_name", "festival_type", "weather_station_id", "lag1_level"]
NUMERICAL_COLS = [
    "is_festival_active", "has_celebrity_performance", "festival_duration",
    "min_temperature", "max_temperature", "rain_duration_active_hours",
    "total_rainfall_active_hours", "is_public_holiday", "year", "month", "day",
    "day_of_week", "is_weekend", "is_final_holiday", "lag1_visitors", "roll7_visitors",
]
# lag1_visitors/roll7_visitors는 동별 시계열 초반에 결측이 생김 -> 결측 표시 피처 추가
MISSING_FLAG_COLS = ["lag1_visitors", "roll7_visitors"]


# ----------------------------------------------------------------------
# LightGBM v3와 동일한 전처리 (재사용)
# ----------------------------------------------------------------------

def fix_sigungu_mapping(df: pd.DataFrame):
    real = df[df["sigungu_name"] != "데이터없음"]
    g = real.groupby("dong_name")["sigungu_name"].unique()
    safe_map = {d: v[0] for d, v in g.items() if len(v) == 1}
    df = df.copy()
    df["sigungu_name"] = df.apply(lambda r: safe_map.get(r["dong_name"], r["sigungu_name"]), axis=1)
    return df, safe_map


def build_labels(df, target_col="total_visitors_daily", group_col="dong_name"):
    def _qcut_safe(s):
        try:
            return pd.qcut(s, q=3, labels=LEVEL_LABELS, duplicates="drop")
        except ValueError:
            return pd.Series(["보통"] * len(s), index=s.index)
    return df.groupby(group_col, group_keys=False)[target_col].apply(_qcut_safe)


def add_lag_features(df):
    df = df.sort_values(["dong_name", "festival_date"]).reset_index(drop=True)
    g = df.groupby("dong_name")
    df["lag1_visitors"] = g["total_visitors_daily"].shift(1)
    df["roll7_visitors"] = g["total_visitors_daily"].transform(
        lambda s: s.shift(1).rolling(7, min_periods=3).mean()
    )
    df["lag1_level"] = g["level"].shift(1)
    return df


# ----------------------------------------------------------------------
# 신경망 전용 인코딩: 범주형은 정수 인덱스(0=결측/미확인), 수치형은 표준화
# ----------------------------------------------------------------------

def fit_categorical_encoders(df: pd.DataFrame, cols: list[str]) -> dict:
    """0은 결측/미확인 전용 인덱스로 예약하고, 실제 범주는 1부터 시작."""
    encoders = {}
    for c in cols:
        uniques = sorted(df[c].dropna().astype(str).unique().tolist())
        encoders[c] = {v: i + 1 for i, v in enumerate(uniques)}
    return encoders


def apply_categorical_encoders(df: pd.DataFrame, cols: list[str], encoders: dict) -> pd.DataFrame:
    out = pd.DataFrame(index=df.index)
    for c in cols:
        mapping = encoders[c]
        out[c] = df[c].astype(str).map(mapping).fillna(0).astype(int)
    return out


def build_numerical_matrix(df: pd.DataFrame, medians: dict, scaler: StandardScaler = None):
    num = df[NUMERICAL_COLS].copy()
    for c in MISSING_FLAG_COLS:
        num[f"{c}_missing"] = num[c].isna().astype(float)
        num[c] = num[c].fillna(medians[c])
    if scaler is None:
        scaler = StandardScaler()
        scaled = scaler.fit_transform(num)
    else:
        scaled = scaler.transform(num)
    return scaled.astype(np.float32), scaler, num.columns.tolist()


class TabularDataset(Dataset):
    def __init__(self, cat_arr, num_arr, labels):
        self.cat_arr = torch.tensor(cat_arr, dtype=torch.long)
        self.num_arr = torch.tensor(num_arr, dtype=torch.float32)
        self.labels = torch.tensor(labels, dtype=torch.long)

    def __len__(self):
        return len(self.labels)

    def __getitem__(self, idx):
        return self.cat_arr[idx], self.num_arr[idx], self.labels[idx]


class CongestionNet(nn.Module):
    def __init__(self, cat_cardinalities: list[int], num_numerical: int,
                 hidden=(128, 64), n_classes=3, dropout=0.3):
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


def run_epoch(model, loader, criterion, optimizer=None):
    is_train = optimizer is not None
    model.train() if is_train else model.eval()
    total_loss, all_preds, all_labels = 0.0, [], []
    with torch.set_grad_enabled(is_train):
        for cat_x, num_x, y in loader:
            cat_x, num_x, y = cat_x.to(device), num_x.to(device), y.to(device)
            logits = model(cat_x, num_x)
            loss = criterion(logits, y)
            if is_train:
                optimizer.zero_grad()
                loss.backward()
                optimizer.step()
            total_loss += loss.item() * len(y)
            all_preds.append(logits.argmax(1).cpu().numpy())
            all_labels.append(y.cpu().numpy())
    preds = np.concatenate(all_preds)
    labels = np.concatenate(all_labels)
    return total_loss / len(labels), f1_score(labels, preds, average="macro")


def main():
    torch.manual_seed(42)
    np.random.seed(42)

    df = pd.read_csv(DATA_PATH)
    df["festival_date"] = pd.to_datetime(df["festival_date"])
    df = df.sort_values("festival_date").reset_index(drop=True)

    df, sigungu_map = fix_sigungu_mapping(df)
    df["level"] = build_labels(df)
    df = add_lag_features(df)
    df = df.sort_values("festival_date").reset_index(drop=True)

    label_to_idx = {lab: i for i, lab in enumerate(LEVEL_LABELS)}
    df["level_idx"] = df["level"].map(label_to_idx)

    # LightGBM v3와 동일한 시간 기준 분할
    cutoff_idx = int(len(df) * 0.85)
    cutoff_date = df["festival_date"].iloc[cutoff_idx]
    train_mask = df["festival_date"] < cutoff_date
    # 학습 구간 내에서 마지막 10%를 검증(조기 종료)용으로 추가 분리, 테스트셋은 끝까지 안 건드림
    train_df_full = df[train_mask].reset_index(drop=True)
    val_cut = int(len(train_df_full) * 0.9)
    val_split_date = train_df_full["festival_date"].iloc[val_cut]
    val_mask_within_train = train_df_full["festival_date"] >= val_split_date

    train_df = train_df_full[~val_mask_within_train]
    val_df = train_df_full[val_mask_within_train]
    test_df = df[~train_mask]

    print(f"분할: 학습 {len(train_df)} / 검증 {len(val_df)} / 테스트 {len(test_df)} "
          f"(테스트 기준일 {cutoff_date.date()}, LightGBM v3와 동일)\n")

    # 인코더/스케일러는 학습 데이터로만 fit (검증·테스트엔 transform만)
    cat_encoders = fit_categorical_encoders(train_df, CATEGORICAL_COLS)
    medians = {c: train_df[c].median() for c in MISSING_FLAG_COLS}

    def prep(split_df):
        cat_arr = apply_categorical_encoders(split_df, CATEGORICAL_COLS, cat_encoders).values
        return cat_arr, split_df["level_idx"].values

    cat_train, y_train = prep(train_df)
    cat_val, y_val = prep(val_df)
    cat_test, y_test = prep(test_df)

    num_train, scaler, num_cols = build_numerical_matrix(train_df, medians)
    num_val, _, _ = build_numerical_matrix(val_df, medians, scaler)
    num_test, _, _ = build_numerical_matrix(test_df, medians, scaler)

    train_ds = TabularDataset(cat_train, num_train, y_train)
    val_ds = TabularDataset(cat_val, num_val, y_val)
    test_ds = TabularDataset(cat_test, num_test, y_test)

    train_loader = DataLoader(train_ds, batch_size=256, shuffle=True)
    val_loader = DataLoader(val_ds, batch_size=512, shuffle=False)
    test_loader = DataLoader(test_ds, batch_size=512, shuffle=False)

    cat_cardinalities = [len(cat_encoders[c]) for c in CATEGORICAL_COLS]
    model = CongestionNet(cat_cardinalities, num_numerical=num_train.shape[1]).to(device)
    optimizer = torch.optim.Adam(model.parameters(), lr=1e-3, weight_decay=1e-4)
    criterion = nn.CrossEntropyLoss()

    best_val_f1, best_state, patience, patience_left = 0.0, None, 15, 15
    max_epochs = 200

    for epoch in range(1, max_epochs + 1):
        train_loss, train_f1 = run_epoch(model, train_loader, criterion, optimizer)
        val_loss, val_f1 = run_epoch(model, val_loader, criterion)

        if val_f1 > best_val_f1:
            best_val_f1, best_state, patience_left = val_f1, {k: v.clone() for k, v in model.state_dict().items()}, patience
        else:
            patience_left -= 1

        if epoch % 10 == 0 or patience_left == 0:
            print(f"epoch {epoch:3d} | train loss {train_loss:.4f} f1 {train_f1:.4f} "
                  f"| val loss {val_loss:.4f} f1 {val_f1:.4f}")

        if patience_left == 0:
            print(f"-> 검증 macro-F1이 {patience}epoch 동안 개선 없어 조기 종료\n")
            break

    model.load_state_dict(best_state)  # 검증 macro-F1 최고 시점 가중치로 복원
    print(f"최고 검증 macro-F1: {best_val_f1:.4f}\n")

    # 최종 테스트 평가
    model.eval()
    all_preds, all_labels = [], []
    with torch.no_grad():
        for cat_x, num_x, y in test_loader:
            logits = model(cat_x.to(device), num_x.to(device))
            all_preds.append(logits.argmax(1).cpu().numpy())
            all_labels.append(y.numpy())
    preds = np.concatenate(all_preds)
    labels = np.concatenate(all_labels)

    print("=== PyTorch 신경망 최종 평가 (테스트, LightGBM v3와 동일 구간) ===")
    print("Accuracy:", round(accuracy_score(labels, preds), 4))
    print("Macro F1:", round(f1_score(labels, preds, average="macro"), 4))
    print()
    print(classification_report(labels, preds, target_names=LEVEL_LABELS))
    print("혼동행렬 (행=실제, 열=예측, 순서:", LEVEL_LABELS, "):")
    print(confusion_matrix(labels, preds))
    print("\n참고 - LightGBM v3: Accuracy 0.6114 / Macro F1 0.6076")

    torch.save({
        "model_state": model.state_dict(),
        "cat_cardinalities": cat_cardinalities,
        "num_numerical": num_train.shape[1],
        "categorical_cols": CATEGORICAL_COLS,
        "numerical_cols": num_cols,
        "label_to_idx": label_to_idx,
    }, "congestion_model_torch.pt")
    joblib.dump(cat_encoders, "torch_cat_encoders.pkl")
    joblib.dump(scaler, "torch_num_scaler.pkl")
    joblib.dump(medians, "torch_num_medians.pkl")
    print("\n저장 완료: congestion_model_torch.pt, torch_cat_encoders.pkl, "
          "torch_num_scaler.pkl, torch_num_medians.pkl")


if __name__ == "__main__":
    main()
