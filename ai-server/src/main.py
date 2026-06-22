from fastapi import FastAPI
from contextlib import asynccontextmanager

from api.api import router
from service.sample_service import SampleService

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("AI Server is starting...")
    app.state.predictor = SampleService()
    print("AI Server is ready!")
    yield
    print("AI Server is closing...")
    ## 종료될 때 필요한 작업을 아래에 추가합니다.
    ### print("TODO: close whatever you need")
    print("AI Server is closed!")

app = FastAPI(
    title="FeNoJam AI Server",
    lifespan=lifespan
)

app.include_router(router)