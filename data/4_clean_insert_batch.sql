USE PharmacyDB;
GO

INSERT INTO Medicine (medicine_id, name, unit)
SELECT DISTINCT
    medicine_id,
    medicine_name,
    'VIEN'
FROM Batch_Staging
WHERE medicine_id IS NOT NULL
  AND medicine_id <> '';
