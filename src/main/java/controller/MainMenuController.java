package controller;

import javafx.stage.Stage;
import model.Game;
import model.MapGenerator;
import model.Player;
import model.GameState;
import view.GameWindow;
import view.MainMenuView;

/**
 * MainMenuController
 *
 * Responsible for all main-menu navigation:
 *   - Showing the main menu
 *   - Starting a new game
 *   - Loading a saved game
 *   - Deleting a save slot
 *   - Exiting the application
 *
 * The controller creates View instances and tells them to show themselves.
 * It must NOT contain any JavaFX layout or drawing code.
 */
public class MainMenuController {

    private final Stage stage;

    public MainMenuController(Stage stage) {
        this.stage = stage;
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    /**
     * Shows the main menu screen.
     * Called by App.start() and whenever we need to return to the menu.
     */
    public void showMainMenu() {
        MainMenuView view = new MainMenuView(stage, this);
        view.show();
    }

    /**
     * Shows the new-game setup screen (world-name input).
     * Called when the player clicks "New Game".
     */
    public void newGame() {
        // Check if a save file exists first
        model.service.SaveManager saveManager = new model.service.SaveManager();
        if (saveManager.saveExists()) {
            // Show warning that existing save will be lost
            MainMenuView view = new MainMenuView(stage, this);
            view.showConfirmNewGameWarning();
        } else {
            // No existing save, go directly to world name input
            showNewGameInput();
        }
    }

    /**
     * Shows the world name input screen.
     * Called after confirming that new game will be started (or no existing save exists).
     */
    public void showNewGameInput() {
        MainMenuView view = new MainMenuView(stage, this);
        view.showNewGame();
    }

    /**
     * Called when the player types a world name and clicks "Start".
     * Creates a fresh game and launches the game window.
     *
     * @param worldName the name the player entered
     */
    public void startGame(String worldName) {
        // Build the model
        MapGenerator generator = new MapGenerator();
        Game game = generator.generate(55, 55, 4, 5);
        game.setWorldName(worldName);

        // Build the controllers
        Player player = new Player("Player 1", 100000);
        GameState state = new GameState(game, player);
        GameController       gameController = new GameController(state);
        SimulationController simController  = new SimulationController(state);

        // Build and show the game window (View)
        GameWindow gameWindow = new GameWindow(stage, gameController, simController);
        gameWindow.show();
    }

    /**
     * Starts the game from a loaded GameState.
     *
     * @param state the loaded game state
     */
    public void startGameFromState(GameState state) {
        // Build the controllers
        GameController       gameController = new GameController(state);
        SimulationController simController  = new SimulationController(state);

        // Build and show the game window (View)
        GameWindow gameWindow = new GameWindow(stage, gameController, simController);
        gameWindow.show();
    }

    /**
     * Shows the load-game screen.
     * Called when the player clicks "Load Game".
     *
     * @param slot save-slot index (0-based)
     */
    public void loadGame(int slot) {
        // Load the game state
        model.service.SaveManager saveManager = new model.service.SaveManager();
        GameState loadedState = saveManager.load(slot);
        if (loadedState != null) {
            startGameFromState(loadedState);
        } else {
            // Show error message
            MainMenuView view = new MainMenuView(stage, this);
            view.showLoadError();
        }
    }

    /**
     * Shows the delete-save confirmation screen.
     * Called when the player clicks "Delete Game".
     *
     * @param slot save-slot index (0-based)
     */
    public void deleteSave(int slot) {
        MainMenuView view = new MainMenuView(stage, this);
        view.showDeleteConfirm(slot);
    }

    /**
     * Called when the player confirms deletion.
     * Deletes the save and returns to the main menu.
     *
     * @param slot save-slot index (0-based)
     */
    public void confirmDeleteSave(int slot) {
        // TODO: call SaveManager.delete(slot)
        System.out.println("[MainMenuController] confirmDeleteSave(slot=" + slot + ") — save deleted");
        showMainMenu();
    }

    /**
     * Shows the confirmation screen when starting a new game with an existing save.
     * Called when the player confirms they want to start a new game.
     *
     * @param worldName the name the player entered
     */
    public void confirmStartNewGame(String worldName) {
        model.service.SaveManager saveManager = new model.service.SaveManager();
        if (saveManager.saveExists()) {
            // Show confirmation that existing save will be lost
            MainMenuView view = new MainMenuView(stage, this);
            view.showConfirmNewGame(worldName);
        } else {
            // No existing save, start directly
            startGame(worldName);
        }
    }

    /**
     * Closes the application.
     */
    public void exit() {
        stage.close();
    }

    /** Gives Views access to the Stage for any direct scene operations. */
    public Stage getStage() {
        return stage;
    }
}
