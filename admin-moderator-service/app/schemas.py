from pydantic import BaseModel
from datetime import datetime
from typing import Optional
from enum import Enum

class UserBase(BaseModel):
    username: str
    email: str
    role: str
    status: str = 'active'

class UserCreate(UserBase):
    pass

class UserUpdate(BaseModel):
    status: Optional[str] = None
    role: Optional[str] = None

class UserOut(UserBase):
    id: int
    created_at: datetime
    is_active: bool

    class Config:
        orm_mode = True

class ServiceBase(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None

class ServiceCreate(ServiceBase):
    pass

class ServiceUpdate(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None

    class Config:
        orm_mode = True

class ServiceOut(BaseModel):
    id: int
    name: str
    description: Optional[str] = None
    is_active: bool
    created_at: datetime

    class Config:
        orm_mode = True

class ViolationReportBase(BaseModel):
    reporter_id: int
    reported_id: int
    description: str

class ViolationReportCreate(ViolationReportBase):
    pass

class ViolationReportUpdate(BaseModel):
    status: str = 'resolved'

class ViolationReportOut(ViolationReportBase):
    id: int
    status: str
    created_at: datetime

    class Config:
        orm_mode = True

class TutorActivityStatus(str, Enum):
    TEACHING = "teaching"
    RESTING = "resting"

class TutorBase(BaseModel):
    username: str
    description: Optional[str] = None
    activity_status: Optional[TutorActivityStatus] = TutorActivityStatus.TEACHING

class TutorCreate(TutorBase):
    pass

class TutorUpdate(BaseModel):
    username: Optional[str] = None
    description: Optional[str] = None
    activity_status: Optional[TutorActivityStatus] = None

    class Config:
        orm_mode = True

class TutorOut(BaseModel):
    id: int
    username: str
    description: Optional[str] = None
    is_active: bool
    activity_status: TutorActivityStatus
    created_at: datetime

    class Config:
        orm_mode = True