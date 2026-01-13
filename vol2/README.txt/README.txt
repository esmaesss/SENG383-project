🐝 KidTask: Task & Wish Management System
KidTask is a Java-based Desktop application designed to help children manage their daily responsibilities while earning rewards. It features a robust Graphical User Interface (GUI) and role-based access for Children, Parents, and Teachers.

📋 Features
Role-Based Access Control (RBAC):

Child: View tasks, mark them as completed, and request "wishes" (rewards).

Parent: Assign tasks, approve completed work, and manage the wish list.

Teacher: Assign educational tasks and provide feedback.

Gamified Progress: A visual progress bar and leveling system that updates dynamically as tasks are approved.

Data Persistence: All data is saved locally in the /data directory using structured text files.

Input Validation: Robust handling of numerical inputs to prevent application crashes.

🏗️ Project Structure
The project follows a standard Maven architecture:

Plaintext

KIDTASK/
├── pom.xml                # Maven configuration and dependencies
├── src/
│   └── main/
│       └── java/
│           └── KidTaskApp.java # Main application logic and GUI
├── data/                  # Local database (Auto-generated)
│   ├── tasks.txt          # Stores all task records
│   └── wishes.txt         # Stores points, level, and wish records
└── README.md              # Project documentation
🚀 Getting Started
Prerequisites
Java JDK 17 or higher.

Apache Maven installed.

🛠️ Technical Details
Language: Java 17

GUI Library: Java Swing (System Look & Feel)

Build Tool: Maven

Storage: File-based I/O (BufferedReader/PrintWriter)