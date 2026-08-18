import java.io.IOException;
import java.nio.file.Path;

public interface Exporter {
    void exportSummary(Ledger ledger, Path outDir) throws IOException;
}