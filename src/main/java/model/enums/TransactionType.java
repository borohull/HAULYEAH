package model.enums;

/**
 * Category label for entries in the {@link model.FinanceLedger}.
 * Used by the Finance panel to filter and group transactions.
 */
public enum TransactionType {
    /** Money spent constructing roads, stops, bridges, or traffic lights. */
    BUILD,
    /** Money spent buying a vehicle. */
    PURCHASE,
    /** Ongoing per-vehicle maintenance cost (aggregated, not listed individually). */
    MAINTENANCE,
    /** Money earned by completing a cargo or passenger delivery. */
    DELIVERY,
    /** General income entry (not delivery-specific). */
    INCOME,
    /** Catch-all for entries that don't fit another category. */
    OTHER
}
