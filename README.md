<div align="center">
  <img width="220" alt="SMRTS Logo" src="https://github.com/user-attachments/assets/9ef782d3-3939-4622-82e6-4c8773d380e3" />
  <h1>SMRTS — Smart Maintenance Request & Tracking System</h1>
  <p><strong>v3.0 &nbsp;·&nbsp; Java Swing Desktop Application</strong></p>

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.x-red?style=flat-square&logo=apachemaven)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![FlatLaf](https://img.shields.io/badge/FlatLaf-3.4-purple?style=flat-square)](https://www.formdev.com/flatlaf/)
[![License](https://img.shields.io/badge/License-Academic-green?style=flat-square)](#license)

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
  - [Admin Guide](#admin-guide)
  - [Technician Portal Guide](#technician-portal-guide)
  - [User Portal Guide](#user-portal-guide)
- [Role Permissions](#role-permissions)
- [Dependencies](#dependencies)
- [Troubleshooting](#troubleshooting)
- [Contributors](#contributors)
- [License](#license)

---

## Overview

SMRTS is a full-featured **facility maintenance request tracking system** built entirely in Java as a semester group project. It enables facilities staff, technicians, and end-users to submit, manage, and track maintenance requests through a polished dark-themed desktop GUI.

The system supports **four user roles** (Admin, Technician, Staff/Technician Portal, User), BCrypt-secured authentication, an offline AI-style chatbot with live database querying, and a reports dashboard — all hand-coded without UI builders.

---

## Features

| Feature | Description |
|---|---|
| 🔐 **Secure Authentication** | BCrypt-hashed passwords with role-based routing on login |
| 📋 **Maintenance Requests** | Full CRUD — create, view, edit, delete, filter by status and priority |
| 👷 **Technician Management** | Add, update, delete, and search technicians with live availability tracking |
| 🛠️ **Technician Portal** | Dedicated portal for technicians to self-assign and update request statuses |
| 👤 **User Management** | Admins can create, edit, and delete accounts and assign roles |
| 📊 **Reports Dashboard** | Summary stats by status, priority, and per-technician workload |
| 🤖 **AI Chatbot** | Offline keyword-based assistant with live DB context and persistent chat history |
| 🧾 **User Portal** | End-users can submit requests and track only their own submissions |
| 🖥️ **Modern Dark UI** | FlatLaf dark theme with fully custom-painted components and inline SVG icons |
| 🔎 **Search & Filter** | Real-time search across all tables; filter requests by status |
| ℹ️ **About System** | Live app version, runtime info, DB metadata, and server time |

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

<br/><br/>

### 🛠️ Technician Portal
<img width="1919" height="1015" alt="Technician Portal" src="https://github.com/user-attachments/assets/11b1cc74-7056-41bb-b2dc-fbc5018b8ad5" />

</div>

---

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
        │   │   └── Main.java                     ← Entry point; applies FlatLaf theme
        │   │
        │   ├── database/
        │   │   └── DBConnection.java              ← MySQL connection factory
        │   │
        │   ├── model/                             ← Plain Java data classes (POJOs)
        │   │   ├── User.java
        │   │   ├── Technician.java
        │   │   ├── MaintenanceRequest.java        ← Includes Priority & Status enums
        │   │   └── ChatMessage.java
        │   │
        │   ├── dao/                               ← Data Access Objects (SQL logic)
        │   │   ├── UserDAO.java
        │   │   ├── TechnicianDAO.java
        │   │   ├── MaintenanceRequestDAO.java
        │   │   └── ChatHistoryDAO.java
        │   │
        │   └── gui/                               ← All Swing UI classes (hand-coded)
        │       ├── LoginForm.java                 ← Auth screen; routes by role
        │       ├── AdminDashboard.java            ← Main shell (sidebar + CardLayout)
        │       ├── TechnicianForm.java            ← Technician CRUD panel
        │       ├── MaintenanceRequestForm.java    ← Request CRUD panel
        │       ├── UsersPanel.java                ← User account management panel
        │       ├── ReportsWindow.java             ← Statistics & reports panel
        │       ├── AIChatbotWindow.java           ← Offline chatbot panel
        │       ├── TechnicianPortalWindow.java    ← Dedicated portal for STAFF role
        │       ├── UserPortalWindow.java          ← End-user submission portal
        │       ├── RequestDetailsWindow.java      ← Modal: full request details
        │       └── AboutSystemWindow.java         ← Modal: version & DB info
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
    ├──── Role: ADMIN ─────────────►  [AdminDashboard]
    │                                       │
    │                                ┌──────┴────────┐
    │                       Sidebar  │  CardLayout   │
    │                       Nav      │               │
    │                                ├── Home (stats + recent requests)
    │                                ├── TechnicianForm
    │                                ├── MaintenanceRequestForm
    │                                ├── UsersPanel  (Admin only)
    │                                ├── ReportsWindow
    │                                └── AIChatbotWindow
    │
    ├──── Role: TECHNICIAN / STAFF ─►  [TechnicianPortalWindow]
    │                                       └── View, self-assign & update requests
    │
    └──── Role: USER ───────────────►  [UserPortalWindow]
                                            └── Submit & track own requests
```

---

## Database Schema

Run the following SQL in MySQL Workbench or any MySQL client to create the database:

```sql
CREATE DATABASE IF NOT EXISTS smrts_db;
USE smrts_db;

-- Users table
CREATE TABLE users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,                   -- BCrypt hash
    role       VARCHAR(50)  NOT NULL DEFAULT 'USER',    -- ADMIN | STAFF | USER
    full_name  VARCHAR(200)          DEFAULT ''
);

-- Technicians table
CREATE TABLE technicians (
    technician_id INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    contact_no    VARCHAR(30),
    email         VARCHAR(150),
    department    VARCHAR(100),
    status        VARCHAR(50)  DEFAULT 'Available'      -- Available | Busy
);

-- Maintenance Requests table
CREATE TABLE maintenance_requests (
    request_id              INT AUTO_INCREMENT PRIMARY KEY,
    title                   VARCHAR(200)  NOT NULL,
    description             TEXT,
    location                VARCHAR(200),
    priority                VARCHAR(20)   DEFAULT 'MEDIUM',   -- LOW | MEDIUM | HIGH
    status                  VARCHAR(20)   DEFAULT 'PENDING',  -- PENDING | IN_PROGRESS | COMPLETED
    assigned_technician_id  INT           DEFAULT NULL,
    submitted_by_username   VARCHAR(100)  DEFAULT NULL,
    created_at              DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_technician_id) REFERENCES technicians(technician_id)
        ON DELETE SET NULL
);

-- Chat History table (AI Chatbot)
CREATE TABLE chat_history (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    role       VARCHAR(20)   NOT NULL,   -- 'user' | 'assistant'
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

> **Note:** The BCrypt hash above corresponds to the password `admin123`. See the [Security Note](#security-note) section to generate a hash for a different password.

---

## Setup & Installation

### Prerequisites

Ensure the following are installed before proceeding:

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
3. Run the full SQL script from the [Database Schema](#database-schema) section.
4. Verify that the `smrts_db` database and all four tables were created successfully.

---

### Step 3 — Configure the Database Connection

Open the file:

```
src/main/java/database/DBConnection.java
```

Update the credentials to match your local MySQL setup:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/smrts_db?useSSL=false&serverTimezone=UTC";
private static final String USER     = "root";
private static final String PASSWORD = "your_mysql_password";  // ← Change this
```

---

### Step 4 — Build the Project

**Option A — NetBeans**

Open NetBeans → `File` → `Open Project` → select the `SMRTS_v3.0` folder → right-click the project → **Clean and Build**. Maven will download all dependencies automatically.

**Option B — Command Line (Maven)**

```bash
mvn clean install
```

---

### Step 5 — Run the Application

**Option A — NetBeans**

Right-click `Main.java` → **Run File**

**Option B — Maven (Command Line)**

```bash
mvn exec:java -Dexec.mainClass="main.Main"
```

**Option C — JAR directly**

```bash
java -jar target/SMRTS-2.0.jar
```

> If the JAR fails due to missing classpath dependencies, use the Maven exec approach above.

---

### Security Note

To generate a BCrypt hash for a new admin password, run the following utility class once:

```java
import org.mindrot.jbcrypt.BCrypt;

public class HashUtil {
    public static void main(String[] args) {
        String hashed = BCrypt.hashpw("yourNewPassword", BCrypt.gensalt());
        System.out.println(hashed);
        // Then update the DB:
        // UPDATE users SET password = '<hash>' WHERE username = 'admin';
    }
}
```

---

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |

> **Important:** Change the default admin password immediately after your first login. Additional accounts can be created from within the application by an Admin.

---

## User Guide

### Admin Guide

#### Logging In

1. Launch the application.
2. Enter your **username** and **password**, and select the **Admin** role chip.
3. Click **Login**. You will be routed to the **Admin Dashboard**.

---

#### Home Dashboard

The home view displays live stat cards at the top: Total Pending, In Progress, Completed, Technicians, and Total Users. A **Recent Requests** table shows the latest 10 requests with their status and assigned technician. A **System Information** panel shows the logged-in user, live server clock, database status, and total record count. Stats refresh automatically whenever you return to the Home view.

---

#### Managing Technicians

Navigate to **Technicians** in the sidebar.

| Action | How to do it |
|---|---|
| **Add** | Fill in Name, Contact, Email, Department → click **Add** |
| **Update** | Click a row to auto-fill the form → edit → click **Update** |
| **Delete** | Click a row → click **Delete** → confirm the dialog |
| **Search** | Type in the Search box to filter results in real time |
| **Clear** | Click **Clear** to reset all form fields |

Technician availability (Available / Busy) updates automatically when they are assigned or unassigned from requests.

---

#### Managing Maintenance Requests

Navigate to **Requests** in the sidebar.

| Action | How to do it |
|---|---|
| **Add** | Fill Title, Description, Location → choose Priority, Status, Technician → click **Add** |
| **Update** | Click a row → edit fields → click **Update** |
| **Delete** | Click a row → click **Delete** → confirm |
| **Filter** | Use the **Filter** dropdown (All / Pending / In Progress / Completed) |
| **Search** | Search by title, location, or description |
| **View Details** | Double-click any row to open the full Request Details dialog |

**Priority levels:** 🔴 HIGH &nbsp;·&nbsp; 🟡 MEDIUM &nbsp;·&nbsp; 🟢 LOW

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

**Available Roles:** `ADMIN` &nbsp;·&nbsp; `TECHNICIAN` (STAFF) &nbsp;·&nbsp; `USER`

Passwords are automatically BCrypt-hashed before being stored. Existing hashes are never displayed in the form.

---

#### Reports

Navigate to **Reports** in the sidebar.

The dashboard displays summary counts (Total, Pending, In Progress, Completed), a priority breakdown (High / Medium / Low), a per-technician workload table showing each technician's request counts by status, and a table of the 10 most recently completed requests. Click **🔄 Refresh** to reload all data.

---

#### AI Chatbot

Navigate to **AI Assistant** in the sidebar.

Type questions in natural language about the system. The chatbot queries the **live database** to answer questions such as:

- *"How many pending requests are there?"*
- *"List available technicians"*
- *"Show high priority requests"*
- *"Who is assigned to the most requests?"*

Chat history is **saved per user** and persists across sessions. Click **Clear Chat** to wipe your history.

> The chatbot is fully **offline** — it uses keyword matching and direct database queries, not an external API.

---

#### About System

Click the **⚙ About** button at the bottom of the sidebar to view the application version, build info, logged-in user, live database connection details (host, DB name, MySQL version), and current server date/time.

---

### Technician Portal Guide

Users with the **TECHNICIAN / STAFF** role are routed to a dedicated **Technician Portal** after login.

#### Viewing Requests

The portal lists all open maintenance requests visible to the technician. Each row shows the request ID, title, priority, current status, and assigned technician.

#### Self-Assigning a Request

Click any unassigned request row and use the **Assign to Me** action to claim the request. The system will update the `assigned_technician_id` to the technician's own record and set their availability status to **Busy**.

#### Updating Request Status

Once assigned, technicians can update a request's status through the workflow:

`PENDING` → `IN_PROGRESS` → `COMPLETED`

#### Viewing Request Details

Double-click any row to open the full **Request Details** dialog showing all fields including description, location, priority, timestamps, and assignment history.

---

### User Portal Guide

Users with the **USER** role are routed to a simplified **User Portal** after login.

#### Submitting a New Request

Fill in the **Title**, **Description**, and **Location** fields, select a **Priority** (Low / Medium / High), then click **Submit Request**. A confirmation message will appear on success.

#### Tracking Your Requests

The **My Requests** table shows only requests you have submitted, with columns for ID, Title, Status, Priority, Assigned Technician, and Date Submitted. Click **🔄 Refresh** to reload the latest status updates. Double-click any row to view full request details.

#### Filtering Your Requests

Use the **Filter** dropdown to view All, Pending, In Progress, or Completed requests.

---

## Role Permissions

| Feature | ADMIN | TECHNICIAN | USER |
|---|:---:|:---:|:---:|
| View Dashboard & Stats | ✅ | ❌ | ❌ |
| Manage Maintenance Requests (CRUD) | ✅ | ❌ | ❌ |
| View & Self-Assign Requests (Portal) | ❌ | ✅ | ❌ |
| Update Request Status (Portal) | ❌ | ✅ | ❌ |
| Submit New Request (User Portal) | ❌ | ❌ | ✅ |
| View Own Requests | ❌ | ❌ | ✅ |
| Manage Technicians (CRUD) | ✅ | ❌ | ❌ |
| Manage Users (CRUD) | ✅ | ❌ | ❌ |
| View Reports | ✅ | ❌ | ❌ |
| AI Chatbot | ✅ | ❌ | ❌ |
| About System | ✅ | ❌ | ❌ |

---

## Dependencies

Declared in `pom.xml` — Maven downloads all of these automatically on build:

| Library | Version | Purpose |
|---|---|---|
| `mysql-connector-j` | 9.7.0 | MySQL JDBC driver |
| `flatlaf` | 3.4 | Modern dark UI Look and Feel |
| `flatlaf-extras` | 3.4 | Inline SVG icon rendering support |
| `jbcrypt` | 0.4 | BCrypt password hashing |

**Java version target:** 17 (configured via `maven.compiler.release`)

---

## Troubleshooting

**"Cannot connect to database"**
Verify MySQL is running. Confirm the credentials in `DBConnection.java` match your MySQL setup and that the `smrts_db` database exists (run the setup SQL).

**"BCrypt dependency not found"**
Run `mvn clean install` to force Maven to re-download all dependencies from the central repository.

**UI appears plain/grey with no dark theme**
Ensure `flatlaf-3.4.jar` was downloaded successfully. Run `mvn dependency:resolve` to check.

**JAR fails to launch**
Use `mvn exec:java` instead, which automatically constructs the full classpath. If packaging a fat JAR is needed, configure `maven-assembly-plugin` to bundle dependencies.

**`submitted_by_username` column warning in logs**
Run this migration SQL once on your database:

```sql
ALTER TABLE maintenance_requests
  ADD COLUMN submitted_by_username VARCHAR(100) DEFAULT NULL;
```

**Technician login routes to Admin Dashboard instead of Technician Portal**
Ensure the user's `role` column in the database is set to `STAFF` (not `TECHNICIAN`). The login form maps the "Technician" chip to the `STAFF` DB role.

---

## Contributors

This project was developed as a group semester assignment by:

| # | Student ID | Name | Role / Contribution |
|---|---|---|---|
| 1 | PE/2023/002 | **Elankeeran** | Project Lead · Admin Dashboard · Login Form |
| 2 | PE/2023/013 | **Kithusan** | Database Design · DAO Layer · Reports Module |
| 3 | PE/2023/040 | **Danushan** | Maintenance Request System · User Portal |
| 4 | PE/2023/044 | **Nanthakishor** | Technician Management · AI Chatbot · Technician Portal · UI Styling |

---

## License

This project was developed for **academic purposes**. All rights reserved by the contributors.

---

<div align="center">

Built with ☕ Java &nbsp;·&nbsp; Semester Project &nbsp;·&nbsp; 2025 / 2026

</div>
