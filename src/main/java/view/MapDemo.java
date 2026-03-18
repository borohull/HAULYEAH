package view;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import model.Game;
import model.MapGenerator;

public class MapDemo extends Application {

    @Override
    public void start(Stage stage) {
        MapGenerator generator = new MapGenerator();
        Game game = generator.generate(30, 30, 4, 5);

        MapPanel mapPanel = new MapPanel();
        mapPanel.drawGame(game);

        ScrollPane scroll = new ScrollPane(mapPanel);
        scroll.setPannable(true);

        Scene scene = new Scene(scroll, 800, 600);
        stage.setTitle("Haul Yea! — Map Preview");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
