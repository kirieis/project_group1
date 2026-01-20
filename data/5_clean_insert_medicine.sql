USE PharmacyDB;
GO

INSERT INTO Batch (batch_id, medicine_id, expiry_date, quantity_vien, branch_id)
SELECT
    s.batch_id,
    s.medicine_id,
    TRY_CONVERT(DATE, s.expiry_date),
    TRY_CONVERT(INT, s.quantity_vien),
    s.branch_id
FROM Batch_Staging s
WHERE
    s.batch_id IS NOT NULL AND s.batch_id <> ''
    AND s.medicine_id IS NOT NULL AND s.medicine_id <> ''
    AND TRY_CONVERT(DATE, s.expiry_date) IS NOT NULL
    AND TRY_CONVERT(INT, s.quantity_vien) > 0
    AND NOT EXISTS (
        SELECT 1
        FROM Batch b
        WHERE b.batch_id = s.batch_id
    );
