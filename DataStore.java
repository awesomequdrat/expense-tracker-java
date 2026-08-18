import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Very small CSV-based persistence layer.
 * Files live under a folder (for example, ./data):
 *   - expenses.csv: id,amount,date,category,note
 *   - categories.txt: one category per line
 *   - budget.txt: single number (limit) or empty
 */
public class DataStore {
    private final Path dir;

    public DataStore(Path dir) { this.dir = dir; }

    public Ledger load() {
        Ledger ledger = new Ledger();
        try {
            Files.createDirectories(dir);
            Path expenses = dir.resolve("expenses.csv");
            Path categories = dir.resolve("categories.txt");
            Path budget = dir.resolve("budget.txt");

            if (Files.exists(categories)) {
                Set<String> cats = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                for (String line : Files.readAllLines(categories, StandardCharsets.UTF_8)) {
                    String trimmed = line.trim();
                    if (!trimmed.isBlank()) cats.add(trimmed);
                }
                if (!cats.isEmpty()) ledger.replaceCategories(cats);
            }

            if (Files.exists(expenses)) {
                List<String> lines = Files.readAllLines(expenses, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (line.isBlank() || line.startsWith("id,")) continue; // header/empty
                    String[] parts = parseCsv(line, 5);
                    try {
                        int id = Integer.parseInt(parts[0]);
                        BigDecimal amt = new BigDecimal(parts[1]).setScale(2);
                        LocalDate date = LocalDate.parse(parts[2]);
                        String category = parts[3];
                        String note = parts[4];
                        ledger.addExpenseDirect(new Expense(id, amt, date, note, category));
                    } catch (NumberFormatException | DateTimeParseException ignore) {
                        // skip bad row
                    }
                }
            }

            if (Files.exists(budget)) {
                String s = Files.readString(budget, StandardCharsets.UTF_8).trim();
                if (!s.isBlank()) {
                    ledger.setBudgetDirect(new Budget(new BigDecimal(s).setScale(2)));
                }
            }
        } catch (IOException ex) {
            System.out.println("Warning: failed to load data: " + ex.getMessage());
        }
        return ledger;
    }

    public void save(Ledger ledger) throws IOException {
        Files.createDirectories(dir);
        // categories
        Path categories = dir.resolve("categories.txt");
        Files.write(categories, ledger.getCategories(), StandardCharsets.UTF_8);

        // budget
        Path budget = dir.resolve("budget.txt");
        String val = ledger.getBudget().map(b -> b.getLimit().toString()).orElse("");
        Files.writeString(budget, val, StandardCharsets.UTF_8);

        // expenses (with header)
        Path expenses = dir.resolve("expenses.csv");
        StringBuilder sb = new StringBuilder();
        sb.append("id,amount,date,category,note\n");
        for (Expense e : ledger.listExpenses()) {
            sb.append(e.getId()).append(',')
              .append(e.getAmount()).append(',')
              .append(e.getDate()).append(',')
              .append(escapeCsv(e.getCategory())).append(',')
              .append(escapeCsv(e.getNote())).append('\n');
        }
        Files.writeString(expenses, sb.toString(), StandardCharsets.UTF_8);
    }

    // CSV helpers (very small escape/unescape)
    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    // Updated: switch-based CSV parser to silence lint hint
    private String[] parseCsv(String line, int expectedParts) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                // Inside quotes: either close quote, escaped quote, or plain char
                switch (c) {
                    case '"' -> {
                        if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                            cur.append('"');  // escaped quote ("")
                            i++;
                        } else {
                            inQuotes = false; // end quoted field
                        }
                    }
                    default -> cur.append(c);
                }
            } else {
                // Outside quotes: comma ends field, quote starts quoted field, else append
                switch (c) {
                    case ',' -> { parts.add(cur.toString()); cur.setLength(0); }
                    case '"' -> inQuotes = true;
                    default -> cur.append(c);
                }
            }
        }

        parts.add(cur.toString());
        while (parts.size() < expectedParts) parts.add("");
        return parts.toArray(String[]::new);
    }
}