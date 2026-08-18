import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Console entry point for the Expense Tracker.
 * Menu-driven UI that exercises the domain classes.
 */
public class Main {
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Expense Tracker (Console) ===");
        Path dataDir = Path.of("data");
        DataStore store = new DataStore(dataDir);
        Ledger ledger = store.load();

        while (true) {
            printMenu();
            String choice = in.nextLine().trim();
            switch (choice) {
                case "1" -> recordExpense(ledger, store);
                case "2" -> manageCategories(ledger, store);
                case "3" -> setBudget(ledger, store);
                case "4" -> viewTotalsByCategory(ledger);
                case "5" -> viewBudgetStatus(ledger);
                case "6" -> listExpenses(ledger);
                case "7" -> editOrDeleteExpense(ledger, store);
                case "8" -> exportSummary(ledger);
                case "9" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1) Record Expense");
        System.out.println("2) Manage Categories");
        System.out.println("3) Set Budget");
        System.out.println("4) View Totals by Category");
        System.out.println("5) View Budget Status");
        System.out.println("6) List Expenses");
        System.out.println("7) Edit/Delete Expense");
        System.out.println("8) Export Summary (CSV/JSON)");
        System.out.println("9) Exit");
        System.out.print("Choose an option: ");
    }

    private static void recordExpense(Ledger ledger, DataStore store) {
        try {
            System.out.print("Amount (e.g., 12.34): ");
            BigDecimal amount = new BigDecimal(in.nextLine().trim()).setScale(2);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            if (ledger.getCategories().isEmpty()) {
                System.out.println("No categories defined. Add a category first.");
                return;
            }
            System.out.println("Choose a category from: " + ledger.getCategories());
            System.out.print("Category: ");
            String category = in.nextLine().trim();
            if (!ledger.getCategories().contains(category)) {
                System.out.println("Unknown category.");
                return;
            }

            System.out.print("Date (YYYY-MM-DD, blank = today): ");
            String ds = in.nextLine().trim();
            LocalDate date = ds.isBlank() ? LocalDate.now() : LocalDate.parse(ds);

            System.out.print("Note (optional): ");
            String note = in.nextLine();

            Expense e = ledger.addExpense(amount, category, date, note);
            store.save(ledger);
            System.out.println("Saved expense with id " + e.getId());
        } catch (NumberFormatException ex) {
            System.out.println("Invalid amount.");
        } catch (DateTimeParseException ex) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
        } catch (IOException ex) {
            System.out.println("Failed to save: " + ex.getMessage());
        }
    }

    private static void manageCategories(Ledger ledger, DataStore store) {
        System.out.println("Categories: " + ledger.getCategories());
        System.out.println("a) Add   b) Remove   other) Back");
        System.out.print("Choose: ");
        String c = in.nextLine().trim().toLowerCase(Locale.ROOT);
        try {
            switch (c) {
                case "a" -> {
                    System.out.print("New category name: ");
                    String name = in.nextLine().trim();
                    if (name.isBlank()) {
                        System.out.println("Name cannot be blank.");
                        return;
                    }
                    if (ledger.addCategory(name)) {
                        store.save(ledger);
                        System.out.println("Added category.");
                    } else {
                        System.out.println("Category already exists.");
                    }
                }
                case "b" -> {
                    System.out.print("Category to remove: ");
                    String name = in.nextLine().trim();
                    if (ledger.removeCategory(name)) {
                        store.save(ledger);
                        System.out.println("Removed category.");
                    } else {
                        System.out.println("Category not found or in use by expenses.");
                    }
                }
                default -> { /* back */ }
            }
        } catch (IOException ex) {
            System.out.println("Save error: " + ex.getMessage());
        }
    }

    private static void setBudget(Ledger ledger, DataStore store) {
        try {
            System.out.print("Budget amount: ");
            BigDecimal limit = new BigDecimal(in.nextLine().trim()).setScale(2);
            ledger.setBudget(limit);
            store.save(ledger);
            System.out.println("Budget set to $" + limit);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid number.");
        } catch (IOException ex) {
            System.out.println("Save error: " + ex.getMessage());
        }
    }

    private static void viewTotalsByCategory(Ledger ledger) {
        Map<String, BigDecimal> map = ledger.totalsByCategory();
        if (map.isEmpty()) {
            System.out.println("No expenses recorded.");
            return;
        }
        System.out.println("--- Totals by Category ---");
        map.forEach((cat, total) -> System.out.printf("%s : $%s%n", cat, total));
    }

    private static void viewBudgetStatus(Ledger ledger) {
        BigDecimal total = ledger.totalSpent();
        Optional<Budget> b = ledger.getBudget();
        System.out.printf("Total spent: $%s%n", total);
        if (b.isPresent()) {
            Budget budget = b.get();
            BigDecimal remaining = budget.remaining(total);
            boolean over = budget.isOver(total);
            if (over) {
                System.out.printf("Over budget by $%s%n", remaining.abs());
            } else {
                System.out.printf("Remaining: $%s%n", remaining);
            }
        } else {
            System.out.println("No budget set.");
        }
    }

    private static void listExpenses(Ledger ledger) {
        List<Expense> list = ledger.listExpenses();
        if (list.isEmpty()) {
            System.out.println("No expenses recorded.");
            return;
        }
        System.out.printf("%-4s %-10s %-12s %-15s %s%n", "ID", "Amount", "Date", "Category", "Note");
        for (Expense e : list) {
            System.out.printf("%-4d $%-9s %-12s %-15s %s%n",
                    e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
        }
    }

    private static void editOrDeleteExpense(Ledger ledger, DataStore store) {
        listExpenses(ledger);
        if (ledger.listExpenses().isEmpty()) return;
        System.out.print("Enter ID to edit/delete: ");
        String idS = in.nextLine().trim();
        try {
            int id = Integer.parseInt(idS);
            Optional<Expense> opt = ledger.findById(id);
            if (opt.isEmpty()) {
                System.out.println("ID not found.");
                return;
            }
            Expense e = opt.get();
            System.out.println("a) Edit  b) Delete  other) Back");
            String c = in.nextLine().trim().toLowerCase(Locale.ROOT);
            if (c.equals("b")) {
                ledger.removeExpense(id);
                store.save(ledger);
                System.out.println("Deleted.");
            } else if (c.equals("a")) {
                System.out.print("New amount (blank=keep " + e.getAmount() + "): ");
                String a = in.nextLine().trim();
                BigDecimal amount = a.isBlank() ? e.getAmount() : new BigDecimal(a).setScale(2);

                System.out.print("New category (blank=keep " + e.getCategory() + "): ");
                String cat = in.nextLine().trim();
                String category = cat.isBlank() ? e.getCategory() : cat;
                if (!ledger.getCategories().contains(category)) {
                    System.out.println("Unknown category.");
                    return;
                }

                System.out.print("New date YYYY-MM-DD (blank=keep " + e.getDate() + "): ");
                String ds = in.nextLine().trim();
                LocalDate date = ds.isBlank() ? e.getDate() : LocalDate.parse(ds);

                System.out.print("New note (blank=keep current): ");
                String note = in.nextLine();
                if (note.isBlank()) note = e.getNote();

                ledger.editExpense(id, amount, category, date, note);
                store.save(ledger);
                System.out.println("Updated.");
            }
        } catch (NumberFormatException | DateTimeParseException ex) {
            System.out.println("Invalid number/date: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Save failed: " + ex.getMessage());
        }
    }

    private static void exportSummary(Ledger ledger) {
        System.out.println("Choose format: 1) CSV  2) JSON");
        System.out.print("Format: ");
        String c = in.nextLine().trim();
        Exporter exporter = c.equals("2") ? new JSONExporter() : new CSVExporter();
        try {
            Path out = Path.of("export");
            exporter.exportSummary(ledger, out);
            System.out.println("Exported to folder: " + out.toAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Export failed: " + ex.getMessage());
        }
    }
}