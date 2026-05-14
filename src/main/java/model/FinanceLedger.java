package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks all money entering and leaving a {@link Player}'s account.
 *
 * <p>Regular income and spending are recorded as individual {@link Transaction} entries.
 * Vehicle maintenance is handled separately: it deducts from {@code currentCapital} but
 * is aggregated into {@link #maintenanceTotals} (keyed by {@code "VehicleType | RouteName"})
 * rather than adding a transaction per tick, to avoid flooding the Finance panel.
 */
public class FinanceLedger {
    /** Current cash balance. May go negative (bankruptcy is tracked separately). */
    private double currentCapital;
    /** Ordered log of all non-maintenance transactions. */
    private final List<Transaction> transactions;
    /** Aggregated maintenance totals keyed by "VehicleType | RouteName". */
    private final Map<String, Double> maintenanceTotals = new LinkedHashMap<>();

    /**
     * Creates a ledger with the given starting balance and an empty transaction log.
     *
     * @param startingCapital initial cash amount
     */
    public FinanceLedger(double startingCapital) {
        this.currentCapital = startingCapital;
        this.transactions = new ArrayList<>();
    }

    /** Returns the current cash balance. */
    public double getCurrentCapital() {
        return currentCapital;
    }

    /** Returns an unmodifiable view of all logged transactions (newest is last). */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Returns {@code true} if the current balance is at least {@code amount}.
     *
     * @param amount the amount to check affordability for
     */
    public boolean canAfford(double amount) {
        return currentCapital >= amount;
    }

    /**
     * Deducts {@code amount} and logs a negative transaction.
     *
     * @param amount positive value to deduct
     * @param type   transaction category
     * @param note   human-readable description shown in the Finance panel
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    public void spend(double amount, model.enums.TransactionType type, String note) {
        if (amount < 0) throw new IllegalArgumentException("Spend amount must be positive");
        currentCapital -= amount;
        transactions.add(new Transaction(-amount, type, note));
    }

    /**
     * Adds {@code amount} and logs a positive transaction.
     *
     * @param amount positive value to add
     * @param type   transaction category
     * @param note   human-readable description shown in the Finance panel
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    public void earn(double amount, model.enums.TransactionType type, String note) {
        if (amount < 0) throw new IllegalArgumentException("Earn amount must be positive");
        currentCapital += amount;
        transactions.add(new Transaction(amount, type, note));
    }

    /**
     * Replaces the entire ledger state — used by {@link model.service.SaveManager} on load.
     *
     * @param capital                    restored cash balance
     * @param restoredTransactions       transaction log from the save file
     * @param restoredMaintenanceTotals  maintenance aggregates from the save file
     */
    public void restore(double capital, List<Transaction> restoredTransactions, Map<String, Double> restoredMaintenanceTotals) {
        this.currentCapital = capital;
        this.transactions.clear();
        this.transactions.addAll(restoredTransactions);
        this.maintenanceTotals.clear();
        this.maintenanceTotals.putAll(restoredMaintenanceTotals);
    }

    /** Deducts maintenance silently — aggregated by key, not stored as individual transactions. */
    public void recordMaintenance(String vehicleKey, double amount) {
        currentCapital -= amount;
        maintenanceTotals.merge(vehicleKey, amount, Double::sum);
    }

    /** Returns an unmodifiable view of the running maintenance totals. */
    public Map<String, Double> getMaintenanceTotals() {
        return Collections.unmodifiableMap(maintenanceTotals);
    }
}
