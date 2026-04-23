package controller;

import model.GameState;
import model.service.SimulationEngine;
import javafx.animation.AnimationTimer;
import javafx.stage.Window;
import view.PopupTheme;
/**
 * SimulationController
 *
 * Manages the simulation lifecycle:
 *   - Play / Pause
 *   - Speed control (1×, 2×, 4×)
 *   - Saving the current game to a slot
 *
 * In a future issue this controller will drive a JavaFX AnimationTimer
 * that calls SimulationEngine.tick(state, dt) on every frame.
 *
 * It fires an onStateChanged callback after each action so the HUD
 * (speed indicator, pause icon) can refresh without the controller
 * touching JavaFX directly.
 */
public class SimulationController {

    /** Matches the TimeSpeed enum in the UML (currently stored in GameSpeed). */
    public enum Speed {
        PAUSED(0, "⏸"),
        X1(1,    "▶  1×"),
        X2(2,    "▶▶ 2×"),
        X4(4,    "▶▶▶ 4×");

        public final int multiplier;
        public final String label;

        Speed(int multiplier, String label) {
            this.multiplier = multiplier;
            this.label = label;
        }

        /** Cycles: PAUSED → X1 → X2 → X4 → PAUSED */
        public Speed next() {
            return switch (this) {
                case PAUSED -> X1;
                case X1     -> X2;
                case X2     -> X4;
                case X4     -> PAUSED;
            };
        }
    }

    private final GameState state;
    private Speed      speed   = Speed.PAUSED;
    private boolean    running = false;
    private boolean hasUnsavedChanges = false;

    private final SimulationEngine engine = new SimulationEngine();
    private AnimationTimer timer;
    private long lastTime = 0;

    // Registered by the View (HudPanel) to refresh speed/pause display
    private Runnable onStateChanged;
    private Runnable onBankrupt;
    private Window dialogOwner;

    public SimulationController(GameState state) {
        this.state = state;
        
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                
                // Elapsed physical time in seconds
                double elapsedSeconds = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (speed != Speed.PAUSED) {
                    double dt = elapsedSeconds * speed.multiplier;
                    engine.tick(SimulationController.this.state, dt);
                    notifyView();

                    if (state.getPlayer().getLedger().getCurrentCapital() < 0) {
                        pause();
                        if (onBankrupt != null) onBankrupt.run();
                    }
                }
            }
        };
    }

    public void setOnStateChanged(Runnable callback) { this.onStateChanged = callback; }
    public void setOnBankrupt(Runnable callback)      { this.onBankrupt = callback; }
    public void setDialogOwner(Window owner)          { this.dialogOwner = owner; }

    // -----------------------------------------------------------------------
    // Simulation control
    // -----------------------------------------------------------------------

    /**
     * Starts (or resumes) the simulation.
     * If currently paused, switches to 1× speed.
     */
    public void play() {
        if (speed == Speed.PAUSED) {
            speed = Speed.X1;
        }
        running = true;
        System.out.println("[SimulationController] play() — " + speed.label);
        lastTime = 0;
        timer.start();
        notifyView();
    }

    /**
     * Pauses the simulation.
     */
    public void pause() {
        running = false;
        speed   = Speed.PAUSED;
        System.out.println("[SimulationController] pause()");
        timer.stop();
        notifyView();
    }

    /**
     * Cycles through PAUSED → 1× → 2× → 4× → PAUSED.
     * Convenient for a single speed-toggle button.
     */
    public void cycleSpeed() {
        speed   = speed.next();
        running = speed != Speed.PAUSED;
        System.out.println("[SimulationController] speed -> " + speed.label);
        
        if (running) {
            lastTime = 0;
            timer.start();
        } else {
            timer.stop();
        }
        
        notifyView();
    }

    /**
     * Sets a specific speed directly.
     *
     * @param newSpeed desired speed (use Speed.PAUSED to pause)
     */
    public void setSpeed(Speed newSpeed) {
        this.speed   = newSpeed;
        this.running = newSpeed != Speed.PAUSED;
        System.out.println("[SimulationController] setSpeed -> " + newSpeed.label);
        
        if (running) {
            lastTime = 0;
            timer.start();
        } else {
            timer.stop();
        }
        
        notifyView();
    }

    // -----------------------------------------------------------------------
    // Save
    // -----------------------------------------------------------------------

    /**
     * Saves the game to the given slot.
     *
     * @param slot save-slot index (0-based)
     */
    public void saveGame(int slot) {
        new model.service.SaveManager().save(state, slot);
        clearUnsavedChanges();
        // Show success dialogue
        javafx.scene.control.Alert alert = PopupTheme.createAlert(
                dialogOwner,
                javafx.scene.control.Alert.AlertType.INFORMATION,
                "Save Game",
                null,
                "Game saved successfully!");
        PopupTheme.showAndWait(alert, dialogOwner);

    }

    /**
     * Marks that there are unsaved changes.
     */
    public void markUnsavedChanges() {
        hasUnsavedChanges = true;
    }

    /**
     * Checks if there are unsaved changes.
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /**
     * Clears the unsaved changes flag (called after saving).
     */
    public void clearUnsavedChanges() {
        hasUnsavedChanges = false;
    }

    // -----------------------------------------------------------------------
    // Getters for the HUD
    // -----------------------------------------------------------------------

    public boolean isRunning() { return running; }
    public Speed   getSpeed()  { return speed; }

    /** Clears saved tile-progress for a vehicle so it restarts cleanly. */
    public void resetVehicle(String vehicleId) {
        engine.resetVehicle(vehicleId);
    }

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    private void notifyView() {
        if (onStateChanged != null) {
            onStateChanged.run();
        }
    }
}
