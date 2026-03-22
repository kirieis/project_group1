-- =============================================
-- SQL Script: Create Customer Table for PharmacyDB
-- Database: SQL Server
-- Run this in SQL Server Management Studio (SSMS)
-- =============================================

-- First, create the database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'PharmacyDB')
BEGIN
    CREATE DATABASE PharmacyDB;
END
GO

USE PharmacyDB;
GO

-- Drop foreign keys referencing Customer table first (to fix the error)
DECLARE @sql NVARCHAR(MAX) = N'';
SELECT @sql += N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id))
    + '.' + QUOTENAME(OBJECT_NAME(parent_object_id)) + 
    ' DROP CONSTRAINT ' + QUOTENAME(name) + ';'
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('Customer');
EXEC sp_executesql @sql;

-- Drop existing table
IF OBJECT_ID('Customer', 'U') IS NOT NULL DROP TABLE Customer;

-- Create Customer table
CREATE TABLE Customer (
    customer_id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(20),
    address NVARCHAR(255),
    email NVARCHAR(255),
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    role NVARCHAR(20) DEFAULT 'CUSTOMER' CHECK (role IN ('ADMIN', 'CUSTOMER'))
);
GO

-- Insert default admin account
-- Username: admin
-- Password: Admin123 (meets the 8+ chars, uppercase, lowercase, number requirements)
INSERT INTO Customer (full_name, phone, address, username, password, role)
VALUES (N'Quản trị viên', '0900000000', N'Hà Nội, Việt Nam', 'admin', 'Admin123', 'ADMIN');

-- Insert a sample customer
INSERT INTO Customer (full_name, phone, address, username, password, role)
VALUES (N'Nguyễn Văn Test', '0912345678', N'TP. Hồ Chí Minh', 'testuser', 'Test1234', 'CUSTOMER');

GO

-- Show all users
SELECT * FROM Customer;
GO

PRINT '✅ Customer table created successfully!';
PRINT '📧 Default Admin: username=admin, password=Admin123';
