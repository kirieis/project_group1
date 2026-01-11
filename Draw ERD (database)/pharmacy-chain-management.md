# Pharmacy Chain Management – System Analysis

## 1. Overview
Hệ thống quản lý chuỗi nhà thuốc theo mô hình Long Châu / Pharmacity.

## 2. Entities
- Medicine
- Batch
- Store
- Inventory
- Customer
- Pharmacist
- Invoice

## 3. Business Rules
- FIFO theo batch
- Không bán thuốc hết hạn
- Cảnh báo thuốc sắp hết hạn

## 4. ER Diagram
```mermaid
erDiagram
    MEDICINE ||--o{ BATCH : has
    BATCH ||--o{ INVENTORY : stocked_in
    STORE ||--o{ INVENTORY : has
    INVOICE ||--o{ INVOICE_ITEM : contains
