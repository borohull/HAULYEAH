package model.service;

import model.Game;
import model.GameState;
import model.Player;
import model.Road;
import model.Stop;
import model.Tile;
import model.enums.TileType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
     * @param state the game state to save
     * @param slot save slot index (reserved for future multi-save support)
     */
    public void save(GameState state, int slot) {
        model.Game game = state.getMap();
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
     * Loads a game from the given slot.
     * @param slot save slot index
     * @return the loaded GameState, or null if it failed
     */
    public GameState load(int slot) {
        try {
            Path file = SAVE_FILE;
            if (!Files.exists(file)) {
                System.err.println("[SaveManager] Save file does not exist: " + file);
                return null;
            }

            List<String> lines = Files.readAllLines(file);
            if (lines.isEmpty()) {
                System.err.println("[SaveManager] Save file is empty");
                return null;
            }

            String worldName = null;
            int width = 0, height = 0;
            List<Road> roads = new ArrayList<>();
            List<Stop> stops = new ArrayList<>();
            TileType[][] tileTypes = null;

            int section = 0; // 0: header, 1: roadList, 2: stopList, 3: tileTypes
            int roadCount = 0, stopCount = 0;
            int y = 0;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("worldName=")) {
                    worldName = line.substring(10);
                } else if (line.startsWith("width=")) {
                    width = Integer.parseInt(line.substring(6));
                } else if (line.startsWith("height=")) {
                    height = Integer.parseInt(line.substring(7));
                } else if (line.startsWith("roads=")) {
                    roadCount = Integer.parseInt(line.substring(6));
                } else if (line.startsWith("stops=")) {
                    stopCount = Integer.parseInt(line.substring(6));
                } else if (line.equals("[roadList]")) {
                    section = 1;
                } else if (line.equals("[stopList]")) {
                    section = 2;
                } else if (line.equals("[tileTypes]")) {
                    section = 3;
                    tileTypes = new TileType[height][width];
                } else {
                    if (section == 1 && roads.size() < roadCount) {
                        String[] parts = line.split(",");
                        if (parts.length == 4) {
                            String id = parts[0];
                            int rx = Integer.parseInt(parts[1]);
                            int ry = Integer.parseInt(parts[2]);
                            Road.RoadType type = Road.RoadType.valueOf(parts[3]);
                            roads.add(new Road(id, rx, ry, type));
                        }
                    } else if (section == 2 && stops.size() < stopCount) {
                        String[] parts = line.split(",");
                        if (parts.length == 4) {
                            String id = parts[0];
                            int sx = Integer.parseInt(parts[1]);
                            int sy = Integer.parseInt(parts[2]);
                            String name = parts[3];
                            stops.add(new Stop(id, sx, sy, name));
                        }
                    } else if (section == 3 && y < height) {
                        String[] types = line.split(",");
                        for (int x = 0; x < width && x < types.length; x++) {
                            tileTypes[y][x] = TileType.valueOf(types[x]);
                        }
                        y++;
                    }
                }
            }

            if (worldName == null || width == 0 || height == 0 || tileTypes == null) {
                System.err.println("[SaveManager] Invalid save file format");
                return null;
            }

            // Create Game
            Game game = new Game(width, height);
            game.setWorldName(worldName);

            // Set tile types
            for (int yy = 0; yy < height; yy++) {
                for (int xx = 0; xx < width; xx++) {
                    Tile tile = game.getTile(xx, yy);
                    if (tile != null && tileTypes[yy][xx] != null) {
                        tile.setType(tileTypes[yy][xx]);
                    }
                }
            }

            // Add roads
            for (Road road : roads) {
                game.getRoads().add(road);
            }

            // Add stops
            for (Stop stop : stops) {
                game.getStops().add(stop);
            }

            // Create GameState with default Player
            Player player = new Player("Player 1", 100000);
            GameState state = new GameState(game, player);

            System.out.println("[SaveManager] Game loaded: " + SAVE_FILE);
            return state;

        } catch (Exception ex) {
            System.err.println("[SaveManager] Failed to load game: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}
