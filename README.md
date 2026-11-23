# RescueGrid - Intelligent Disaster Response & Resource Allocation System

Console-based Core Java project (46 classes) with MySQL via JDBC.

## Compile and run (Windows / Linux / Mac)
```
cd RescueGrid
mkdir out
javac -d out src/*/*.java
java -cp "out;mysql-connector-j-8.x.x.jar" main.Main      (Windows)
java -cp "out:mysql-connector-j-8.x.x.jar" main.Main      (Linux / Mac)
```
Without the MySQL jar everything except menu 11 (Database) still works.

Default login: `admin` / `admin123`

## Database setup
1. Start MySQL and download the MySQL Connector/J jar.
2. Run the program -> menu 11 -> option 1 (set your MySQL user/password) -> option 2 (creates database and tables).
   (`schema.sql` has the same tables if you prefer to run it yourself.)
3. Option 3 saves everything, option 4 loads it back.

## Packages
main, interfaces, model, exception, service, dao, thread, filehandling, util
