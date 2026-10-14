# Smart Grocery Inventory & Shopping List Generator

A complete, database-connected household grocery inventory and automated meal planning web application designed for a College Mini Project.

---

## 1. Project Introduction

Managing household groceries often suffers from lack of inventory visibility, unexpected food spoilage, uncoordinated meal planning, and grocery budget overruns. The **Smart Grocery Inventory & Shopping List Generator** solves these problems with a unified, database-backed platform:
- Tracks batch-level quantities and expiration dates with FEFO (First-Expired, First-Out) inventory deduction.
- Plans weekly meals across Breakfast, Lunch, Dinner, and Snacks with automated ingredient scaling.
- Generates shopping lists automatically based on ingredient shortages (`Shortage = MAX(0, Required - Usable)`).
- Tracks monthly budget allocations, records actual grocery expenses, and calculates real-time budget utilization percentages.
- Simulates optical barcode scanning and multi-item receipt importing with audit tracking.

---

## 2. Technology Stack

- **Programming Language**: Java 21
- **Backend Framework**: Spring Boot 3.3.4 (Spring MVC, Spring Data JPA, Spring Security, Spring Validation)
- **Frontend / Template Engine**: Thymeleaf, Bootstrap 5.3, Bootstrap Icons, HTML5, CSS3, JavaScript
- **Database Engine**: MySQL 8.x
- **ORM & Migrations**: Hibernate 6, MySQL Connector/J, Flyway Database Migrations
- **Build & Dependency Tool**: Apache Maven 3.9+
- **Automated Testing**: JUnit 5, Spring Boot Test, MockMvc, H2 (isolated in-memory test profile)

---

## 3. Project Directory Structure

```text
smartgrocery new/
│── pom.xml                                      # Maven dependencies & build configuration
│── README.md                                    # Comprehensive setup & project guide
│── .gitignore                                   # Git ignore rules for build & IDE files
├── database/
│   ├── schema.sql                               # Standalone MySQL schema definition
│   └── sample-data.sql                          # Standalone initial sample data
├── src/
│   ├── main/
│   │   ├── java/com/example/smartgrocery/
│   │   │   ├── SmartGroceryApplication.java     # Spring Boot application entry point
│   │   │   ├── config/                          # Security & Web configuration
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── security/                        # UserDetails & authentication services
│   │   │   │   ├── CustomUserDetails.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   ├── entity/                          # 15 JPA Entity classes
│   │   │   │   ├── User.java
│   │   │   │   ├── GroceryCategory.java
│   │   │   │   ├── GroceryItem.java
│   │   │   │   ├── InventoryBatch.java
│   │   │   │   ├── Recipe.java
│   │   │   │   ├── RecipeIngredient.java
│   │   │   │   ├── MealPlan.java
│   │   │   │   ├── MealPlanEntry.java
│   │   │   │   ├── ShoppingList.java
│   │   │   │   ├── ShoppingListItem.java
│   │   │   │   ├── Budget.java
│   │   │   │   ├── Expense.java
│   │   │   │   ├── Alert.java
│   │   │   │   ├── ScanImport.java
│   │   │   │   └── ScanImportItem.java
│   │   │   ├── repository/                      # Spring Data JPA repositories
│   │   │   ├── dto/                             # Form & Transfer objects with Jakarta Validation
│   │   │   ├── exception/                       # Custom exceptions & GlobalExceptionHandler
│   │   │   ├── service/                         # Business logic services
│   │   │   │   ├── UserService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── InventoryService.java
│   │   │   │   ├── RecipeService.java
│   │   │   │   ├── MealPlanService.java
│   │   │   │   ├── ShoppingListService.java
│   │   │   │   ├── BudgetService.java
│   │   │   │   ├── ExpenseService.java
│   │   │   │   ├── AlertService.java
│   │   │   │   ├── ScanningService.java
│   │   │   │   ├── ReportService.java
│   │   │   │   └── DashboardService.java
│   │   │   └── controller/                      # MVC Web Controllers
│   │   │       ├── AuthController.java
│   │   │       ├── DashboardController.java
│   │   │       ├── InventoryController.java
│   │   │       ├── CategoryController.java
│   │   │       ├── RecipeController.java
│   │   │       ├── MealPlanController.java
│   │   │       ├── ShoppingListController.java
│   │   │       ├── BudgetController.java
│   │   │       ├── ExpenseController.java
│   │   │       ├── AlertController.java
│   │   │       ├── ScanningController.java
│   │   │       ├── ReportController.java
│   │   │       └── SettingsController.java
│   │   └── resources/
│   │       ├── application.properties           # MySQL connection and app properties
│   │       ├── db/migration/                    # Flyway migration scripts
│   │       │   ├── V1__create_initial_schema.sql
│   │       │   ├── V2__insert_default_categories.sql
│   │       │   └── V3__insert_sample_data.sql
│   │       ├── static/
│   │       │   ├── css/style.css                # Green/Orange themed stylesheet
│   │       │   └── js/main.js                   # Dynamic scanning & UI logic
│   │       └── templates/                       # 17 Thymeleaf HTML templates
│   │           ├── fragments/                   # Reusable navbar, sidebar, footer, layout
│   │           ├── login.html
│   │           ├── register.html
│   │           ├── dashboard.html
│   │           ├── inventory.html
│   │           ├── add-item.html
│   │           ├── edit-item.html
│   │           ├── categories.html
│   │           ├── recipes.html
│   │           ├── meal-planner.html
│   │           ├── shopping-list.html
│   │           ├── budget.html
│   │           ├── expenses.html
│   │           ├── alerts.html
│   │           ├── scanning.html
│   │           ├── reports.html
│   │           ├── settings.html
│   │           └── error.html
│   └── test/
│       ├── java/com/example/smartgrocery/       # JUnit 5 & MockMvc automated tests
│       │   ├── UnitConversionTest.java
│       │   ├── CalculationServiceTest.java
│       │   ├── ValidationTest.java
│       │   ├── AuthControllerTest.java
│       │   └── InventoryServiceTest.java
│       └── resources/
│           └── application.properties           # Isolated test database profile
```

---

## 4. Database Setup Guide (MySQL Workbench)

Follow these exact steps to prepare your local MySQL database:

### STEP 1: Open MySQL Workbench
Launch **MySQL Workbench** from your Windows Start Menu.

### STEP 2: Connect to MySQL Server
Click on your **Local instance MySQL80** (default host `localhost`, port `3306`) and authenticate as `root`.

### STEP 3: Create the Database
In a new SQL Query tab, execute:
```sql
CREATE DATABASE smart_grocery_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### STEP 4: Configure Database Credentials
You can provide credentials either via environment variables or directly in `src/main/resources/application.properties`.

**Option A (PowerShell Environment Variables):**
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/smart_grocery_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password_here"
```

**Option B (application.properties):**
Open `src/main/resources/application.properties` and verify or set your password:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_grocery_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password_here
server.port=8081
```

### STEP 5: Start the Spring Boot Application
Run the application using Maven:
```powershell
mvn spring-boot:run
```

### STEP 6: Automatic Flyway Table Creation
When Spring Boot starts up, Flyway will automatically execute:
- `V1__create_initial_schema.sql` (Creates all 15 tables with keys and constraints)
- `V2__insert_default_categories.sql` (Inserts standard grocery categories)
- `V3__insert_sample_data.sql` (Inserts demo user and sample records)

### STEP 7: Verify Tables in MySQL Workbench
Return to MySQL Workbench, open a new SQL tab, and execute:
```sql
USE smart_grocery_db;
SHOW TABLES;
```

### STEP 8: Expected Database Tables
You should see all **15 tables**:
1. `users`
2. `grocery_categories`
3. `grocery_items`
4. `inventory_batches`
5. `recipes`
6. `recipe_ingredients`
7. `meal_plans`
8. `meal_plan_entries`
9. `shopping_lists`
10. `shopping_list_items`
11. `budgets`
12. `expenses`
13. `alerts`
14. `scan_imports`
15. `scan_import_items`
plus `flyway_schema_history`.

---

## 5. How to Run the Application

### Prerequisites
- **Java 21 JDK** installed (`java -version`)
- **Apache Maven 3.8+** installed (`mvn -version`)
- **MySQL 8.x** server running

### Check MySQL Service on Windows
In PowerShell, verify MySQL is running:
```powershell
Get-Service -Name "*mysql*"
```
If stopped, start it:
```powershell
Start-Service -Name "MySQL80"
```

### Compile and Package
```powershell
mvn clean compile
```

### Run Automated Tests
```powershell
mvn test
```
*(Tests run in an isolated test database without modifying your MySQL records)*

### Launch Web Server
```powershell
mvn spring-boot:run
```
Once started, access the web application in any browser:
```text
http://localhost:8081
```

### How to Stop the Application
Press `Ctrl + C` in the PowerShell terminal running Spring Boot.

---

## 6. Default Demo Credentials & Feature Walkthrough

### Pre-populated Demo Account
- **Email**: `demo@grocery.com`
- **Password**: `password123`

### Key Modules Walkthrough
1. **Dashboard (`/dashboard`)**:
   Live statistics from MySQL: total items, calculated inventory valuation, low stock count, expiration attention count, upcoming meals, and budget spending progress bar.
2. **Inventory Management (`/inventory`)**:
   Add items with minimum stock alerts and unit prices. Add stock batches with purchase and expiration dates.
3. **Category Classification (`/categories`)**:
   11 pre-loaded database categories (Fruits, Vegetables, Dairy, Grains, Bakery, Meat and Fish, Beverages, Snacks, Spices, Household Essentials, Other).
4. **Recipe Book (`/recipes`)**:
   Create recipes with ingredient lists. Use the **Scale Servings** feature to dynamically recalculate ingredient quantities for any number of persons.
5. **Weekly Meal Planner (`/meal-plans`)**:
   Schedule meals for the week. Click **Cook** to verify inventory and deduct stock automatically using FEFO (First-Expired, First-Out).
6. **Automated Shopping List (`/shopping-lists`)**:
   Click **Auto-Generate List** to calculate shortage (`MAX(0, Required - Usable)`). Items are categorized automatically. Mark items as purchased with one-click inventory batch creation and expense logging.
7. **Monthly Budget & Expenses (`/budgets`, `/expenses`)**:
   Set monthly budgets. Record grocery expenditures and track budget percentage utilization with automatic overrun warnings.
8. **Simulated Barcode & Receipt Scanning (`/scanning`)**:
   Click quick barcodes (e.g. `8901030382910`) to lookup product information, confirm quantities, and add stock batches. Enter receipt line items for bulk import.
9. **Reports & CSV Export (`/reports`)**:
   View low-stock audits and expiration reports. Export full inventory or expense records as CSV files.

---

## 7. Troubleshooting

| Issue | Cause | Solution |
| :--- | :--- | :--- |
| `Access denied for user 'root'@'localhost'` | Incorrect MySQL password | Set `$env:DB_PASSWORD="your_password"` or update `application.properties`. |
| `Communications link failure` | MySQL service is not running | Run `Start-Service -Name "MySQL80"` in PowerShell. |
| `Unknown database 'smart_grocery_db'` | Database was not created yet | Execute `CREATE DATABASE smart_grocery_db;` in MySQL Workbench. |
| `Port 8080 already in use` | Another process is using port 8080 | Change `server.port=8081` in `application.properties` or stop the conflicting process. |
| `Flyway migration failed` | Incompatible database state | Drop and recreate `smart_grocery_db` to allow Flyway to initialize cleanly. |
