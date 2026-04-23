package model;

import model.enums.TransactionType;
import java.time.LocalDateTime;

/**
 * Transaction represents a single financial action in the game.
 */
public class Transaction {
    private final double amount;
    private final TransactionType type;
    private final LocalDateTime timestamp;
    private final String note;

    public Transaction(double amount, TransactionType type, LocalDateTime timestamp, String note) {
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.note = note;
    }

    public Transaction(double amount, TransactionType type, String note) {
        this(amount, type, LocalDateTime.now(), note);
    }


    public double getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getNote() { return note; }
}
