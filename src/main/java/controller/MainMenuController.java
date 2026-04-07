package controller;

import javafx.stage.Stage;

/**
 * MainMenuController
 *
 * Responsible for all main-menu actions:
 *   - Starting a new game
 *   - Loading a saved game
 *   - Deleting a save slot
 *   - Exiting the application
 *
 * The controller owns the Stage reference so it can swap scenes.
 * It must NOT contain any JavaFX layout or drawing code.
 */
public class MainMenuController {

    private final Stage stage;

    public MainMenuController(Stage stage) {
        this.stage = stage;
    }

    /**
     * Called when the player clicks "New Game".
     * Shows the new-game setup screen (world name input, map size, etc.).
     */
    public void newGame() {
        // TODO: create MainMenuView, call view.showNewGame(stage, this)
        System.out.println("[MainMenuController] newGame()");
    }

    /**
     * Called when the player selects a save slot to load.
     *
     * @param slot save-slot index (0-based)
     */
    public void loadGame(int slot) {
        // TODO: call SaveManager.load(slot), build GameController + GameWindow
        System.out.println("[MainMenuController] loadGame(slot=" + slot + ")");
    }

    /**
     * Called when the player confirms deleting a save slot.
     *
     * @param slot save-slot index (0-based)
     */
    public void deleteSave(int slot) {
        // TODO: call SaveManager.delete(slot), refresh the menu view
        System.out.println("[MainMenuController] deleteSave(slot=" + slot + ")");
    }

    /**
     * Closes the application.
     */
    public void exit() {
        stage.close();
    }

    /** Convenience accessor so Views can get the Stage for scene switching. */
    public Stage getStage() {
        return stage;
    }
}
