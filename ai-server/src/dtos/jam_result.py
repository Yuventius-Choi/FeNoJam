from pydantic import BaseModel
from datetime import datetime

from event_jam import EventJam
from traffic import Traffic

class JamResult(BaseModel):
    id: int
    name: str
    lat: float
    lng: float
    address: str
    stDate: datetime
    edDate: datetime
    thumbnail: str
    weights: list[EventJam]
    traffics: list[Traffic]