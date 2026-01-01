import copy
from models import TimeSlot
from constraints import ConstraintManager

class BeeScheduler:
    def __init__(self, courses):
        self.courses = courses
        # Schedule Key: (Year, Day, Hour) -> Value: Course Code (str)
        self.schedule = {}
        self.cm = ConstraintManager()

    def solve(self):
        """Main entry point to generate schedule."""
        self.schedule = {}
        # 1. Reset instructor assignments for a fresh run
        for c in self.courses:
            c.instructor.assigned_slots = []

        # 2. Separate Fixed (Common) vs Flexible Courses
        # In a real scenario, you'd populate fixed_slots from common_courses.csv here.
        # For now, we assume fixed_slots are empty or pre-filled.
        
        flexible_courses = [c for c in self.courses if not c.fixed_slots]
        
        # 3. Sort courses to solve hardest first (High hours first)
        flexible_courses.sort(key=lambda x: x.theory_hours + x.lab_hours, reverse=True)

        if self._assign_course_recursively(flexible_courses, 0):
            return self.schedule
        else:
            return None

    def _assign_course_recursively(self, courses, index):
        if index == len(courses):
            return True  # All done!

        course = courses[index]
        
        # Try to find slots for THEORY hours
        theory_solutions = self._find_valid_combinations(course, course.theory_hours, is_lab=False)
        
        for theory_slots in theory_solutions:
            # Commit Theory
            self._commit_slots(course, theory_slots)
            
            # Now try to find LAB hours (must be after theory)
            if course.lab_hours > 0:
                # Find earliest theory slot to ensure lab is after
                # Sort theory slots by time
                sorted_theory = sorted(theory_slots, key=lambda s: s.to_sort_key())
                last_theory_slot = sorted_theory[-1]
                
                lab_solutions = self._find_valid_combinations(
                    course, course.lab_hours, is_lab=True, after_slot=last_theory_slot
                )
                
                found_lab = False
                for lab_slots in lab_solutions:
                    self._commit_slots(course, lab_slots)
                    if self._assign_course_recursively(courses, index + 1):
                        return True # Success
                    self._rollback_slots(course, lab_slots) # Backtrack Lab
            else:
                # No lab, just recurse
                if self._assign_course_recursively(courses, index + 1):
                    return True

            # Backtrack Theory
            self._rollback_slots(course, theory_slots)

        return False

    def _find_valid_combinations(self, course, hours_needed, is_lab, after_slot=None):
        """
        Finds all valid sets of slots for 'hours_needed'.
        Currently implements a simple search (non-consecutive allowed for flexibility).
        Returns a List[List[TimeSlot]].
        """
        valid_slots = []
        
        # Iterate all times
        for day in self.cm.DAYS:
            for hour in self.cm.HOURS:
                # Lab Timing Check (Must be AFTER theory)
                if after_slot:
                    current_metric = self.cm.DAYS.index(day) * 100 + hour
                    limit_metric = self.cm.DAYS.index(after_slot.day) * 100 + after_slot.start_hour
                    if current_metric <= limit_metric:
                        continue

                # Standard Constraints
                if self.cm.is_exam_block(day, hour): continue
                if not self.cm.check_instructor_availability(course.instructor, day, hour, is_lab): continue
                if not self.cm.check_year_overlap(self.schedule, course.year, day, hour): continue
                
                valid_slots.append(TimeSlot(day, hour, is_lab))

        # Determine combinations (simplified: take first N available)
        # In a robust system, this generates combinations. Here we take chunks.
        if len(valid_slots) >= hours_needed:
            # Return just one valid option for performance in this demo
            # Ideally, yield multiple options
            return [valid_slots[:hours_needed]]
        return []

    def _commit_slots(self, course, slots):
        for slot in slots:
            self.schedule[(course.year, slot.day, slot.start_hour)] = course.code
            course.instructor.assigned_slots.append(slot)

    def _rollback_slots(self, course, slots):
        for slot in slots:
            key = (course.year, slot.day, slot.start_hour)
            if key in self.schedule:
                del self.schedule[key]
            if slot in course.instructor.assigned_slots:
                course.instructor.assigned_slots.remove(slot)