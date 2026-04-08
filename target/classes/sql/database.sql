CREATE DATABASE IF NOT EXISTS gearrentpro;
USE gearrentpro;

-- System Configuration Table for configurable business rules
CREATE TABLE IF NOT EXISTS system_config (
    config_key VARCHAR(50) PRIMARY KEY,
    config_value VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- 1. Branches
CREATE TABLE IF NOT EXISTS branch (
    branch_id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    contact VARCHAR(20)
);

-- 2. Users (Admin, Branch Manager, Staff)
CREATE TABLE IF NOT EXISTS user (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'BRANCH_MANAGER', 'STAFF') NOT NULL,
    branch_id VARCHAR(10),
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id) ON DELETE SET NULL
);

-- 3. Categories with Pricing Rules
CREATE TABLE IF NOT EXISTS category (
    category_id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    base_price_factor DOUBLE DEFAULT 1.0,
    weekend_multiplier DOUBLE DEFAULT 1.2,
    late_fee_per_day DOUBLE DEFAULT 0.0,
    is_active TINYINT DEFAULT 1
);

-- 4. Equipment - Full specification as per coursework
CREATE TABLE IF NOT EXISTS equipment (
    equipment_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    brand VARCHAR(50),
    model VARCHAR(50),
    purchase_year INT,
    base_daily_price DECIMAL(10, 2) NOT NULL,
    security_deposit DECIMAL(10, 2) NOT NULL,
    status ENUM('AVAILABLE', 'RESERVED', 'RENTED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    branch_id VARCHAR(10) NOT NULL,
    category_id VARCHAR(10) NOT NULL,
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id),
    FOREIGN KEY (category_id) REFERENCES category(category_id)
);

-- 5. Customers with Membership Levels
CREATE TABLE IF NOT EXISTS customer (
    customer_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    nic_passport VARCHAR(20) UNIQUE NOT NULL,
    contact VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255),
    membership_level ENUM('REGULAR', 'SILVER', 'GOLD') DEFAULT 'REGULAR'
);

-- Configuration for Membership Discounts
CREATE TABLE IF NOT EXISTS membership_config (
    level_name VARCHAR(20) PRIMARY KEY,
    discount_percentage DOUBLE DEFAULT 0.0
);

-- 6. Reservations - blocks equipment for specified period
CREATE TABLE IF NOT EXISTS reservation (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL,
    equipment_id VARCHAR(20) NOT NULL,
    branch_id VARCHAR(10) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CONVERTED', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id),
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
);

-- 7. Rentals with full pricing breakdown
CREATE TABLE IF NOT EXISTS rental (
    rental_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id VARCHAR(20) NOT NULL,
    customer_id VARCHAR(20) NOT NULL,
    branch_id VARCHAR(10) NOT NULL,
    reservation_id INT NULL,
    
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    actual_return_date DATE,
    
    -- Pricing breakdown
    base_rental_cost DECIMAL(10, 2) NOT NULL,
    security_deposit_held DECIMAL(10, 2) NOT NULL,
    weekend_charges DECIMAL(10, 2) DEFAULT 0.0,
    
    -- Discounts applied
    long_rental_discount DECIMAL(10, 2) DEFAULT 0.0,
    membership_discount DECIMAL(10, 2) DEFAULT 0.0,
    
    -- Final amounts
    total_before_discount DECIMAL(10, 2) NOT NULL,
    final_payable_amount DECIMAL(10, 2) NOT NULL,
    
    -- Status
    payment_status ENUM('PAID', 'PARTIALLY_PAID', 'UNPAID') DEFAULT 'UNPAID',
    rental_status ENUM('ACTIVE', 'RETURNED', 'OVERDUE', 'CANCELLED') DEFAULT 'ACTIVE',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id),
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
);

-- 8. Returns / Damages log
CREATE TABLE IF NOT EXISTS rental_return_info (
    return_id INT AUTO_INCREMENT PRIMARY KEY,
    rental_id INT NOT NULL,
    return_date DATE NOT NULL,
    
    late_fee DECIMAL(10, 2) DEFAULT 0.0,
    damage_charge DECIMAL(10, 2) DEFAULT 0.0,
    damage_description TEXT,
    
    deposit_refunded DECIMAL(10, 2) DEFAULT 0.0,
    total_charges DECIMAL(10, 2) DEFAULT 0.0,
    final_settlement_status VARCHAR(50),
    
    FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
);

-- =====================================================
-- INITIAL SEED DATA
-- =====================================================

-- System Configuration - Business Rules
INSERT INTO system_config VALUES 
('MAX_DEPOSIT_LIMIT', '500000', 'Maximum total security deposit per customer in LKR'),
('MAX_RENTAL_DAYS', '30', 'Maximum rental duration in days'),
('LONG_RENTAL_DAYS', '7', 'Minimum days to qualify for long-rental discount'),
('LONG_RENTAL_DISCOUNT_PERCENT', '10', 'Discount percentage for long-term rentals');

-- Membership Config
INSERT INTO membership_config (level_name, discount_percentage) VALUES 
('REGULAR', 0.0),
('SILVER', 5.0),
('GOLD', 10.0);

-- Branches
INSERT INTO branch VALUES ('B001', 'Panadura HQ', '45 Station Road, Panadura', '0382234567');
INSERT INTO branch VALUES ('B002', 'Galle Branch', '89 Fort Road, Galle', '0912345678');
INSERT INTO branch VALUES ('B003', 'Colombo Branch', '123 Main Street, Colombo 3', '0112345678');

-- Users
INSERT INTO user (username, password, role, branch_id) VALUES 
('admin', '1234', 'ADMIN', 'B001'),
('manager_panadura', '1234', 'BRANCH_MANAGER', 'B001'),
('staff_panadura', '1234', 'STAFF', 'B001'),
('manager_galle', '1234', 'BRANCH_MANAGER', 'B002'),
('staff_galle', '1234', 'STAFF', 'B002'),
('manager_colombo', '1234', 'BRANCH_MANAGER', 'B003');

-- Categories with Pricing Rules
INSERT INTO category VALUES 
('C001', 'Camera', 1.0, 1.0, 500.0, 1),
('C002', 'Drone', 2.0, 1.2, 1000.0, 1),
('C003', 'Lens', 0.8, 1.0, 300.0, 1),
('C004', 'Lighting', 0.5, 1.0, 200.0, 1),
('C005', 'Audio', 0.6, 1.0, 250.0, 1);

-- Equipment Inventory
INSERT INTO equipment VALUES 
('E001', 'Sony A7III', 'Sony', 'A7III', 2022, 5000.00, 20000.00, 'AVAILABLE', 'B001', 'C001'),
('E002', 'DJI Mavic 3', 'DJI', 'Mavic 3', 2023, 8000.00, 50000.00, 'AVAILABLE', 'B001', 'C002'),
('E003', 'Canon R5', 'Canon', 'R5', 2023, 6000.00, 25000.00, 'AVAILABLE', 'B001', 'C001'),
('E004', 'Sony 24-70mm f/2.8', 'Sony', 'GM 24-70', 2021, 2500.00, 15000.00, 'AVAILABLE', 'B001', 'C003'),
('E005', 'Godox SL60W', 'Godox', 'SL60W', 2022, 1500.00, 8000.00, 'AVAILABLE', 'B001', 'C004'),
('E006', 'Rode Wireless GO II', 'Rode', 'Wireless GO II', 2023, 2000.00, 10000.00, 'AVAILABLE', 'B001', 'C005'),
('E007', 'Nikon Z6 II', 'Nikon', 'Z6 II', 2022, 4500.00, 18000.00, 'AVAILABLE', 'B002', 'C001'),
('E008', 'DJI Mini 3 Pro', 'DJI', 'Mini 3 Pro', 2023, 5000.00, 35000.00, 'AVAILABLE', 'B002', 'C002'),
('E009', 'Canon RF 50mm f/1.2', 'Canon', 'RF 50mm', 2022, 3000.00, 20000.00, 'AVAILABLE', 'B002', 'C003'),
('E010', 'Aputure 120D II', 'Aputure', '120D II', 2021, 2500.00, 12000.00, 'AVAILABLE', 'B002', 'C004'),
('E011', 'Sony A7S III', 'Sony', 'A7S III', 2023, 7000.00, 30000.00, 'AVAILABLE', 'B003', 'C001'),
('E012', 'DJI Inspire 2', 'DJI', 'Inspire 2', 2022, 15000.00, 100000.00, 'AVAILABLE', 'B003', 'C002');

-- Sample Customers
INSERT INTO customer VALUES 
('CUST001', 'Nimal Perera', '123456789V', '0771234567', 'nimal@email.com', '45 Temple Road, Colombo', 'GOLD'),
('CUST002', 'Kamal Silva', '987654321V', '0719876543', 'kamal@email.com', '78 Beach Road, Galle', 'REGULAR'),
('CUST003', 'Samantha Fernando', '456789123V', '0764567890', 'samantha@email.com', '12 Lake View, Kandy', 'SILVER'),
('CUST004', 'Dinesh Jayawardena', '789123456V', '0757891234', 'dinesh@email.com', '33 Main Street, Panadura', 'REGULAR'),
('CUST005', 'Rashmi Wickramasinghe', '321654987V', '0783216549', 'rashmi@email.com', '67 Hill Street, Nugegoda', 'GOLD');
