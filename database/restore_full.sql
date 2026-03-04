/* 
   =============================================
   PHARMACY SYSTEM - FULL DATABASE RESTORE SCRIPT
   =============================================
   Database: PharmacyDB
   Tool: SQL Server Management Studio (SSMS)
   
   This script recreates ALL tables according to the 
   latest requirements (including FIFO/Batch support).
*/

-- 1. Create Database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'PharmacyDB')
BEGIN
    CREATE DATABASE PharmacyDB;
END
GO

USE PharmacyDB;
GO

-- 2. Drop Tables with Foreign Key constraints in order
IF OBJECT_ID('Invoice_Detail', 'U') IS NOT NULL DROP TABLE Invoice_Detail;
IF OBJECT_ID('Invoice', 'U') IS NOT NULL DROP TABLE Invoice;
IF OBJECT_ID('Batch', 'U') IS NOT NULL DROP TABLE Batch;
IF OBJECT_ID('Pharmacist', 'U') IS NOT NULL DROP TABLE Pharmacist;
IF OBJECT_ID('Customer', 'U') IS NOT NULL DROP TABLE Customer;
IF OBJECT_ID('Medicine', 'U') IS NOT NULL DROP TABLE Medicine;
GO

-- 3. Create Tables
CREATE TABLE Medicine (
    medicine_id VARCHAR(20) PRIMARY KEY,
    name NVARCHAR(255),
    batch VARCHAR(100),
    ingredient NVARCHAR(255),
    dosage_form NVARCHAR(100),
    strength NVARCHAR(50),
    unit NVARCHAR(50),
    manufacturer NVARCHAR(255),
    expiry DATE,
    quantity INT,
    price INT
);

CREATE TABLE Batch (
    batch_id INT IDENTITY PRIMARY KEY,
    medicine_id VARCHAR(20) NOT NULL,
    batch_number VARCHAR(100),
    manufacture_date DATE,
    expiry_date DATE,
    quantity_in INT,
    quantity_available INT,
    import_price DECIMAL(18,2),
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id)
);

CREATE TABLE Customer (
    customer_id INT IDENTITY PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(20),
    address NVARCHAR(255),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'CUSTOMER'
);

CREATE TABLE Pharmacist (
    pharmacist_id INT IDENTITY PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    license_number VARCHAR(50),
    role VARCHAR(30)
);

CREATE TABLE Invoice (
    invoice_id INT IDENTITY PRIMARY KEY,
    invoice_date DATETIME DEFAULT GETDATE(),
    customer_id INT NULL,
    total_amount DECIMAL(18,2),
    payment_method NVARCHAR(50),
    status NVARCHAR(20) DEFAULT 'PENDING',
    payment_proof NVARCHAR(500),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

CREATE TABLE Invoice_Detail (
    invoice_id INT NOT NULL,
    medicine_id VARCHAR(20),
    medicine_name NVARCHAR(255),
    quantity INT,
    unit_price DECIMAL(18,2),
    import_price DECIMAL(18,2), -- Tracking profit
    FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id)
);
GO

-- 4. Import Data from CSV
-- Adjust paths if necessary
-- Medicine Data
BULK INSERT Medicine 
FROM 'c:\Users\nguye\Documents\GitHub\lab211-project-group1\data\medicines_clean.csv'
WITH (
    FIRSTROW = 2,
    FIELDTERMINATOR = ',',
    ROWTERMINATOR = '0x0a',
    CODEPAGE = '65001'
);

-- Initial Batch data from the same CSV (Matching medicine_id)
INSERT INTO Batch (medicine_id, batch_number, expiry_date, quantity_in, quantity_available, import_price)
SELECT medicine_id, batch, expiry, quantity, quantity, price * 0.7 
FROM Medicine m
-- This is a temporary join/sync logic to seed the Batch table
-- in a real app, batches are added via Inventory Management.

-- 5. Seed Users
INSERT INTO Customer (full_name, phone, address, username, password, role)
VALUES (N'Quản trị viên', '0900000000', N'Hệ thống', 'admin', 'Admin123', 'ADMIN');

INSERT INTO Pharmacist (full_name, license_number, role)
VALUES (N'Dược sĩ A', 'LC-123456', 'PHARMACIST');

PRINT '✅ Database restored and populated successfully!';
