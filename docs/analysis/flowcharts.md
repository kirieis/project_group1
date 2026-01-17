                           ┌──────────────┐
                           │    START     │
                           └──────┬───────┘
                                  │
                                  ▼
                        ┌───────────────────┐
                        │   User Login      │
                        │ (Username, Pass)  │
                        └──────┬────────────┘
                               │
                 ┌─────────────┴─────────────┐
                 │   Credentials valid?       │
                 └──────┬─────────────┬──────┘
                        │No            │Yes
                        ▼              ▼
              ┌──────────────────┐   ┌────────────────────────┐
              │   Show error     │   │   Identify User Role   │
              │   Login again    │   │ (Admin / Manager /     │
              └────────┬─────────┘   │  Staff)                │
                       │             └─────────┬──────────────┘
                       │                       │
                       └───────────────────────┘
                                               ▼
                          ┌────────────────────────────────┐
                          │    Display System Dashboard     │
                          └───────────┬────────────────────┘
                                      │
      ┌───────────────────────────────┼──────────────────────────────────┐
      │                               │                                  │
      ▼                               ▼                                  ▼
┌──────────────────┐        ┌────────────────────────┐        ┌────────────────────┐
│ Medicine         │        │ Inventory & Batch      │        │ Sales & Invoice     │
│ Management       │        │ Management             │        │ Management          │
└───────┬──────────┘        └─────────┬──────────────┘        └─────────┬──────────┘
        │                              │                                   │
        ▼                              ▼                                   ▼
[Add / Update / Delete]      [Import / Export Stock]            [Create Invoice]
        │                              │                                   │
        ▼                              ▼                                   ▼
[Save Medicine Data]   [Update Batch & Inventory Data]   [Check Inventory]
        │                              │                                   │
        └──────────────┬───────────────┘                                   ▼
                       │                                   [Select batch with
                       │                                    nearest expiry
                       │                                    (FIFO)]
                       ▼                                                │
            ┌──────────────────────────────┐                           ▼
            │   Expiry Date Monitoring     │                [Deduct Inventory]
            │   (All Batches)              │                           │
            └───────────┬──────────────────┘                           ▼
                        │                                  [Save Invoice Details]
        ┌───────────────┴────────────────┐                             │
        │ Expired or Near Expiry?         │                             ▼
        └──────┬─────────────┬───────────┘                   ┌──────────────────┐
               │Yes           │No                              │   Payment        │
               ▼              ▼                               │   Processing     │
     ┌──────────────────┐     │                               └──────┬───────────┘
     │ Display Warning  │     │                                      │
     │ to Manager/Admin │     │                    ┌─────────────────┴─────────────────┐
     └──────────┬───────┘     │                    │   Payment Successful?              │
                │             │                    └──────┬──────────┬────────────────┘
                └─────────────┘                           │No         │Yes
                                                            ▼           ▼
                                              [Mark Invoice as UNPAID]  [Mark Invoice as PAID]
                                                                       │
                                                                       ▼
                                                             [Update Revenue]
                                                                       │
                                                                       ▼
                                         ┌──────────────────────────────────────────┐
                                         │     Reports & Analytics                   │
                                         │  - Revenue Reports                        │
                                         │  - Inventory Status                       │
                                         │  - Expiry Warnings                        │
                                         └───────────────┬──────────────────────────┘
                                                         │
                                                         ▼
                                               ┌────────────────┐
                                               │      END       │
                                               └────────────────┘
