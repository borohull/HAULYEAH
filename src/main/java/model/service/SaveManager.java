package model.service;

import model.Game;
import model.Road;
import model.Stop;
import model.Tile;
import model.enums.TileType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SaveManager — handles persisting and loading the state of the game.
 * Extracted from the original MapDemo class.
 */
public class SaveManager {

    private static final Path SAVE_DIR = Path.of(System.getProperty("user.home"), ".haulyea");
    private static final Path SAVE_FILE = SAVE_DIR.resolve("savegame.txt");

    /**
     * Saves the provided Game state to ~/.haulyea/savegame.txt.
     * Currently ignores the slot parameter to stick to the default behavior.
     *
     * @param game the game state to save
     * @param slot save slot index (reserved for future multi-save support)
     */
    public void save(Game game, int slot) {
        StringBuilder content = new StringBuilder();
        content.append("# Haul Yea Save\n");
        content.append("savedAt=")
                .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .append('\n');
        content.append("worldName=").append(game.getWorldName()).append('\n');
        content.append("width=").append(game.getWidth()).append('\n');
        content.append("height=").append(game.getHeight()).append('\n');
        content.append("roads=").append(game.getRoads().size()).append('\n');

        content.append("[roadList]\n");
        for (Road road : game.getRoads()) {
            content.append(road.getId()).append(',')
                    .append(road.getPosition().getX()).append(',')
                    .append(road.getPosition().getY()).append(',')
                    .append(road.getType())
                    .append('\n');
        }

        content.append("stops=").append(game.getStops().size()).append('\n');
        content.append("[stopList]\n");
        for (Stop stop : game.getStops()) {
            content.append(stop.getId()).append(',')
                    .append(stop.getPosition().getX()).append(',')
                    .append(stop.getPosition().getY()).append(',')
                    .append(stop.getName())
                    .append('\n');
        }

        content.append("[tileTypes]\n");
        for (int y = 0; y < game.getHeight(); y++) {
            for (int x = 0; x < game.getWidth(); x++) {
                Tile tile = game.getTile(x, y);
                TileType type = tile == null ? TileType.EMPTY : tile.getType();
                content.append(type.name());
                if (x < game.getWidth() - 1) {
                    content.append(',');
                }
            }
            content.append('\n');
        }

        try {
            Files.createDirectories(SAVE_DIR);
            Files.writeString(SAVE_FILE, content);
            System.out.println("[SaveManager] Game saved: " + SAVE_FILE);
        } catch (IOException ex) {
            System.err.println("[SaveManager] Failed to save game: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Stub for loading a game.
     * @param slot save slot index
     * @return the loaded Game, or null if it failed
     */
    public Game load(int slot) {
        System.out.println("[SaveManager] load() not yet implemented");
        return null;
    }
}
