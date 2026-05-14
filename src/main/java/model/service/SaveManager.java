package model.service;

import model.Game;
import model.GameState;
import model.Player;
import model.Position;
import model.Road;
import model.Route;
import model.Stop;
import model.Tile;
import model.TrafficLight;
import model.Vehicle;
import model.enums.Direction;
import model.enums.TileType;
import model.enums.VehicleType;
import model.MapGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Persists and restores the full game state to/from a plain-text file.
 *
 * <p>The save file lives at {@code ~/.haulyea/savegame.txt}. The {@code slot} parameter
 * on all methods is accepted for API compatibility but currently ignored — there is
 * exactly one save slot. {@link controller.MainMenuController} calls {@link #delete(int)}
 * before generating a new world to ensure a clean state.
 *
 * <p>Load strategy: a fresh world is generated via {@link MapGenerator} with the saved
 * dimensions and city/facility counts, then roads, stops, routes, vehicles, and traffic
 * lights are replaced with the saved data. Tile types are restored row-by-row from the
 * {@code [tileTypes]} section.
 */
public class SaveManager {

    private static final Path SAVE_DIR = Path.of(System.getProperty("user.home"), ".haulyea");
    private static final Path SAVE_FILE = SAVE_DIR.resolve("savegame.txt");

    /**
     * Writes the current game state to {@code ~/.haulyea/savegame.txt}.
     *
     * @param state the game state to persist
     * @param slot  ignored (single-slot implementation)
     */
    public void save(GameState state, int slot) {
        Game game = state.getMap();
        Player player = state.getPlayer();

        StringBuilder content = new StringBuilder();
        content.append("# Haul Yea Save\n");
        content.append("savedAt=")
                .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .append('\n');

        // Finance data
        content.append("playerName=").append(player.getName()).append('\n');
        content.append("capital=").append(player.getLedger().getCurrentCapital()).append('\n');

        // World data
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

        content.append("routes=").append(game.getRoutes().size()).append('\n');
        content.append("[routeList]\n");
        for (Route route : game.getRoutes()) {
            String stopIds = route.getStops().stream()
                    .map(Stop::getId)
                    .collect(Collectors.joining(";"));

            String tilePath = route.getTilePath().stream()
                    .map(p -> p.getX() + ":" + p.getY())
                    .collect(Collectors.joining(";"));

            content.append(route.getId()).append('|')
                    .append(route.getName()).append('|')
                    .append(route.isReversed()).append('|')
                    .append(stopIds).append('|')
                    .append(tilePath)
                    .append('\n');
        }

        content.append("vehicles=").append(game.getVehicles().size()).append('\n');
        content.append("[vehicleList]\n");
        for (Vehicle vehicle : game.getVehicles()) {
            String routeId = vehicle.getRoute() != null ? vehicle.getRoute().getId() : "";

            content.append(vehicle.getId()).append('|')
                    .append(vehicle.getType()).append('|')
                    .append(vehicle.getPosition().getX()).append('|')
                    .append(vehicle.getPosition().getY()).append('|')
                    .append(routeId).append('|')
                    .append(vehicle.getRoutePathIndex()).append('|')
                    .append(vehicle.isMovingForward()).append('|')
                    .append(vehicle.getTravelDirection()).append('|')
                    .append(vehicle.getRouteProgress())
                    .append('\n');
        }

        content.append("trafficLights=").append(game.getTrafficLights().size()).append('\n');
        content.append("[trafficLightList]\n");
        for (TrafficLight tl : game.getTrafficLights().values()) {
            String directionData = tl.getActiveDirections().stream()
                    .map(d -> d.name() + ":" + tl.getGreenDuration(d))
                    .collect(Collectors.joining(";"));

            content.append(tl.getId()).append('|')
                    .append(tl.getPosition().getX()).append('|')
                    .append(tl.getPosition().getY()).append('|')
                    .append(directionData).append('|')
                    .append(tl.getCurrentPhaseIndex()).append('|')
                    .append(tl.getPhaseTimer())
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
     * Reads {@code ~/.haulyea/savegame.txt} and reconstructs a {@link GameState}.
     *
     * @param slot ignored (single-slot implementation)
     * @return the restored game state, or {@code null} if the file doesn't exist or is invalid
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

            String playerName = "Player 1";
            double capital = 100000;

            String worldName = null;
            int width = 0, height = 0;
            List<Road> roads = new ArrayList<>();
            List<Stop> stops = new ArrayList<>();
            TileType[][] tileTypes = null;

            record ParsedRoute(String id, String name, boolean reversed, List<String> stopIds, List<Position> tilePath) {}
            record ParsedVehicle(String id, VehicleType type, int x, int y, String routeId, int routePathIndex,
                                 boolean movingForward, Direction travelDirection, double routeProgress) {}
            record ParsedTrafficLight(String id, int x, int y, List<Direction> directions,
                                      Map<Direction, Double> durations, int phaseIndex, double phaseTimer) {}

            List<ParsedRoute> parsedRoutes = new ArrayList<>();
            List<ParsedVehicle> parsedVehicles = new ArrayList<>();
            List<ParsedTrafficLight> parsedTrafficLights = new ArrayList<>();

            int section = 0;
            int roadCount = 0, stopCount = 0, routeCount = 0, vehicleCount = 0, trafficLightCount = 0;
            int y = 0;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("playerName=")) {
                    playerName = line.substring(11);
                } else if (line.startsWith("capital=")) {
                    capital = Double.parseDouble(line.substring(8));
                } else if (line.startsWith("worldName=")) {
                    worldName = line.substring(10);
                } else if (line.startsWith("width=")) {
                    width = Integer.parseInt(line.substring(6));
                } else if (line.startsWith("height=")) {
                    height = Integer.parseInt(line.substring(7));
                } else if (line.startsWith("roads=")) {
                    roadCount = Integer.parseInt(line.substring(6));
                } else if (line.startsWith("stops=")) {
                    stopCount = Integer.parseInt(line.substring(6));
                } else if (line.startsWith("routes=")) {
                    routeCount = Integer.parseInt(line.substring(7));
                } else if (line.startsWith("vehicles=")) {
                    vehicleCount = Integer.parseInt(line.substring(9));
                } else if (line.startsWith("trafficLights=")) {
                    trafficLightCount = Integer.parseInt(line.substring(14));
                } else if (line.equals("[roadList]")) {
                    section = 1;
                } else if (line.equals("[stopList]")) {
                    section = 2;
                } else if (line.equals("[routeList]")) {
                    section = 3;
                } else if (line.equals("[vehicleList]")) {
                    section = 4;
                } else if (line.equals("[trafficLightList]")) {
                    section = 5;
                } else if (line.equals("[tileTypes]")) {
                    section = 6;
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
                    } else if (section == 3 && parsedRoutes.size() < routeCount) {
                        String[] parts = line.split("\\|", -1);
                        if (parts.length == 5) {
                            List<String> stopIds = parts[3].isBlank()
                                    ? new ArrayList<>()
                                    : List.of(parts[3].split(";"));

                            List<Position> tilePath = new ArrayList<>();
                            if (!parts[4].isBlank()) {
                                for (String token : parts[4].split(";")) {
                                    String[] xy = token.split(":");
                                    tilePath.add(new Position(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
                                }
                            }

                            parsedRoutes.add(new ParsedRoute(
                                    parts[0],
                                    parts[1],
                                    Boolean.parseBoolean(parts[2]),
                                    stopIds,
                                    tilePath
                            ));
                        }
                    } else if (section == 4 && parsedVehicles.size() < vehicleCount) {
                        String[] parts = line.split("\\|", -1);
                        if (parts.length >= 8) {
                            double routeProgress = parts.length >= 9 ? Double.parseDouble(parts[8]) : 0.0;
                            parsedVehicles.add(new ParsedVehicle(
                                    parts[0],
                                    VehicleType.valueOf(parts[1]),
                                    Integer.parseInt(parts[2]),
                                    Integer.parseInt(parts[3]),
                                    parts[4],
                                    Integer.parseInt(parts[5]),
                                    Boolean.parseBoolean(parts[6]),
                                    Direction.valueOf(parts[7]),
                                    routeProgress
                            ));
                        }
                    } else if (section == 5 && parsedTrafficLights.size() < trafficLightCount) {
                        String[] parts = line.split("\\|", -1);
                        if (parts.length == 6) {
                            List<Direction> directions = new ArrayList<>();
                            Map<Direction, Double> durations = new HashMap<>();

                            if (!parts[3].isBlank()) {
                                for (String token : parts[3].split(";")) {
                                    String[] kv = token.split(":");
                                    Direction dir = Direction.valueOf(kv[0]);
                                    double duration = Double.parseDouble(kv[1]);
                                    directions.add(dir);
                                    durations.put(dir, duration);
                                }
                            }

                            parsedTrafficLights.add(new ParsedTrafficLight(
                                    parts[0],
                                    Integer.parseInt(parts[1]),
                                    Integer.parseInt(parts[2]),
                                    directions,
                                    durations,
                                    Integer.parseInt(parts[4]),
                                    Double.parseDouble(parts[5])
                            ));
                        }
                    } else if (section == 6 && y < height) {
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

            MapGenerator generator = new MapGenerator();
            Game game = generator.generate(width, height, 7, 15);
            game.setWorldName(worldName);

            game.getRoads().clear();
            game.getStops().clear();
            game.getVehicles().clear();
            game.getRoutes().clear();
            game.getTrafficLights().clear();

            for (int yy = 0; yy < height; yy++) {
                for (int xx = 0; xx < width; xx++) {
                    Tile tile = game.getTile(xx, yy);
                    if (tile != null && tileTypes[yy][xx] != null) {
                        tile.setType(tileTypes[yy][xx]);
                    }
                }
            }

            for (Road road : roads) {
                game.getRoads().add(road);
            }

            Map<String, Stop> stopById = new HashMap<>();
            for (Stop stop : stops) {
                game.getStops().add(stop);
                stopById.put(stop.getId(), stop);
            }

            Map<String, Route> routeById = new HashMap<>();
            for (ParsedRoute parsedRoute : parsedRoutes) {
                List<Stop> routeStops = parsedRoute.stopIds().stream()
                        .map(stopById::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                Route route = new Route(
                        parsedRoute.id(),
                        parsedRoute.name(),
                        routeStops,
                        parsedRoute.tilePath()
                );
                route.setReversed(parsedRoute.reversed());
                game.addRoute(route);
                routeById.put(route.getId(), route);
            }

            for (ParsedVehicle parsedVehicle : parsedVehicles) {
                Vehicle vehicle = new Vehicle(
                        parsedVehicle.id(),
                        parsedVehicle.type(),
                        new Position(parsedVehicle.x(), parsedVehicle.y())
                );

                if (!parsedVehicle.routeId().isBlank()) {
                    Route route = routeById.get(parsedVehicle.routeId());
                    if (route != null) {
                        vehicle.assignRoute(route);
                        vehicle.restoreRouteState(parsedVehicle.routePathIndex(), parsedVehicle.movingForward());
                    }
                }

                vehicle.setPosition(new Position(parsedVehicle.x(), parsedVehicle.y()));
                vehicle.setSmoothPosition(parsedVehicle.x(), parsedVehicle.y());
                vehicle.setTravelDirection(parsedVehicle.travelDirection());
                vehicle.setRouteProgress(parsedVehicle.routeProgress());
                game.addVehicle(vehicle);
            }

            for (ParsedTrafficLight parsedTrafficLight : parsedTrafficLights) {
                TrafficLight tl = new TrafficLight(
                        parsedTrafficLight.id(),
                        new Position(parsedTrafficLight.x(), parsedTrafficLight.y()),
                        parsedTrafficLight.directions()
                );

                for (Map.Entry<Direction, Double> entry : parsedTrafficLight.durations().entrySet()) {
                    tl.setGreenDuration(entry.getKey(), entry.getValue());
                }

                tl.restoreState(parsedTrafficLight.phaseIndex(), parsedTrafficLight.phaseTimer());
                game.addTrafficLight(tl);
            }

            Player player = new Player(playerName, capital);
            GameState state = new GameState(game, player);

            System.out.println("[SaveManager] Game loaded: " + SAVE_FILE);
            return state;

        } catch (Exception ex) {
            System.err.println("[SaveManager] Failed to load game: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Returns {@code true} if a save file exists at the default path.
     *
     * @param slot ignored (single-slot implementation)
     */
    public boolean saveExists() {
        return Files.exists(SAVE_FILE);
    }

    /**
     * Deletes the save file if it exists.
     *
     * @param slot ignored (single-slot implementation)
     */
    public void delete(int slot) {
        try {
            Path file = SAVE_FILE;
            if (Files.exists(file)) {
                Files.delete(file);
                System.out.println("[SaveManager] Save file deleted: " + file);
            } else {
                System.out.println("[SaveManager] Save file does not exist: " + file);
            }
        } catch (IOException ex) {
            System.err.println("[SaveManager] Failed to delete save file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
