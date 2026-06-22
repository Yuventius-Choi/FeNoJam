from fastapi import APIRouter, UploadFile, File, Request

router = APIRouter()

@router.post("/predict")
async def predict(
        request: Request,
        file: UploadFile = File(...)
):
    data = await file.read()

    predictor = request.app.state.predictor
    result = predictor.predict(data)

    return result

@router.get("/health")
async def health():
    return {"status": "ok"}