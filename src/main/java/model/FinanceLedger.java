package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FinanceLedger tracks all money entering and leaving a Player's account.
 */
public class FinanceLedger {
    private double currentCapital;
    private final List<Transaction> transactions;
    /** Aggregated maintenance totals keyed by "VehicleType | RouteName". */
    private final Map<String, Double> maintenanceTotals = new LinkedHashMap<>();

    public FinanceLedger(double startingCapital) {
        this.currentCapital = startingCapital;
        this.transactions = new ArrayList<>();
    }

    public double getCurrentCapital() {
        return currentCapital;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public boolean canAfford(double amount) {
        return currentCapital >= amount;
    }

    public void spend(double amount, model.enums.TransactionType type, String note) {
        if (amount < 0) throw new IllegalArgumentException("Spend amount must be positive");
        currentCapital -= amount;
        transactions.add(new Transaction(-amount, type, note));
    }

    public void earn(double amount, model.enums.TransactionType type, String note) {
        if (amount < 0) throw new IllegalArgumentException("Earn amount must be positive");
        currentCapital += amount;
        transactions.add(new Transaction(amount, type, note));
    }

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

    public Map<String, Double> getMaintenanceTotals() {
        return Collections.unmodifiableMap(maintenanceTotals);
    }
}
