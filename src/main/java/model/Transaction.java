package model;

import model.enums.TransactionType;
import java.time.LocalDateTime;

/**
 * Immutable record of a single financial event logged by {@link FinanceLedger}.
 *
 * <p>Positive amounts represent income; negative amounts represent expenditure.
 * Maintenance costs are NOT stored as transactions — they are aggregated separately
 * in {@link FinanceLedger#getMaintenanceTotals()}.
 */
public class Transaction {
    /** Signed amount: positive = income, negative = spending. */
    private final double amount;
    /** Category used for filtering in the Finance panel. */
    private final TransactionType type;
    /** Wall-clock time the transaction occurred (simulation time is not tracked). */
    private final LocalDateTime timestamp;
    /** Short human-readable description shown in the Finance panel. */
    private final String note;

    /**
     * Creates a transaction with an explicit timestamp (used by {@link model.service.SaveManager} on load).
     *
     * @param amount    signed cash delta
     * @param type      transaction category
     * @param timestamp when the transaction occurred
     * @param note      display description
     */
    public Transaction(double amount, TransactionType type, LocalDateTime timestamp, String note) {
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.note = note;
    }

    /**
     * Creates a transaction timestamped to {@link LocalDateTime#now()}.
     *
     * @param amount signed cash delta
     * @param type   transaction category
     * @param note   display description
     */
    public Transaction(double amount, TransactionType type, String note) {
        this(amount, type, LocalDateTime.now(), note);
    }

    /** Returns the signed cash delta (positive = income, negative = spending). */
    public double getAmount() { return amount; }
    /** Returns the category of this transaction. */
    public TransactionType getType() { return type; }
    /** Returns when this transaction was recorded. */
    public LocalDateTime getTimestamp() { return timestamp; }
    /** Returns the display description shown in the Finance panel. */
    public String getNote() { return note; }
}
