from pydantic import BaseModel
from datetime import datetime

from traffic_jam import TrafficJam

class Traffic(BaseModel):
    date: datetime
    weights: list[TrafficJam]