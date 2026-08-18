import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Ledger {
    private final List<Expense> expenses = new ArrayList<>();
    private final Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private Budget budget; 
    private final AtomicInteger nextId = new AtomicInteger(1);

    public Ledger() {
        // Some sensible defaults for first launch
        categories.addAll(List.of("Food", "Transportation", "Entertainment", "Other"));
    }

    public List<Expense> listExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public Set<String> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    public Optional<Budget> getBudget() {
        return Optional.ofNullable(budget);
    }

    public Expense addExpense(BigDecimal amount, String category, LocalDate date, String note) {
        Expense e = new Expense(nextId.getAndIncrement(), amount.setScale(2), date, note, category);
        expenses.add(e);
        return e;
    }

    public boolean removeExpense(int id) {
        return expenses.removeIf(e -> e.getId() == id);
    }

    public Optional<Expense> findById(int id) {
        return expenses.stream().filter(e -> e.getId() == id).findFirst();
    }

    public void editExpense(int id, BigDecimal amount, String category, LocalDate date, String note) {
        Expense e = findById(id).orElseThrow();
        e.setAmount(amount.setScale(2));
        e.setCategory(category);
        e.setDate(date);
        e.setNote(note);
    }

    public boolean addCategory(String name) {
        return categories.add(name);
    }

    public boolean removeCategory(String name) {
        // don't allow removing if used
        boolean used = expenses.stream().anyMatch(e -> e.getCategory().equalsIgnoreCase(name));
        if (used) return false;
        return categories.remove(name);
    }

    public void setBudget(BigDecimal limit) {
        this.budget = new Budget(limit.setScale(2));
    }

    public BigDecimal totalSpent() {
        return expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
    }

    public Map<String, BigDecimal> totalsByCategory() {
        Map<String, BigDecimal> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Expense e : expenses) {
            map.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }
        // scale
        return map.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            en -> en.getValue().setScale(2),
            (a,b) -> a,
            TreeMap::new
        ));
    }

    // Loading helpers
    void addExpenseDirect(Expense e) {
        expenses.add(e);
        nextId.set(Math.max(nextId.get(), e.getId()+1));
    }
    void replaceCategories(Set<String> cats) {
        categories.clear();
        categories.addAll(cats);
    }
    void setBudgetDirect(Budget b) { this.budget = b; }
}