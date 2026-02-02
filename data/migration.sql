/* =========================
   CREATE DATABASE
   ========================= */
CREATE DATABASE PharmacyDB;
GO
USE PharmacyDB;
GO

/* =========================
   DROP TABLES (SAFE RE-RUN)
   ========================= */
DROP TABLE IF EXISTS Invoice_Detail;
DROP TABLE IF EXISTS Invoice;
DROP TABLE IF EXISTS Pharmacist;
DROP TABLE IF EXISTS Customer;
DROP TABLE IF EXISTS Medicine;
GO

/* =========================
   MEDICINE
   (MATCH CSV STRUCTURE)
   ========================= */
CREATE TABLE Medicine (
    medicine_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    batch VARCHAR(250),
    ingredient VARCHAR(100),
    dosage_form VARCHAR(50),
    strength VARCHAR(20),
    unit VARCHAR(10),
    manufacturer VARCHAR(100),
    expiry DATE,
    quantity INT,
    price INT,
);
GO

/* =========================
   IMPORT MEDICINE DATA
   ========================= */
--Nguyen_Van_An path:E:\Project-LAB-github\lab211-project-group1\data\medicines_clean.csv--
--Nguyen_Van_An path:E:\Download setup\apache-tomcat-10.1.50\webapps\target\data\medicines_clean.csv--
--Tran Quoc Thinh path:C:\Users\PC\Documents\GitHub\project_group1\data\medicines_clean.csv--
--Nguyen Tri Thien path: C:\Users\nguye\Documents\GitHub\lab211-project-group1\data\medicines_clean.csv--
--Hang Vo Minh Nhat path: C:\Users\nhatg\Documents\GitHub\lab211-project-group1\data\medicines_clean.csv--
BULK INSERT Medicine 
FROM 'E:\Project-LAB-github\lab211-project-group1\data\medicines_clean.csv'
WITH (
    FIRSTROW = 2,
    FIELDTERMINATOR = ',',
    ROWTERMINATOR = '0x0a',
    CODEPAGE = '65001'
);
GO

/* =========================
   CUSTOMER
   (MATCH JAVA MODEL: Customer.java)
   ========================= */
CREATE TABLE Customer (
    customer_id INT IDENTITY PRIMARY KEY,
    full_name VARCHAR(100),
    phone INT,
    address VARCHAR(255),
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(20) DEFAULT 'CUSTOMER'
);
GO

/* =========================
   IMPORT CUSTOMER DATA
   ========================= */
--Nguyen_Van_An path:E:\Project-LAB-github\lab211-project-group1\data\customers.csv--
--Nguyen_Van_An path:E:\Download setup\apache-tomcat-10.1.50\webapps\target\data\customers.csv--
--Tran Quoc Thinh path:C:\Users\PC\Documents\GitHub\project_group1\data\customers.csv--
--Nguyen Tri Thien path: C:\Users\nguye\Documents\GitHub\lab211-project-group1\data\customers.csv--
--Hang Vo Minh Nhat path: C:\Users\nhatg\Documents\GitHub\lab211-project-group1\data\customers.csv--
BULK INSERT Customer 
FROM 'E:\Project-LAB-github\lab211-project-group1\data\customers.csv'
WITH (
    FIRSTROW = 2,
    FIELDTERMINATOR = ',',
    ROWTERMINATOR = '0x0a',
    CODEPAGE = '65001'
);
GO

-- Seed initial users (optional, if not in CSV)
-- Admin: admin / admin
-- User: user / 123
--INSERT INTO Customer (full_name, phone, address, username, password, role) 
--VALUES (N'Quản Trị Viên', '0909000111', 'HCMC', 'admin', 'admin', 'ADMIN');

--INSERT INTO Customer (full_name, phone, address, username, password, role) 
--VALUES (N'Nguyễn Văn A', '0912345678', 'Hanoi', 'user', '123', 'CUSTOMER');
--GO

/* =========================
   PHARMACIST
   (MATCH JAVA MODEL: Pharmacist.java)
   ========================= */
CREATE TABLE Pharmacist (
    pharmacist_id INT IDENTITY PRIMARY KEY,
    full_name VARCHAR(100),
    license_number VARCHAR(50),
    role VARCHAR(30)
);
GO

/* =========================
   IMPORT PHARMACIST DATA
   ========================= */
--Nguyen_Van_An path:E:\Project-LAB-github\lab211-project-group1\data\pharmacists.csv--
--Nguyen_Van_An path:E:\Download setup\apache-tomcat-10.1.50\webapps\target\data\pharmacists.csv--
--Tran Quoc Thinh path:C:\Users\PC\Documents\GitHub\project_group1\data\pharmacists.csv--
--Nguyen Tri Thien path: C:\Users\nguye\Documents\GitHub\lab211-project-group1\data\pharmacists.csv--
--Hang Vo Minh Nhat path: C:\Users\nhatg\Documents\GitHub\lab211-project-group1\data\pharmacists.csv--
BULK INSERT Pharmacist 
FROM 'E:\Project-LAB-github\lab211-project-group1\data\pharmacists.csv'
WITH (
    FIRSTROW = 2,
    FIELDTERMINATOR = ',',
    ROWTERMINATOR = '0x0a',
    CODEPAGE = '65001'
);
GO

/* =========================
   INVOICE
   ========================= */
CREATE TABLE Invoice (
    invoice_id INT IDENTITY PRIMARY KEY,
    invoice_date DATETIME DEFAULT GETDATE(),
    pharmacist_id INT,
    customer_id INT,
    total_amount DECIMAL(12,2),
    payment_method NVARCHAR(50),

    FOREIGN KEY (pharmacist_id) REFERENCES Pharmacist(pharmacist_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);
GO

/* =========================
   INVOICE DETAIL
   (LINK DIRECTLY TO MEDICINE)
   ========================= */
CREATE TABLE Invoice_Detail (
    invoice_id INT NOT NULL,
    medicine_id VARCHAR(20) NOT NULL,
    medicine_name NVARCHAR(100) NOT NULL,
    quantity INT CHECK (quantity > 0),
    unit_price DECIMAL(12,2) CHECK (unit_price >= 0),

    FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id),
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id)
);
GO