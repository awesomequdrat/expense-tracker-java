import java.math.BigDecimal;

public class Budget {
    private final BigDecimal limit;
    public Budget(BigDecimal limit) { this.limit = limit; }
    public BigDecimal getLimit() { return limit; }
    public BigDecimal remaining(BigDecimal total) { return limit.subtract(total).setScale(2); }
    public boolean isOver(BigDecimal total) { return total.compareTo(limit) > 0; }
}