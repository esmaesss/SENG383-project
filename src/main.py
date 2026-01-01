import sys
import os
from PyQt5.QtWidgets import QApplication
from gui import BeePlanApp
from utils import load_instructors, load_courses

def main():
    app = QApplication(sys.argv)
    
    # 1. Path Setup
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    data_dir = os.path.join(base_dir, 'data', 'raw')
    
    inst_path = os.path.join(data_dir, 'instructers.csv')
    curr_path = os.path.join(data_dir, 'curriculum.csv')
    
    # Check if files exist
    if not os.path.exists(inst_path) or not os.path.exists(curr_path):
        print("Error: CSV files not found in 'data/raw/'. Please create them.")
        return

    # 2. Load Data
    print("Loading BeePlan data...")
    instructors = load_instructors(inst_path)
    courses = load_courses(curr_path, instructors)
    
    print(f"Loaded {len(courses)} courses.")

    # 3. Launch GUI
    window = BeePlanApp(courses, instructors)
    window.show()
    
    sys.exit(app.exec_())

if __name__ == "__main__":
    main()
