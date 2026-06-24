<div align="center">
  <img width="220" alt="SMRTS Logo" src="https://github.com/user-attachments/assets/9ef782d3-3939-4622-82e6-4c8773d380e3" />
  <h1>SMRTS — Smart Maintenance Request & Tracking System</h1>
  <p><strong>v3.0 · Java Swing Desktop Application</strong></p>
</div>

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.x-red?style=flat-square&logo=apachemaven)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![FlatLaf](https://img.shields.io/badge/FlatLaf-3.4-purple?style=flat-square)](https://www.formdev.com/flatlaf/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

*A modern, role-based desktop application for managing facility maintenance requests, technicians, and real-time tracking — built with Java Swing and MySQL.*

</div>

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Setup & Installation](#setup--installation)
- [Default Credentials](#default-credentials)
- [User Guide](#user-guide)
  - [Admin / Technician](#admin--technician-guide)
  - [Regular User (USER Role)](#regular-user-portal-guide)
- [Role Permissions](#role-permissions)
- [Dependencies](#dependencies)
- [Contributors](#contributors)

---

## Overview

SMRTS is a semester group project — a full-featured **maintenance request tracking system** built entirely in Java. It allows facilities staff and end-users to submit, manage, and track maintenance requests through a polished dark-themed GUI. The system supports three user roles (Admin, Technician, User), BCrypt-secured authentication, an offline AI-style chatbot, and reporting.

---

## Features

| Feature | Description |
|---|---|
| 🔐 **Secure Login** | BCrypt-hashed passwords, role-based routing |
| 📋 **Maintenance Requests** | Full CRUD — create, view, edit, delete, filter by status |
| 👷 **Technician Management** | Add, update, delete, search technicians with availability status |
| 👤 **User Management** | Admin can add, edit, delete user accounts and assign roles |
| 📊 **Reports Dashboard** | Summary stats by status, priority, and technician workload |
| 🤖 **AI Chatbot** | Offline keyword-based assistant with live DB context and chat history |
| 🧾 **User Portal** | End-users submit requests and track only their own submissions |
| 🖥️ **Modern Dark UI** | FlatLaf dark theme with custom-painted components and SVG icons |
| 🔎 **Search & Filter** | Search across all tables; filter requests by status |
| ℹ️ **About System** | Shows app version, runtime info, and live DB metadata |

---
## Screenshots

<div align="center">

### 🔐 Login Screen
<img width="800" src="https://github.com/user-attachments/assets/f0b1344a-dbbc-4dc4-b33a-865e33af4d9e" alt="Login Screen"/>

<br/><br/>

### 🖥️ Admin Dashboard
<img width="800" src="https://github.com/user-attachments/assets/faecd4c2-ebc8-4b7b-bd26-ec589c4b6cff" alt="Admin Dashboard"/>

<br/><br/>

### 📋 Maintenance Requests
<img width="800" src="https://github.com/user-attachments/assets/a7dbc947-8a3b-4d11-8c3b-104c913134d9" alt="Maintenance Requests"/>

<br/><br/>

### 📊 Reports Dashboard
<img width="800" src="https://github.com/user-attachments/assets/c91cc3fd-f2a1-4af9-a5d7-3a68fcc899f2" alt="Reports Dashboard"/>

<br/><br/>

### 🤖 AI Chatbot
<img width="800" src="https://github.com/user-attachments/assets/89261dc9-4be1-4528-8df2-9b952db1a69b" alt="AI Chatbot"/>

<br/><br/>

### 👤 User Portal
<img width="800" src="https://github.com/user-attachments/assets/327fb930-1211-40f6-9d91-6b3e943c1bc7" alt="User Portal"/>

</div>

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | Java Swing + FlatLaf 3.4 (Dark theme) |
| Database | MySQL 8.x |
| Build Tool | Apache Maven 3.x |
| Password Security | jBCrypt 0.4 |
| DB Connector | MySQL Connector/J 9.7.0 |
| IDE | NetBeans / IntelliJ IDEA |

---

## Project Structure

```
SMRTS_v3.0/
├── pom.xml                          ← Maven build file
├── README.md
└── src/
    └── main/
        ├── java/
        │   ├── main/
        │   │   └── Main.java                    ← Entry point; applies FlatLaf theme
        │   │
        │   ├── database/
        │   │   └── DBConnection.java             ← MySQL connection factory
        │   │
        │   ├── model/                            ← Plain Java data classes (POJOs)
        │   │   ├── User.java
        │   │   ├── Technician.java
        │   │   ├── MaintenanceRequest.java       ← Includes Priority & Status enums
        │   │   └── ChatMessage.java
        │   │
        │   ├── dao/                              ← Database Access Objects (SQL logic)
        │   │   ├── UserDAO.java
        │   │   ├── TechnicianDAO.java
        │   │   ├── MaintenanceRequestDAO.java
        │   │   └── ChatHistoryDAO.java
        │   │
        │   └── gui/                              ← All Swing UI classes (hand-coded)
        │       ├── LoginForm.java                ← Auth screen; routes by role
        │       ├── AdminDashboard.java           ← Main shell (sidebar + CardLayout)
        │       ├── TechnicianForm.java           ← Technician CRUD panel
        │       ├── MaintenanceRequestForm.java   ← Request CRUD panel
        │       ├── UsersPanel.java               ← User account management panel
        │       ├── ReportsWindow.java            ← Statistics & reports panel
        │       ├── AIChatbotWindow.java          ← Offline chatbot panel
        │       ├── UserPortalWindow.java         ← End-user submission portal
        │       ├── RequestDetailsWindow.java     ← Modal: full request details
        │       └── AboutSystemWindow.java        ← Modal: version & DB info
        │
        └── resources/
            ├── logo.png
            └── icons/
                ├── home.svg
                ├── clipboard-list.svg
                ├── users.svg
                ├── package.svg
                ├── bar-chart.svg
                ├── settings.svg
                └── log-out.svg
```

### Architecture Overview

```
[Main.java]
    │
    ▼
[LoginForm]  ──── BCrypt auth ────►  MySQL: users table
    │
    ├──── Role: ADMIN / TECHNICIAN ────►  [AdminDashboard]
    │                                           │
    │                                    ┌──────┴───────┐
    │                           Sidebar  │  CardLayout  │
    │                           Nav      │              │
    │                                    ├── Home (stats + recent requests)
    │                                    ├── TechnicianForm
    │                                    ├── MaintenanceRequestForm
    │                                    ├── UsersPanel  (Admin only)
    │                                    ├── ReportsWindow
    │                                    └── AIChatbotWindow
    │
    └──── Role: USER ──────────────────►  [UserPortalWindow]
                                               └── Submit & track own requests
```

---

## Database Schema

Run the following SQL in MySQL Workbench or phpMyAdmin to set up the database:

```sql
CREATE DATABASE IF NOT EXISTS smrts_db;
USE smrts_db;

-- Users table
CREATE TABLE users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,           -- BCrypt hash
    role       VARCHAR(50)  NOT NULL DEFAULT 'USER',  -- ADMIN | TECHNICIAN | USER
    full_name  VARCHAR(200)          DEFAULT ''
);

-- Technicians table
CREATE TABLE technicians (
    technician_id INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    contact_no    VARCHAR(30),
    email         VARCHAR(150),
    department    VARCHAR(100),
    status        VARCHAR(50)  DEFAULT 'Available'   -- Available | Busy
);

-- Maintenance Requests table
CREATE TABLE maintenance_requests (
    request_id              INT AUTO_INCREMENT PRIMARY KEY,
    title                   VARCHAR(200)  NOT NULL,
    description             TEXT,
    location                VARCHAR(200),
    priority                VARCHAR(20)   DEFAULT 'MEDIUM',  -- LOW | MEDIUM | HIGH
    status                  VARCHAR(20)   DEFAULT 'PENDING', -- PENDING | IN_PROGRESS | COMPLETED
    assigned_technician_id  INT           DEFAULT NULL,
    submitted_by_username   VARCHAR(100)  DEFAULT NULL,
    created_at              DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_technician_id) REFERENCES technicians(technician_id)
        ON DELETE SET NULL
);

-- Chat History table (AI Chatbot)
CREATE TABLE chat_history (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    role       VARCHAR(20)   NOT NULL,          -- 'user' | 'assistant'
    content    TEXT          NOT NULL,
    username   VARCHAR(100)  NOT NULL,
    user_role  VARCHAR(50),
    created_at DATETIME      DEFAULT CURRENT_TIMESTAMP
);

-- Default admin account (password: admin123)
INSERT INTO users (username, password, role, full_name)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    'System Administrator'
);
```

> **Note:** The BCrypt hash above corresponds to the password `admin123`. To generate a hash for a different password, see the [Security Note](#security-note) section.

---

## Setup & Installation

### Prerequisites

Make sure you have the following installed:

- **Java JDK 17** or higher — [Download](https://www.oracle.com/java/technologies/downloads/)
- **Apache Maven 3.x** — [Download](https://maven.apache.org/download.cgi)
- **MySQL Server 8.x** — [Download](https://dev.mysql.com/downloads/mysql/)
- **MySQL Workbench** (optional, recommended) — [Download](https://dev.mysql.com/downloads/workbench/)
- **NetBeans IDE** or **IntelliJ IDEA** (recommended)

---

### Step 1 — Clone the Repository

```bash
git clone https://github.com/your-username/SMRTS.git
cd SMRTS
```

---

### Step 2 — Set Up the Database

1. Open **MySQL Workbench** (or any MySQL client).
2. Connect to your local MySQL server.
3. Open and run the full SQL script from the [Database Schema](#database-schema) section above.
4. Verify that the `smrts_db` database and its four tables are created.

---

### Step 3 — Configure Database Connection

Open the file:

```
src/main/java/database/DBConnection.java
```

Update the credentials to match your MySQL setup:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/smrts_db?useSSL=false&serverTimezone=UTC";
private static final String USER     = "root";
private static final String PASSWORD = "your_mysql_password";  // ← Change this
```

---

### Step 4 — Build the Project

#### Option A — NetBeans

1. Open NetBeans → `File` → `Open Project` → select the `SMRTS_v3.0` folder.
2. Right-click the project → **Clean and Build**.
3. Maven will automatically download all dependencies.

#### Option B — Command Line (Maven)

```bash
mvn clean install
```

---

### Step 5 — Run the Application

#### Option A — NetBeans

Right-click `Main.java` → **Run File**

#### Option B — Command Line

```bash
mvn exec:java -Dexec.mainClass="main.Main"
```

#### Option C — Run the JAR directly

```bash
java -jar target/SMRTS-2.0.jar
```

> If running the JAR fails due to missing dependencies, use the Maven exec approach or package with dependencies using `mvn package`.

---

### Security Note

To generate a BCrypt hash for a new admin password:

```java
import org.mindrot.jbcrypt.BCrypt;

public class HashUtil {
    public static void main(String[] args) {
        String hashed = BCrypt.hashpw("yourNewPassword", BCrypt.gensalt());
        System.out.println(hashed);
        // Then run: UPDATE users SET password='...' WHERE username='admin';
    }
}
```

---

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |

> Change the default password immediately after your first login. Additional users and technicians can be created from within the application.

---

## User Guide

### Admin / Technician Guide

#### Logging In

1. Launch the application.
2. Enter your **username** and **password**.
3. Click **Login**. The system will route you to the **Admin Dashboard** automatically.

---

#### Home Dashboard

- Displays **stat cards** at the top: Total Requests, Pending, In Progress, Completed, Total Technicians.
- A **Recent Requests** table shows the latest 10 requests with their status and assigned technician.
- Stats refresh automatically when you navigate back to Home.

---

#### Managing Technicians

Navigate to **Technicians** in the sidebar.

| Action | How to do it |
|---|---|
| **Add** | Fill in Name, Contact, Email, Department fields → click **Add** |
| **Update** | Click a row in the table (fields auto-fill) → edit → click **Update** |
| **Delete** | Click a row → click **Delete** → confirm the dialog |
| **Search** | Type in the Search box → results filter in real time |
| **Clear form** | Click **Clear** to reset all fields |

> The status field (Available / Busy) is set automatically when technicians are assigned to requests.

---

#### Managing Maintenance Requests

Navigate to **Requests** in the sidebar.

| Action | How to do it |
|---|---|
| **Add** | Fill Title, Description, Location → choose Priority, Status, Technician → click **Add** |
| **Update** | Click a row → edit fields → click **Update** |
| **Delete** | Click a row → click **Delete** → confirm |
| **Filter** | Use the **Filter** dropdown (All / Pending / In Progress / Completed) |
| **Search** | Type in the Search box to search by title, location, or description |
| **View Details** | Double-click any row to open the full Request Details dialog |

**Priority levels:** 🔴 HIGH — 🟡 MEDIUM — 🟢 LOW

**Status flow:** `PENDING` → `IN_PROGRESS` → `COMPLETED`

---

#### Managing Users *(Admin only)*

Navigate to **Users** in the sidebar.

| Action | How to do it |
|---|---|
| **Add User** | Enter Username, Full Name, Password, select Role → click **Add** |
| **Update User** | Click a row → edit fields → click **Update** |
| **Delete User** | Click a row → click **Delete** → confirm |
| **Search** | Search by username, full name, or role |

**Available Roles:** `ADMIN` · `TECHNICIAN` · `USER`

> Passwords are automatically hashed with BCrypt before being stored. The existing hash is never shown in the form.

---

#### Reports

Navigate to **Reports** in the sidebar.

- **Summary Cards** — Total, Pending, In Progress, Completed counts.
- **Priority Breakdown** — counts for High, Medium, Low priority requests.
- **Workload by Technician** — table showing each technician's pending, in-progress, and completed request counts.
- **Recently Completed** — table of the last 10 completed requests.
- Click **🔄 Refresh** to reload all data.

---

#### AI Chatbot

Navigate to **AI Assistant** in the sidebar.

- Type any question about the system in natural language.
- The chatbot queries the **live database** to answer questions such as:
  - *"How many pending requests are there?"*
  - *"List available technicians"*
  - *"Show high priority requests"*
  - *"Who is assigned to the most requests?"*
- Chat history is **saved per user** across sessions.
- Click **Clear Chat** to wipe your history.

> The chatbot is fully **offline** — it uses keyword matching and direct database queries, not an external AI API.

---

#### About System

Click the **⚙ About** button at the bottom of the sidebar.

- Shows application version, build info, and logged-in user.
- Displays live **database connection info** (host, DB name, MySQL version).
- Shows current server date/time.

---

### Regular User Portal Guide

Users with the **USER** role are routed to a separate, simplified **User Portal** after login.

#### Submitting a New Request

1. Fill in the **Title**, **Description**, **Location** fields.
2. Select a **Priority** (Low / Medium / High).
3. Click **Submit Request**.
4. A success message confirms your submission.

#### Tracking Your Requests

- The **My Requests** table on the right shows only requests *you* have submitted.
- Columns: ID, Title, Status, Priority, Assigned Technician, Date Submitted.
- Click **🔄 Refresh** to reload the latest status updates.
- **Double-click** any row to view full request details.

#### Filtering Your Requests

Use the **Filter** dropdown above the table to view:
- All Requests
- Pending only
- In Progress only
- Completed only

---

## Role Permissions

| Feature | ADMIN | TECHNICIAN | USER |
|---|:---:|:---:|:---:|
| View Dashboard & Stats | ✅ | ✅ | ❌ |
| Manage Maintenance Requests (CRUD) | ✅ | ✅ | ❌ |
| Submit New Request (User Portal) | ❌ | ❌ | ✅ |
| View Own Requests | ❌ | ❌ | ✅ |
| Manage Technicians (CRUD) | ✅ | ❌ | ❌ |
| Manage Users (CRUD) | ✅ | ❌ | ❌ |
| View Reports | ✅ | ✅ | ❌ |
| AI Chatbot | ✅ | ✅ | ❌ |
| About System | ✅ | ✅ | ❌ |

---

## Dependencies

Declared in `pom.xml` — Maven downloads these automatically on build:

| Library | Version | Purpose |
|---|---|---|
| `mysql-connector-j` | 9.7.0 | MySQL JDBC driver |
| `flatlaf` | 3.4 | Modern dark UI Look and Feel |
| `flatlaf-extras` | 3.4 | SVG icon support for FlatLaf |
| `jbcrypt` | 0.4 | BCrypt password hashing |

**Java version:** 17 (set via `maven.compiler.release`)

---

## Troubleshooting

**"Cannot connect to database"**
- Verify MySQL is running.
- Check the credentials in `DBConnection.java` match your MySQL setup.
- Ensure the `smrts_db` database exists (run the setup SQL).

**"BCrypt dependency not found"**
- Run `mvn clean install` to force Maven to re-download all dependencies.

**UI looks plain/grey (no dark theme)**
- Ensure `flatlaf-3.4.jar` was downloaded. Try `mvn dependency:resolve`.

**JAR won't run**
- Use `mvn exec:java` instead, which includes the classpath automatically.

**`submitted_by_username` column warning in logs**
- Run this SQL once: `ALTER TABLE maintenance_requests ADD COLUMN submitted_by_username VARCHAR(100) DEFAULT NULL;`

---

## Contributors

This project was developed as a group semester assignment by:

| # | Name | Role / Contribution |
|---|---|---|
| 1 | **Elankeeran - PE/2023/002** | Project Lead · Admin Dashboard · Login Form |
| 2 | **Kithusan - PE/2023/013** | Database Design · DAO Layer · Reports Module |
| 3 | **Danushan - PE/2023/040** | Maintenance Request System · User Portal |
| 4 | **Nanthakishor - PE/2023/044** | Technician Management · AI Chatbot · UI Styling |

> *Names are placeholders — edit before publishing.*

---

## License

This project is for **academic purposes**. All rights reserved by the contributors.

---

<div align="center">

Built with ☕ Java · Semester Project · 2025/2026

</div>
