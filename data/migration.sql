CREATE DATABASE PharmacyDB;
GO
USE PharmacyDB;
GO

CREATE TABLE Medicine (
    medicine_id VARCHAR(10) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    unit NVARCHAR(20) NOT NULL,
    price FLOAT CHECK (price > 0)
);

-- Import clean data
BULK INSERT Medicine
FROM 'E:\Project-LAB-github\lab211-project-group1\data\medicines_clean_9500.csv'--This path depend on each device so please check it properly
WITH (
    FIRSTROW = 2,
    FIELDTERMINATOR = ',',
    ROWTERMINATOR = '\n',
    CODEPAGE = '65001'
);

USE PharmacyDB;
GO

-- CUSTOMER
CREATE TABLE Customer (
    customer_id INT IDENTITY PRIMARY KEY,
    full_name NVARCHAR(100),
    phone VARCHAR(20),
    dob DATE,
    address NVARCHAR(255),
    loyalty_points INT DEFAULT 0
);

-- PHARMACIST
CREATE TABLE Pharmacist (
    pharmacist_id INT IDENTITY PRIMARY KEY,
    full_name NVARCHAR(100),
    license_number VARCHAR(50),
    branch_id INT,
    role VARCHAR(30)
);

-- MEDICINE (đã có – mở rộng cho đúng ERD)
ALTER TABLE Medicine
ADD
    active_ingredient NVARCHAR(100),
    dosage_form NVARCHAR(50),
    strength NVARCHAR(50),
    manufacturer NVARCHAR(100),
    requires_prescription BIT DEFAULT 0;

-- BATCH
CREATE TABLE Batch (
    batch_id INT IDENTITY PRIMARY KEY,
    medicine_id VARCHAR(10),
    batch_number VARCHAR(20),
    manufacture_date DATE,
    expiry_date DATE,
    quantity_in INT,
    quantity_available INT,
    import_price FLOAT,
    warehouse_location NVARCHAR(50),

    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id)
);

-- INVOICE
CREATE TABLE Invoice (
    invoice_id INT IDENTITY PRIMARY KEY,
    invoice_date DATETIME DEFAULT GETDATE(),
    pharmacist_id INT,
    customer_id INT,
    total_amount FLOAT,
    payment_method VARCHAR(30),

    FOREIGN KEY (pharmacist_id) REFERENCES Pharmacist(pharmacist_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

-- INVOICE_DETAIL
CREATE TABLE Invoice_Detail (
    invoice_detail_id INT IDENTITY PRIMARY KEY,
    invoice_id INT,
    batch_id INT,
    quantity INT,
    unit_price FLOAT,
    subtotal FLOAT,

    FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id),
    FOREIGN KEY (batch_id) REFERENCES Batch(batch_id)
);
