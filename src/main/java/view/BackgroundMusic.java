package view;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Simple background music controller for app-wide looping music.
 */
public final class BackgroundMusic {

    private static MediaPlayer player;

    private BackgroundMusic() {
    }

    public static void playLoop(String resourcePath, double volume) {
        if (player != null) {
            return;
        }

        try {
            var url = BackgroundMusic.class.getResource(resourcePath);
            if (url == null) {
                System.err.println("[BackgroundMusic] Resource not found: " + resourcePath);
                return;
            }

            Media media = new Media(url.toExternalForm());
            player = new MediaPlayer(media);
            player.setCycleCount(MediaPlayer.INDEFINITE);
            player.setVolume(clampVolume(volume));
            player.play();
        } catch (Exception ex) {
            System.err.println("[BackgroundMusic] Failed to start: " + ex.getMessage());
        }
    }

    public static void stop() {
        if (player == null) {
            return;
        }
        player.stop();
        player.dispose();
        player = null;
    }

    private static double clampVolume(double volume) {
        if (volume < 0.0) return 0.0;
        if (volume > 1.0) return 1.0;
        return volume;
    }
}

