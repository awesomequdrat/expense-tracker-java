import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CSVExporter implements Exporter {
    @Override
    public void exportSummary(Ledger ledger, Path outDir) throws IOException {
        Files.createDirectories(outDir);
        // totals by category
        Path totals = outDir.resolve("totals_by_category.csv");
        StringBuilder sb = new StringBuilder();
        sb.append("Category,Total\n");
        for (Map.Entry<String, BigDecimal> e : ledger.totalsByCategory().entrySet()) {
            sb.append(escape(e.getKey())).append(',').append(e.getValue()).append('\n');
        }
        Files.writeString(totals, sb.toString(), StandardCharsets.UTF_8);

        // expenses
        Path expenses = outDir.resolve("expenses.csv");
        StringBuilder sb2 = new StringBuilder();
        sb2.append("id,amount,date,category,note\n");
        for (Expense ex : ledger.listExpenses()) {
            sb2.append(ex.getId()).append(',')
               .append(ex.getAmount()).append(',')
               .append(ex.getDate()).append(',')
               .append(escape(ex.getCategory())).append(',')
               .append(escape(ex.getNote())).append('\n');
        }
        Files.writeString(expenses, sb2.toString(), StandardCharsets.UTF_8);

        // budget
        Path budget = outDir.resolve("budget.csv");
        String val = ledger.getBudget().map(b -> b.getLimit().toString()).orElse("");
        Files.writeString(budget, "limit\n" + val + "\n", StandardCharsets.UTF_8);
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}