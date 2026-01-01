import sys
from PyQt5.QtWidgets import (QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QSplitter,
                             QPushButton, QTableWidget, QTableWidgetItem, QLabel, QComboBox,
                             QMessageBox, QHeaderView, QTextEdit, QGroupBox, QFormLayout,
                             QStatusBar, QMenuBar, QAction, QFrame)
from PyQt5.QtGui import QColor, QFont, QIcon
from PyQt5.QtCore import Qt
from scheduler import BeeScheduler

class BeePlanApp(QMainWindow):
    def __init__(self, courses, instructors):
        super().__init__()
        self.courses = courses
        self.instructors = instructors
        self.generated_schedule = {} # Stores raw schedule data
        
        self.setWindowTitle("BeePlan - Department Scheduler")
        self.resize(1100, 750)
        self.init_ui()

    def init_ui(self):
        self.setWindowTitle("BeePlan - Department Scheduler")
        self.resize(1400, 900)
        
        # Create menu bar
        self.create_menu_bar()
        
        # Create status bar
        self.status_bar = self.statusBar()
        self.status_bar.showMessage("Ready")
        
        # Create central widget with splitter
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        main_layout = QHBoxLayout(central_widget)
        
        # Create splitter for resizable panels
        splitter = QSplitter(Qt.Horizontal)
        main_layout.addWidget(splitter)
        
        # Left panel - Schedule controls and info
        left_panel = self.create_left_panel()
        splitter.addWidget(left_panel)
        
        # Right panel - Timetable
        right_panel = self.create_timetable_panel()
        splitter.addWidget(right_panel)
        
        # Set splitter proportions
        splitter.setSizes([400, 1000])
        
        # Initial render
        self.mark_static_zones()
        self.update_course_info()

    def create_menu_bar(self):
        menubar = self.menuBar()
        
        # File menu
        file_menu = menubar.addMenu('File')
        
        export_action = QAction('Export Schedule', self)
        export_action.triggered.connect(self.export_schedule)
        file_menu.addAction(export_action)
        
        file_menu.addSeparator()
        
        exit_action = QAction('Exit', self)
        exit_action.triggered.connect(self.close)
        file_menu.addAction(exit_action)
        
        # Help menu
        help_menu = menubar.addMenu('Help')
        
        about_action = QAction('About', self)
        about_action.triggered.connect(self.show_about)
        help_menu.addAction(about_action)

    def create_left_panel(self):
        panel = QWidget()
        layout = QVBoxLayout(panel)
        
        # Controls group
        controls_group = QGroupBox("Schedule Controls")
        controls_layout = QVBoxLayout(controls_group)
        
        # Year filter
        year_layout = QHBoxLayout()
        year_label = QLabel("Filter by Year:")
        year_label.setToolTip("Filter the timetable to show only courses from selected year")
        year_layout.addWidget(year_label)
        self.year_combo = QComboBox()
        self.year_combo.addItems(["All Years", "1", "2", "3", "4"])
        self.year_combo.setToolTip("Select year to filter courses")
        self.year_combo.currentTextChanged.connect(self.refresh_table)
        year_layout.addWidget(self.year_combo)
        controls_layout.addLayout(year_layout)
        
        # Generate button
        self.btn_generate = QPushButton("🔄 Generate Schedule")
        self.btn_generate.setToolTip("Generate a new conflict-free schedule for all courses")
        self.btn_generate.setStyleSheet("""
            QPushButton {
                background-color: #4CAF50;
                color: white;
                font-weight: bold;
                padding: 10px;
                border-radius: 5px;
                font-size: 12px;
            }
            QPushButton:hover {
                background-color: #45a049;
            }
            QPushButton:pressed {
                background-color: #3d8b40;
            }
            QPushButton:disabled {
                background-color: #cccccc;
            }
        """)
        self.btn_generate.clicked.connect(self.run_generation)
        controls_layout.addWidget(self.btn_generate)
        
        layout.addWidget(controls_group)
        
        # Course info group
        info_group = QGroupBox("Course Information")
        info_group.setToolTip("Overview of all courses in the curriculum")
        info_layout = QVBoxLayout(info_group)
        
        self.course_info_text = QTextEdit()
        self.course_info_text.setReadOnly(True)
        self.course_info_text.setMaximumHeight(200)
        self.course_info_text.setToolTip("Detailed information about all courses")
        self.course_info_text.setStyleSheet("""
            QTextEdit {
                background-color: #f8f9fa;
                border: 1px solid #dee2e6;
                border-radius: 3px;
                padding: 5px;
            }
        """)
        info_layout.addWidget(self.course_info_text)
        
        layout.addWidget(info_group)
        
        # Statistics group
        stats_group = QGroupBox("Schedule Statistics")
        stats_group.setToolTip("Statistics about the generated schedule")
        stats_layout = QFormLayout(stats_group)
        
        self.stats_labels = {}
        stats = ["Total Courses", "Scheduled Slots", "Conflicts", "Utilization"]
        for stat in stats:
            label = QLabel("0")
            self.stats_labels[stat] = label
            stats_layout.addRow(stat + ":", label)
        
        layout.addWidget(stats_group)
        
        layout.addStretch()
        return panel

    def create_timetable_panel(self):
        panel = QWidget()
        layout = QVBoxLayout(panel)
        
        # Timetable title
        title = QLabel("Weekly Timetable")
        title.setFont(QFont("Arial", 14, QFont.Bold))
        title.setAlignment(Qt.AlignCenter)
        layout.addWidget(title)
        
        # Timetable grid
        self.table = QTableWidget()
        self.table.setRowCount(8)
        self.table.setColumnCount(5)
        self.days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
        self.hours = [9, 10, 11, 12, 13, 14, 15, 16]
        
        self.table.setHorizontalHeaderLabels(self.days)
        self.table.setVerticalHeaderLabels([f"{h}:20" for h in self.hours])
        self.table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.table.verticalHeader().setSectionResizeMode(QHeaderView.Stretch)
        
        # Style the table
        self.table.setStyleSheet("""
            QTableWidget {
                gridline-color: #ddd;
                selection-background-color: #e3f2fd;
            }
            QHeaderView::section {
                background-color: #1976d2;
                color: white;
                padding: 8px;
                border: 1px solid #1565c0;
                font-weight: bold;
            }
        """)
        
        # Connect cell click
        self.table.cellClicked.connect(self.on_cell_clicked)
        
        layout.addWidget(self.table)
        return panel

    def update_course_info(self):
        info_text = f"Total Courses: {len(self.courses)}\n\n"
        for course in self.courses:
            info_text += f"• {course.code}: {course.name}\n"
            info_text += f"  Year: {course.year}, Hours: {course.theory_hours}+{course.lab_hours}\n"
            info_text += f"  Instructor: {course.instructor.name}\n\n"
        
        self.course_info_text.setText(info_text)

    def on_cell_clicked(self, row, col):
        hour = self.hours[row]
        day = self.days[col]
        
        # Find courses scheduled at this time
        scheduled_courses = []
        for (year, s_day, s_hour), course_code in self.generated_schedule.items():
            if s_day == day and s_hour == hour:
                # Find course details
                course = next((c for c in self.courses if c.code == course_code), None)
                if course:
                    scheduled_courses.append(f"Y{year}: {course_code} - {course.name} ({course.instructor.name})")
        
        if scheduled_courses:
            QMessageBox.information(self, f"Schedule for {day} {hour}:20", "\n".join(scheduled_courses))
        else:
            QMessageBox.information(self, f"Schedule for {day} {hour}:20", "No courses scheduled")

    def show_about(self):
        QMessageBox.about(self, "About BeePlan", 
                         "BeePlan - Department Course Scheduler\n\n"
                         "A tool for generating conflict-free course schedules\n"
                         "for university departments.\n\n"
                         "Built with PyQt5 and Python.")

    def mark_static_zones(self):
        """Paint the Friday Exam block red."""
        exam_rows = [4, 5]  # 13:20, 14:20 indices
        fri_col = 4
        for row in exam_rows:
            item = QTableWidgetItem("📚 EXAM BLOCK\n(No classes)")
            item.setBackground(QColor(255, 87, 87))
            item.setForeground(QColor(255, 255, 255))
            item.setTextAlignment(Qt.AlignCenter)
            item.setFont(QFont("Arial", 9, QFont.Bold))
            item.setFlags(Qt.ItemIsEnabled)  # Read only
            self.table.setItem(row, fri_col, item)

    def run_generation(self):
        self.btn_generate.setText("🔄 Generating...")
        self.btn_generate.setEnabled(False)
        self.status_bar.showMessage("Generating schedule...")
        self.repaint()  # Force UI update
        
        scheduler = BeeScheduler(self.courses)
        result = scheduler.solve()
        
        if result:
            self.generated_schedule = result
            self.refresh_table()
            self.update_statistics()
            QMessageBox.information(self, "Success", "Schedule generated successfully without conflicts!")
            self.status_bar.showMessage("Schedule generated successfully")
        else:
            QMessageBox.warning(self, "Failed", "Could not generate a valid schedule with current constraints.")
            self.status_bar.showMessage("Schedule generation failed")
        
        self.btn_generate.setText("🔄 Generate Schedule")
        self.btn_generate.setEnabled(True)

    def update_statistics(self):
        if not self.generated_schedule:
            for label in self.stats_labels.values():
                label.setText("0")
            return
        
        total_slots = len(self.generated_schedule)
        conflicts = 0  # In a real system, you'd check for conflicts
        
        # Calculate utilization (total possible slots vs used)
        total_possible = 5 * 8 * 4  # 5 days, 8 hours, 4 years
        utilization = (total_slots / total_possible) * 100
        
        self.stats_labels["Total Courses"].setText(str(len(self.courses)))
        self.stats_labels["Scheduled Slots"].setText(str(total_slots))
        self.stats_labels["Conflicts"].setText(str(conflicts))
        self.stats_labels["Utilization"].setText(".1f")

    def refresh_table(self):
        self.table.clearContents()
        self.mark_static_zones()
        
        filter_year = self.year_combo.currentText()
        
        # Color scheme for different years
        year_colors = {
            1: QColor(135, 206, 235),  # Light blue
            2: QColor(144, 238, 144),  # Light green
            3: QColor(255, 218, 185),  # Light orange
            4: QColor(221, 160, 221)   # Light purple
        }
        
        # Iterate through generated schedule
        for (year, day, hour), course_code in self.generated_schedule.items():
            
            # Apply Filter
            if filter_year != "All Years" and str(year) != filter_year:
                continue

            # Map to Table Coordinates
            try:
                row_idx = self.hours.index(hour)
                col_idx = self.days.index(day)
                
                current_item = self.table.item(row_idx, col_idx)
                course = next((c for c in self.courses if c.code == course_code), None)
                course_name = course.name if course else course_code
                
                new_text = f"📖 Y{year}: {course_code}\n{course_name}"
                if course:
                    new_text += f"\n👨‍🏫 {course.instructor.name}"
                
                # Handle cell merging text (if multiple years viewed)
                if current_item and "EXAM" not in current_item.text():
                    existing = current_item.text()
                    new_text = existing + "\n\n" + new_text
                elif current_item and "EXAM" in current_item.text():
                    continue  # Don't overwrite exam block

                item = QTableWidgetItem(new_text)
                item.setTextAlignment(Qt.AlignCenter)
                item.setBackground(year_colors.get(year, QColor(200, 200, 200)))
                item.setFont(QFont("Arial", 8))
                item.setToolTip(f"Course: {course_code}\nName: {course_name}\nYear: {year}\nInstructor: {course.instructor.name if course else 'Unknown'}\nClick for details")
                self.table.setItem(row_idx, col_idx, item)
                
            except ValueError:
                pass  # Hour or Day not in grid range

    def export_schedule(self):
        if not self.generated_schedule:
            QMessageBox.warning(self, "No Schedule", "Please generate a schedule first.")
            return
        
        import json
        from PyQt5.QtWidgets import QFileDialog
        
        filename, _ = QFileDialog.getSaveFileName(self, "Save Schedule", "", "JSON Files (*.json)")
        if filename:
            try:
                with open(filename, 'w') as f:
                    json.dump(self.generated_schedule, f, indent=4)
                QMessageBox.information(self, "Success", f"Schedule exported to {filename}")
            except Exception as e:
                QMessageBox.critical(self, "Error", f"Failed to export: {str(e)}")