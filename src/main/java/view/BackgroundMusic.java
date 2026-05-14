package view;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Utility class for app-wide looping background music.
 *
 * <p>Only one track can play at a time. A second call to {@link #playLoop} while music
 * is already playing is a no-op. Call {@link #stop()} before switching tracks.
 */
public final class BackgroundMusic {

    private static MediaPlayer player;

    private BackgroundMusic() {
    }

    /**
     * Starts looping the audio resource at the given classpath path.
     * Does nothing if music is already playing.
     *
     * @param resourcePath classpath-relative path to the audio file (e.g. {@code "/audio/bgm.mp3"})
     * @param volume       playback volume in the range [0.0, 1.0]; clamped automatically
     */
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

    /** Stops the current track and releases the {@link MediaPlayer} resource. */
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

