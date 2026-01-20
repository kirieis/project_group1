USE PharmacyDB;
GO

BULK INSERT Batch_Staging
FROM 'E:\Project-LAB-github\lab211-project-group1\data\legacy_batches.csv' --phần Project-LAB-github là trên máy của Nguyễn Văn An thôi nếu đường các máy khác thì tự chỉnh sửa lại cho đúng
WITH (
    FIRSTROW = 2,
    FIELDTERMINATOR = ',',
    ROWTERMINATOR = '\n',
    CODEPAGE = '65001'
);
