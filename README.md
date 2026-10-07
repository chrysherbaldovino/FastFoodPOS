# Fast Food POS System
A Java-based Point of Sale system designed to manage restaurant operations, featuring a Customer Kiosk, Kitchen Display System (KDS), and Staff Terminal. Built for Computer Programming 3.

## Technologies Used
* **Frontend & Backend:** Java (Swing)
* **Database:** MySQL (JDBC)

## Features
* Fully normalized relational database with transaction rollback logic.
* Real-time order queueing between the Kiosk and Kitchen Display.
* End-of-day cutoff protocol that archives orders while preserving historical data.
* Dynamic Sales & ROI analytics (Daily, Weekly, Monthly, Lifetime).

## How to Run
1. Execute `1_Schema.sql` in MySQL to build the tables.
2. Execute `2_SampleData.sql` to populate historical analytics.
3. Update the `db.properties` file with your local MySQL credentials.
4. Run the main Java class to launch the POS terminals.
