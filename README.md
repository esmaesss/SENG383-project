# SENG383-project

The Design Part includes the GUI And Diagrams (both Activity and Class) for two projects called "Kid Task" and "Bee Plan"

## Kid Task
Kid Task is a Python-based GUI application which aims to help children manage their daily/weekly tasks and handle wishes created by children with parental and teacher approval

<img width="2854" height="1338" alt="KidTask_StudentAÇocukAnasayfa" src="https://github.com/user-attachments/assets/bf3b6312-dc3d-48a8-b039-abe3d3b59ba1" />

## Bee Plan
Bee Plan is a Python-based GUI application which generates eight outputs of conflict-free university schedules for software engineering department.
Takes CSV or Json as input (Common course schedule ● Department curriculum (course name, hours, instructor, theory/lab info) ● Instructor availability ● Scheduling rules and priorities (e.g., Friday exam block, lab after theory) ● Classroom and lab list (capacity, type) )

<img width="2826" height="height="1324" alt="CanvaBeePlanGUI_StudentB" src="https://github.com/user-attachments/assets/825ae530-7bee-4743-a974-f69bafaeabfd" />

### BeePlan - Department Course Scheduler

A PyQt5-based application for generating conflict-free course schedules for university departments.

#### Features

- **Automatic Schedule Generation**: Uses constraint-based algorithms to create conflict-free schedules
- **Interactive Timetable**: Visual timetable with year-based filtering
- **Course Information Panel**: Detailed view of all courses and instructors
- **Schedule Statistics**: Real-time statistics about schedule utilization and conflicts
- **Export Functionality**: Export schedules to JSON format
- **Modern UI**: Clean, responsive interface with tooltips and styling

#### Installation

1. Install Python 3.7+ if not already installed
2. Clone or download this repository
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

#### Usage

1. Ensure your CSV files are in the `data/raw/` directory:
   - `curriculum.csv`: Course information (Year, Code, Name, Hours, Instructor)
   - `instructers.csv`: Instructor information (Name, MaxTheory)

2. Run the application:
   ```bash
   python src/main.py
   ```

3. Use the interface to:
   - Generate schedules
   - Filter by year
   - View course details
   - Export schedules

#### Data Format

##### curriculum.csv
```
Year,Code,Name,Hours,Instructor
1,SENG 101,Computer Prog I,3+2,S.Esmelioglu
2,SENG 201,Data Structures,3+2,B.Çelikkale
```

##### instructers.csv
```
Name,MaxTheory
B.Avenoglu,4
S.Esmelioglu,4
```

#### Features Overview

- **Left Panel**: Controls, course information, and statistics
- **Right Panel**: Interactive timetable grid
- **Menu Bar**: File operations and help
- **Status Bar**: Application status messages
- **Cell Interaction**: Click cells to see detailed schedule information

#### Constraints

- No courses on Friday 13:20-15:10 (exam block)
- Instructors limited to 4 theory hours per day
- No year-group conflicts at the same time
- Lab sessions must follow theory sessions

## The Coding Part includes:
Coding Task1: Algorithm implementations, clean coding and error handling mechanisms (KidTask, Tool: Cursor)
Coding Task2: GUI implementation and data structures (BeePlan, Tool: CoPilot)
Coding Task3: Review Coding Tasks of Student A
Coding Task4: Evaluation of Coding Tools (Task1,Task2)
