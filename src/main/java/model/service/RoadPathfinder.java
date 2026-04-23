package model.service;

import model.Game;
import model.Position;
import model.Tile;
import model.enums.TileType;

import java.util.*;

/**
 * BFS pathfinder that finds the shortest walkable road path between two positions.
 * Returns null if no connected road path exists.
 */
public class RoadPathfinder {

    private static final int[][] DELTAS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

    /**
     * Finds the shortest road path from {@code from} to {@code to}.
     * @return ordered list of positions (inclusive of both endpoints), or null if unreachable
     */
    public static List<Position> findPath(Game game, Position from, Position to) {
        if (from.equals(to)) return List.of(from);

        Map<Position, Position> parent = new HashMap<>();
        Queue<Position> queue = new LinkedList<>();
        queue.add(from);
        parent.put(from, null);

        while (!queue.isEmpty()) {
            Position cur = queue.poll();
            if (cur.equals(to)) {
                return reconstructPath(parent, to);
            }
            for (int[] d : DELTAS) {
                Position next = new Position(cur.getX() + d[0], cur.getY() + d[1]);
                if (parent.containsKey(next)) continue;
                Tile tile = game.getTile(next.getX(), next.getY());
                if (tile == null || !isWalkable(tile.getType())) continue;
                parent.put(next, cur);
                queue.add(next);
            }
        }
        return null;
    }

    private static List<Position> reconstructPath(Map<Position, Position> parent, Position to) {
        List<Position> path = new ArrayList<>();
        for (Position cur = to; cur != null; cur = parent.get(cur)) {
            path.add(cur);
        }
        Collections.reverse(path);
        return path;
    }

    private static boolean isWalkable(TileType t) {
        return t == TileType.ROAD || t == TileType.STOP
            || t == TileType.CITY_ROAD || t == TileType.CITY_STOP
            || t == TileType.BRIDGE;
    }
}
