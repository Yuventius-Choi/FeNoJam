import torch

# 이대로는 안돌아갑니다!
# ../models/ 경로에 있는 pt 파일을 열기
class SampleService:
    def __init__(self):
        self.model = self._load_model()

    def _load_model(self):
        model = torch.load("../models/model.pt")
        model.eval()
        return model

    def predict(self, datas):
        ## 여기에 Business logic을 정의합니다.
        pred = self.model(datas)
        return pred