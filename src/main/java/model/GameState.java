package model;

/**
 * GameState is the root of the entire simulation.
 * It contains the Map (Game) and the Player (finances).
 */
public class GameState {
    private final Game gameMap;
    private final Player player;

    private double demandTimer = 0.0;

    public double getDemandTimer() {
        return demandTimer;
    }

    public void setDemandTimer(double t) {
        this.demandTimer = t;
    }

    public GameState(Game map, Player player) {
        this.gameMap = map;
        this.player = player;
    }

    /**
     * Returns the spatial simulation (the grid, vehicles, cities, roads).
     */
    public Game getMap() {
        return gameMap;
    }

    /**
     * Returns the player info (finances, name).
     */
    public Player getPlayer() {
        return player;
    }
}
