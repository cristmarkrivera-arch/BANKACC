# Bank Account Management System

A Java Swing + SQLite educational desktop application.

## Requirements
- JDK 17 or later
- Maven 3.8 or later
- Internet connection on the first Maven build (to download SQLite JDBC)

## Run in VS Code / terminal
Open this project folder in VS Code, then run:

```bash
mvn clean compile
mvn exec:java
```

The app creates `bank_system.db` in the project working directory automatically.

## Features
- Separate Login and Registration pages
- Registration automatically opens the dashboard
- SQLite persistence for users, bank accounts, and transaction records
- Passwords stored as salted PBKDF2 hashes (not plain text)
- Accounts are scoped to the signed-in user
- Create, list, search, deposit, withdraw, check balance, update, and delete
- Savings accounts require a ₱500 opening/minimum balance
- Checking accounts cannot be overdrawn

## Notes
This is a classroom/demo application, not production banking software. It does not connect to a real bank or handle real money. Keep the generated database file safe; it contains local account records.
