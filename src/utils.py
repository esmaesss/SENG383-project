import csv
import json
from models import Course, Instructor

def parse_hours(hours_str: str):
    hours_str = str(hours_str).strip()
    if '+' in hours_str:
        parts = hours_str.split('+')
        return int(parts[0]), int(parts[1])
    elif hours_str.isdigit():
        return int(hours_str), 0
    return 0, 0

def load_instructors(filepath: str):
    instructors = {}
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                name = row['Name'].strip()
                instructors[name] = Instructor(name=name)
    except Exception as e:
        print(f"Error loading instructors: {e}")
    return instructors

def load_courses(filepath: str, instructor_map):
    courses = []
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                t, l = parse_hours(row['Hours'])
                inst_name = row['Instructor'].strip()
                
                # Default to a generic instructor if missing
                if inst_name not in instructor_map:
                    instructor_map[inst_name] = Instructor(name=inst_name)
                
                courses.append(Course(
                    year=int(row['Year']),
                    code=row['Code'].strip(),
                    name=row['Name'].strip(),
                    instructor=instructor_map[inst_name],
                    theory_hours=t,
                    lab_hours=l
                ))
    except Exception as e:
        print(f"Error loading courses: {e}")
    return courses