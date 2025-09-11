import logging
from typing import List, Dict, Any
from concurrent.futures import ThreadPoolExecutor, as_completed
from app.services.user_service import user_service
from app.services.tutor_service import tutor_service
from app.services.student_service import student_service

logger = logging.getLogger(__name__)

class RoleService:
    def __init__(self):
        self.max_workers = 5  # Giới hạn số luồng đồng thời để tránh overwhelm dịch vụ khác
    def get_all_users_by_role(self, role: str) -> List[Dict[str, Any]]:
        try:
            all_users = []
            page = 0
            while True:
                users = user_service.get_all_users(page)
                if not users:
                    break
                all_users.extend(users)
                if len(users) < 6:
                    break
                page += 1

            filtered_users = [user for user in all_users if user.get('role') == role]
            return filtered_users
            
        except Exception as e:
            return []
    
    def get_all_tutors_with_details(self) -> Dict[str, Any]:
        try:
            tutor_users = self.get_all_users_by_role("TUTOR")
            
            if not tutor_users:
                return {
                    "total_tutor_users": 0,
                    "tutors_with_profiles": 0,
                    "tutors_without_profiles": 0,
                    "tutors": []
                }
            
            enriched_tutors = []
            
            for user in tutor_users:
                try:
                    user_id = user.get('id')
                    tutor_data = tutor_service.get_tutor_by_user_id(user_id)
                    
                    enriched_tutor = {
                        "user_id": user_id,
                        "email": user.get('email'),
                        "username": user.get('username'),
                        "role": user.get('role'),
                        "has_tutor_profile": tutor_data is not None,
                        "tutor_details": tutor_data
                    }
                    
                    enriched_tutors.append(enriched_tutor)
                    
                except Exception as e:
                    enriched_tutors.append({
                        "user_id": user.get('id'),
                        "email": user.get('email'),
                        "username": user.get('username'),
                        "role": user.get('role'),
                        "has_tutor_profile": False,
                        "tutor_details": None,
                        "error": str(e)
                    })
            
            tutors_with_profiles = len([t for t in enriched_tutors if t["has_tutor_profile"]])
            tutors_without_profiles = len(tutor_users) - tutors_with_profiles
            
            return {
                "total_tutor_users": len(tutor_users),
                "tutors_with_profiles": tutors_with_profiles,
                "tutors_without_profiles": tutors_without_profiles,
                "sync_percentage": round((tutors_with_profiles / len(tutor_users)) * 100, 2) if tutor_users else 0,
                "tutors": enriched_tutors
            }
            
        except Exception as e:
            return {
                "error": str(e),
                "total_tutor_users": 0,
                "tutors_with_profiles": 0,
                "tutors_without_profiles": 0,
                "tutors": []
            }
    
    def get_all_students_with_details(self) -> Dict[str, Any]:
        try:
            student_users = self.get_all_users_by_role("STUDENT")
            
            if not student_users:
                return {
                    "total_student_users": 0,
                    "students_with_profiles": 0,
                    "students_without_profiles": 0,
                    "students": []
                }
            
            enriched_students = []
            for user in student_users:
                try:
                    user_id = user.get('id')
                    student_data = student_service.get_student_by_user_id(user_id)
                    
                    enriched_student = {
                        "user_id": user_id,
                        "email": user.get('email'),
                        "username": user.get('username'),
                        "role": user.get('role'),
                        "has_student_profile": student_data is not None,
                        "student_details": student_data
                    }
                    
                    enriched_students.append(enriched_student)
                    
                except Exception as e:
                    enriched_students.append({
                        "user_id": user.get('id'),
                        "email": user.get('email'),
                        "username": user.get('username'),
                        "role": user.get('role'),
                        "has_student_profile": False,
                        "student_details": None,
                        "error": str(e)
                    })
            
            students_with_profiles = len([s for s in enriched_students if s["has_student_profile"]])
            students_without_profiles = len(student_users) - students_with_profiles
            
            return {
                "total_student_users": len(student_users),
                "students_with_profiles": students_with_profiles,
                "students_without_profiles": students_without_profiles,
                "sync_percentage": round((students_with_profiles / len(student_users)) * 100, 2) if student_users else 0,
                "students": enriched_students
            }
            
        except Exception as e:
            return {
                "error": str(e),
                "total_student_users": 0,
                "students_with_profiles": 0,
                "students_without_profiles": 0,
                "students": []
            }

role_service = RoleService()