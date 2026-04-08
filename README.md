# 📦 GearRent Pro – System Overview

GearRent Pro is a **multi-branch equipment rental management application** designed for the **CMJD Java Developer Program (IJSE)**.  
It manages customers, inventory, reservations, rentals, and branch operations with a **scalable layered architecture**.

---

## 🗂️ Project Architecture
- **UI:** JavaFX  
- **Architecture Pattern:** Layered (Controller → Service → DAO → Entity)  
- **Database:** MySQL  
- **Build Tool:** Maven  

---

# ⚙️ Setup Instructions

## 1️⃣ Prerequisites
- Java **JDK 17+** (Recommended: **JDK 21**)  
- MySQL Server **8.0+**  
- Maven or IntelliJ IDEA with Maven support  

---

## 2️⃣ Database Setup

1. Open **MySQL Workbench** or **Terminal**  
2. Run this script: 'src/main/resources/sql/database.sql'


This will create the **gearrentpro** database and insert initial seed data.

### ✅ Default Login Accounts
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | 1234 |
| Branch Manager | manager_col | 1234 |
| Staff | staff_col | 1234 |

---

## 3️⃣ Database Connection

Edit the following file: 'src/main/java/lk/ijse/gearrentpro/util/DBConnection.java'


Update your MySQL password:

```java
private static final String PASSWORD = "your_mysql_password";
