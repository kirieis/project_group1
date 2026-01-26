------------------------
Medicine
------------------------
- medicineId : int
- name : String
- price : double
- unit : String
------------------------
+ Medicine()
+ getMedicineId() : int
+ getName() : String
+ getPrice() : double
+ getUnit() : String
-----------------------

------------------------
Batch
------------------------
- batchId : int
- medicineId : int
- quantity : int
- expiryDate : LocalDate
- importDate : LocalDate
------------------------
+ Batch()
+ isExpired() : boolean
+ getQuantity() : int
-----------------------

------------------------
Customer
------------------------
- customerId : int
- name : String
- phone : String
------------------------
+ Customer()
+ getCustomerId() : int
+ getName() : String
+ getPhone() : String
-----------------------


------------------------
Pharmacist
------------------------
- pharmacistId : int
- name : String
- username : String
- password : String
------------------------
+ Pharmacist()
+ checkLogin() : boolean
------------------------

----------------------------
Invoice
----------------------------
- invoiceId : int
- customerId : int
- pharmacistId : int
- totalAmount : double
- createdAt : LocalDateTime
----------------------------
+ Invoice()
+ calculateTotal() : double
----------------------------


------------------------
InvoiceDetail
------------------------
- invoiceDetailId : int
- invoiceId : int
- medicineId : int
- quantity : int
- price : double
------------------------
+ InvoiceDetail()
+ getSubtotal() : double
------------------------


