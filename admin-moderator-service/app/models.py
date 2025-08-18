from sqlalchemy import Column, Integer, String, Boolean, DateTime, func, Enum
from .database import Base
from enum import Enum as PythonEnum

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(255), unique=True, index=True)
    email = Column(String(255), unique=True)
    role = Column(String(50))  
    status = Column(String(50), default='active')  
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=func.now())

class Service(Base):
    __tablename__ = "services"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), index=True)
    description = Column(String(255))
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

class ViolationReport(Base):
    __tablename__ = "violation_reports"
    id = Column(Integer, primary_key=True, index=True)
    reporter_id = Column(Integer)  
    reported_id = Column(Integer)  
    description = Column(String(1024))
    status = Column(String(50), default='pending') 
    created_at = Column(DateTime, default=func.now())

class TutorActivityStatus(PythonEnum):
    TEACHING = "teaching"
    OFFLINE = "offline"
    BUSY = "busy"

class Tutor(Base):
    __tablename__ = "tutor"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(100), unique=True, index=True)
    description = Column(String(1024))
    is_active = Column(Boolean, default=True)
    activity_status = Column(Enum(TutorActivityStatus), default=TutorActivityStatus.TEACHING)
    created_at = Column(DateTime(timezone=True), server_default=func.now())