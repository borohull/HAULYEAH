package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FinanceLedger tracks all money entering and leaving a Player's account.
 */
public class FinanceLedger {
    private double currentCapital;
    private final List<Transaction> transactions;

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
}
