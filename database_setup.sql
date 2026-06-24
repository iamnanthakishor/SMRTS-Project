-- ============================================================
--  DANGER: This will DELETE ALL DATA
--  Use only for development/testing
-- ============================================================

-- Recreate fresh database
CREATE DATABASE smrts_db;
USE smrts_db;

-- ── Users Table ─────────────────────────────────────────────
CREATE TABLE users (
    user_id   INT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(100) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    role      ENUM('ADMIN','STAFF') DEFAULT 'STAFF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, password, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');

INSERT INTO users (username, password, role) VALUES
('nanthu', '1112', 'ADMIN');
-- ── Technicians Table ───────────────────────────────────────
CREATE TABLE technicians (
    technician_id INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    contact_no    VARCHAR(50),
    email         VARCHAR(200),
    department    VARCHAR(100),
    status        ENUM('Available', 'Busy') DEFAULT 'Available',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── Maintenance Requests Table ──────────────────────────────
CREATE TABLE maintenance_requests (
    request_id             INT AUTO_INCREMENT PRIMARY KEY,
    title                  VARCHAR(200) NOT NULL,
    description            TEXT,
    location               VARCHAR(200),
    priority               ENUM('LOW','MEDIUM','HIGH') DEFAULT 'MEDIUM',
    status                 ENUM('PENDING','IN_PROGRESS','COMPLETED') DEFAULT 'PENDING',
    assigned_technician_id INT,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completion_date        DATE,
    estimated_hours        INT DEFAULT 1,
    FOREIGN KEY (assigned_technician_id) 
        REFERENCES technicians(technician_id) 
        ON DELETE SET NULL
);

-- Sample Data
INSERT INTO technicians (name, contact_no, email, department, status) VALUES
('Elankeeran', '0712345678', 'elankee@icloud.com', 'Electrical', 'Available'),
('Danushan', '0723456789', 'danushan@icloud.com.com', 'Plumbing', 'Available');

INSERT INTO maintenance_requests (title, description, location, priority, status) VALUES
('Air Conditioner Repair', 'AC not cooling', 'Room 201', 'HIGH', 'PENDING');

-- Verification
SELECT 'Setup Complete!' AS Message;
SELECT * FROM users;
SELECT * FROM technicians;
SELECT * FROM maintenance_requests;

-- Add more realistic technicians
INSERT INTO technicians (name, contact_no, email, department, status) VALUES
('Kasun Perera',          '0712345678', 'kasun.perera@smrts.lk',     'Electrical',   'Available'),
('Nimali Silva',          '0723456789', 'nimali.silva@smrts.lk',     'Plumbing',     'Available'),
('Roshan Fernando',       '0734567890', 'roshan.fernando@smrts.lk',  'Civil',        'Busy'),
('Saman Jayasena',        '0745678901', 'saman.jayasena@smrts.lk',   'HVAC',         'Available'),
('Dilini Weerasinghe',    '0756789012', 'dilini.w@smrts.lk',         'IT Support',   'Available'),
('Tharindu Wickramasinghe','0767890123', 'tharindu.w@smrts.lk',      'Mechanical',   'Available'),
('Anusha Perera',         '0778901234', 'anusha.perera@smrts.lk',    'Carpentry',    'Busy'),
('Malith Rajapaksha',     '0789012345', 'malith.raj@smrts.lk',       'Painting',     'Available'),
('Shalini Gunawardena',   '0790123456', 'shalini.g@smrts.lk',        'Welding',      'Available'),
('Chamara Dissanayake',   '0711122334', 'chamara.d@smrts.lk',        'General Maintenance', 'Available')
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO technicians (name, contact_no, email, department, status) VALUES
('Kavindu Rathnayake',    '0715566778', 'kavindu.r@smrts.lk',      'Electrical',     'Available'),
('Sanduni Fernando',      '0726677889', 'sanduni.f@smrts.lk',      'Plumbing',       'Busy'),
('Lakshan Mendis',        '0737788990', 'lakshan.m@smrts.lk',      'Civil',          'Available'),
('Thilini Jayawardena',   '0748899001', 'thilini.j@smrts.lk',      'HVAC',           'Available'),
('Nipun Wickramaratne',   '0759900112', 'nipun.w@smrts.lk',        'IT Support',     'Available'),
('Ishara Perera',         '0760011223', 'ishara.p@smrts.lk',       'Mechanical',     'Busy'),
('Dulshan Alwis',         '0771122334', 'dulshan.a@smrts.lk',      'Carpentry',      'Available'),
('Harshani Silva',        '0782233445', 'harshani.s@smrts.lk',     'Painting',       'Available'),
('Prabath Gunasekara',    '0793344556', 'prabath.g@smrts.lk',      'Welding',        'Available'),
('Madhavi Ranasinghe',    '0714455667', 'madhavi.r@smrts.lk',      'General Maintenance', 'Available')
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO maintenance_requests 
    (title, description, location, priority, status, assigned_technician_id, estimated_hours) 
VALUES
('UPS Battery Failure', 
 'Main UPS in data center showing low battery warning.', 
 'Data Center - Basement', 'HIGH', 'PENDING', 6, 5),

('Flooding in Basement', 
 'Heavy water accumulation in basement storage area after rain.', 
 'Basement Storage', 'HIGH', 'IN_PROGRESS', 2, 6),

('Projector Not Working in Hall', 
 'Projector in main conference hall not turning on.', 
 'Conference Hall B', 'MEDIUM', 'PENDING', 10, 2),

('Door Lock Malfunction', 
 'Main entrance door lock is stuck. Employees unable to enter easily.', 
 'Main Entrance', 'MEDIUM', 'PENDING', 8, 3),

('Network Cable Damage', 
 'Several network cables damaged near the server rack.', 
 'Server Room', 'HIGH', 'IN_PROGRESS', 5, 4),

('Exhaust Fan Not Working', 
 'Kitchen exhaust fan making loud noise and not venting properly.', 
 'Kitchen Area', 'LOW', 'COMPLETED', 4, 2),

('Lighting Issue in Parking', 
 'Multiple lights in underground parking not working. Safety concern.', 
 'Underground Parking', 'MEDIUM', 'PENDING', 1, 5),

('Water Heater Leak', 
 'Water heater in staff washroom leaking from bottom.', 
 'Staff Washroom - 2nd Floor', 'LOW', 'PENDING', 2, 3),

('Backup Generator Not Starting', 
 'Emergency generator failed to start during last power outage.', 
 'Generator Room', 'HIGH', 'PENDING', 7, 8),

('Pest Control in Store Room', 
 'Rodents observed in the ground floor store room.', 
 'Store Room', 'MEDIUM', 'COMPLETED', 9, 4)
ON DUPLICATE KEY UPDATE title = title;


ALTER TABLE maintenance_requests
  ADD COLUMN submitted_by_username VARCHAR(100) DEFAULT NULL;

-- Also add USER role to your users table if it has a CHECK constraint:
-- ALTER TABLE users MODIFY role ENUM('ADMIN','STAFF','USER');