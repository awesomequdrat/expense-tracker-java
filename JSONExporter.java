import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.math.BigDecimal;

public class JSONExporter implements Exporter {
    @Override
    public void exportSummary(Ledger ledger, Path outDir) throws IOException {
        Files.createDirectories(outDir);
        String json = toJson(ledger);
        Files.writeString(outDir.resolve("summary.json"), json, StandardCharsets.UTF_8);
    }

    // Minimal JSON building without third-party libs
    private String toJson(Ledger ledger) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"budget\": ");
        if (ledger.getBudget().isPresent()) {
            sb.append('"').append(ledger.getBudget().get().getLimit()).append('"');
        } else {
            sb.append("null");
        }
        sb.append(",\n");

        sb.append("  \"totalsByCategory\": {\n");
        int i = 0, size = ledger.totalsByCategory().size();
        for (Map.Entry<String, BigDecimal> e : ledger.totalsByCategory().entrySet()) {
            sb.append("    \"").append(escape(e.getKey())).append("\": \"").append(e.getValue()).append("\"");
            if (++i < size) sb.append(',');
            sb.append("\n");
        }
        sb.append("  },\n");

        sb.append("  \"expenses\": [\n");
        for (int j = 0; j < ledger.listExpenses().size(); j++) {
            Expense ex = ledger.listExpenses().get(j);
            sb.append("    {\n");
            sb.append("      \"id\": ").append(ex.getId()).append(",\n");
            sb.append("      \"amount\": \"").append(ex.getAmount()).append("\",\n");
            sb.append("      \"date\": \"").append(ex.getDate()).append("\",\n");
            sb.append("      \"category\": \"").append(escape(ex.getCategory())).append("\",\n");
            sb.append("      \"note\": \"").append(escape(ex.getNote())).append("\"\n");
            sb.append("    }");
            if (j < ledger.listExpenses().size()-1) sb.append(',');
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}