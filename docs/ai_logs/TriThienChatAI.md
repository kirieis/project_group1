GIùm giúp tôi flowchart của 1 trang web giống FPT Long Châu để tham khảo 
0) Tổng quan hệ thống (User → Web/App → Services → DB)
flowchart LR
  U[Người dùng] -->|Web/Mobile| FE[Frontend]
  FE -->|API| BE[Backend API Gateway]
  BE --> AUTH[Auth Service]
  BE --> CATALOG[Catalog Service]
  BE --> SEARCH[Search Service]
  BE --> PRICE[Pricing/Promotion Service]
  BE --> INV[Inventory Service]
  BE --> CART[Cart Service]
  BE --> ORDER[Order Service]
  BE --> RX[Prescription Service]
  BE --> PAY[Payment Service]
  BE --> SHIP[Shipping/Fulfillment Service]
  BE --> NOTI[Notification Service]

  AUTH --> DB[(Relational DB)]
  CATALOG --> DB
  PRICE --> DB
  INV --> DB
  CART --> DB
  ORDER --> DB
  RX --> DB
  SHIP --> DB
  NOTI --> DB

  SEARCH --> IDX[(Search Index\nElastic/Meili/PG FTS)]
  CATALOG --> CACHE[(Cache Redis)]
  PRICE --> CACHE
  INV --> CACHE