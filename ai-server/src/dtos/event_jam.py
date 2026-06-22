from pydantic import BaseModel
from datetime import datetime

class EventJam(BaseModel):
    date: datetime
    weight: float