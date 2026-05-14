package view;
import javafx.application.Application;
import javafx.stage.Stage;
import controller.MainMenuController;

/**
 * JavaFX entry point for Haul Yea!
 *
 * <p>Bootstraps the application by starting background music and handing the
 * {@link Stage} to {@link MainMenuController}. Contains no game logic — all
 * navigation lives in the controllers and all UI layout lives in the view classes.
 */
public class App extends Application {

    /**
     * Called by the JavaFX runtime after the stage is ready.
     * Starts background music and shows the main menu.
     *
     * @param stage the primary stage provided by the JavaFX runtime
     */
    @Override
    public void start(Stage stage) {
        BackgroundMusic.playLoop("/audio/bgm.mp3", 0.35);
        MainMenuController controller = new MainMenuController(stage);
        controller.showMainMenu();
    }

    /** Called when the application is closing; stops and disposes the music player. */
    @Override
    public void stop() {
        BackgroundMusic.stop();
    }

    /**
     * Standard Java entry point — delegates to {@link Application#launch(String...)}.
     *
     * @param args command-line arguments (passed through to JavaFX, currently unused)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
