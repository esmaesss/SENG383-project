from models import Instructor

class ConstraintManager:
    DAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
    HOURS = [9, 10, 11, 12, 13, 14, 15, 16] 

    @staticmethod
    def is_exam_block(day: str, hour: int) -> bool:
        # Rule: No courses Friday 13:20–15:10
        return day == "Friday" and hour in [13, 14]

    @staticmethod
    def check_instructor_availability(instructor: Instructor, day: str, hour: int, is_lab: bool) -> bool:
        # 1. Check if already busy at this specific time
        for slot in instructor.assigned_slots:
            if slot.day == day and slot.start_hour == hour:
                return False
        
        # 2. Check max daily theory load (4 hours)
        if not is_lab:
            if not instructor.can_teach_theory(day):
                return False
        return True

    @staticmethod
    def check_year_overlap(schedule_map, year: int, day: str, hour: int):
        # Rule: Only one course per year-group at a time
        if (year, day, hour) in schedule_map:
            return False
        return True