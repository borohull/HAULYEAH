package model;

/**
 * Represents the human player: a name and a {@link FinanceLedger}.
 *
 * <p>Deliberately thin — all game-world state lives in {@link model.Game}.
 * {@code Player} only tracks identity and finances so that save/load can
 * restore both independently.
 */
public class Player {
    /** Player's display name, settable during gameplay. */
    private String name;
    /** Ledger tracking all income, expenditure, and maintenance. */
    private final FinanceLedger ledger;

    /**
     * Creates a new player.
     *
     * @param name            display name shown in the HUD
     * @param startingCapital initial cash balance
     */
    public Player(String name, double startingCapital) {
        this.name = name;
        this.ledger = new FinanceLedger(startingCapital);
    }

    /** Returns the player's display name. */
    public String getName() {
        return name;
    }

    /** Updates the player's display name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the ledger that tracks all financial transactions. */
    public FinanceLedger getLedger() {
        return ledger;
    }
}
