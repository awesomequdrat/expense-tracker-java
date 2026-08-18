import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {
    private final int id;
    private BigDecimal amount;
    private LocalDate date;
    private String note;
    private String category;

    public Expense(int id, BigDecimal amount, LocalDate date, String note, String category) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.note = note == null ? "" : note;
        this.category = category;
    }

    public int getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getNote() { return note; }
    public String getCategory() { return category; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setNote(String note) { this.note = note; }
    public void setCategory(String category) { this.category = category; }
}