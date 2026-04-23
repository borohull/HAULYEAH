package view;
import javafx.application.Application;
import javafx.stage.Stage;
import controller.MainMenuController;

/**
 * App — JavaFX entry point for Haul Yea!
 * This class only bootstraps the application.
 * All navigation logic lives in MainMenuController.
 * All layout/UI code lives in the View classes.
 */

public class App extends Application {
    @Override
    public void start(Stage stage) {
        BackgroundMusic.playLoop("/audio/bgm.mp3", 0.35);
        MainMenuController controller = new MainMenuController(stage);
        controller.showMainMenu();
    }

    @Override
    public void stop() {
        BackgroundMusic.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
