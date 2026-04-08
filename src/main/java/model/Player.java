package model;

/**
 * Player holds the user's specific data separated from the world map.
 */
public class Player {
    private String name;
    private final FinanceLedger ledger;

    public Player(String name, double startingCapital) {
        this.name = name;
        this.ledger = new FinanceLedger(startingCapital);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FinanceLedger getLedger() {
        return ledger;
    }
}
