CREATE TABLE Medicine (
    medicine_id VARCHAR(10) PRIMARY KEY,
    name NVARCHAR(100),
    unit VARCHAR(10),
    vien_per_vi INT,
    vi_per_hop INT,
    hop_per_thung INT
);

CREATE TABLE Batch (
    batch_id VARCHAR(20) PRIMARY KEY,
    medicine_id VARCHAR(10),
    expiry_date DATE,
    quantity_vien INT,
    branch_id VARCHAR(10),

    CONSTRAINT fk_batch_medicine
        FOREIGN KEY (medicine_id)
        REFERENCES Medicine(medicine_id)
);

CREATE TABLE Batch_Staging (
    batch_id VARCHAR(50),
    medicine_id VARCHAR(50),
    medicine_name NVARCHAR(100),
    expiry_date VARCHAR(50),   -- warning: STRING
    quantity_vien VARCHAR(50), -- warning: STRING
    branch_id VARCHAR(50)
);
