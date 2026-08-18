Expense Tracker (Java Console)

Requirements
- Java 17 or newer

How to run
1. Open a terminal in the project folder (ExpenseTracker).
2. Compile:
   javac -d out *.java
3. Run:
   java -cp out Main

What the app does
- Record an expense: amount, category, date, note
- Manage categories: add and remove
- Set an overall budget
- See totals by category
- See budget status: remaining or over
- List all expenses
- Edit or delete an expense
- Export data to CSV or JSON

Where data is saved
- A folder named data/ is created next to the program.
  - expenses.csv   : id,amount,date,category,note
  - categories.txt : one category per line
  - budget.txt     : the budget limit or empty if not set
- Exports are written to export/ (CSV files and a JSON file if you choose JSON).

Notes
- Money uses BigDecimal with 2 decimal places.
- The app checks for bad input and asks again instead of crashing.
- No extra libraries are required.