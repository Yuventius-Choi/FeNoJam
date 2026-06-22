from pydantic import BaseModel
from datetime import datetime

class TrafficJam(BaseModel):
    datetime: datetime
    weight: float
