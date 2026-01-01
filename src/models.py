from dataclasses import dataclass, field
from typing import List, Tuple

@dataclass
class TimeSlot:
    day: str        # "Monday", "Tuesday", etc.
    start_hour: int # 9, 10, ... 16
    is_lab: bool = False

    def __repr__(self):
        type_str = "Lab" if self.is_lab else "Theory"
        return f"{self.day} {self.start_hour}:20 ({type_str})"

    def to_sort_key(self):
        # Helper to compare times: Day Index * 100 + Hour
        days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
        return days.index(self.day) * 100 + self.start_hour

@dataclass
class Instructor:
    name: str
    max_daily_theory_hours: int = 4
    assigned_slots: List[TimeSlot] = field(default_factory=list)

    def can_teach_theory(self, day: str) -> bool:
        theory_today = sum(1 for slot in self.assigned_slots 
                           if slot.day == day and not slot.is_lab)
        return theory_today < self.max_daily_theory_hours

@dataclass
class Course:
    year: int
    code: str
    name: str
    instructor: Instructor
    theory_hours: int
    lab_hours: int
    is_elective: bool = False
    # Fixed slots for common courses (Math, Phys)
    fixed_slots: List[TimeSlot] = field(default_factory=list) 

    def __repr__(self):
        return f"{self.code} ({self.theory_hours}+{self.lab_hours})"