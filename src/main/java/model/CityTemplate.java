package model;

public class CityTemplate {
    private final String name;
    private final int width;
    private final int height;
    private final char[][] layout;

    public CityTemplate(String name, String... rows) {
        if (rows == null || rows.length == 0) {
            throw new IllegalArgumentException("Template rows cannot be empty");
        }

        this.name = name;
        this.height = rows.length;
        this.width = rows[0].length();
        this.layout = new char[width][height];

        for (int y = 0; y < rows.length; y++) {
            if (rows[y].length() != width) {
                throw new IllegalArgumentException("All template rows must have the same width");
            }

            for (int x = 0; x < width; x++) {
                char c = rows[y].charAt(x);
                if (c != 'B' && c != 'H' && c != 'V' && c != 'X' && c != '.') {
                    throw new IllegalArgumentException("Supported symbols: B, H, V, X, .");
                }
                layout[x][y] = c;
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public char getTileSymbol(int x, int y) {
        return layout[x][y];
    }

    public boolean isBuilding(int x, int y) {
        return layout[x][y] == 'B';
    }

    public boolean isRoad(int x, int y) {
        char c = layout[x][y];
        return c == 'H' || c == 'V' || c == 'X';
    }

    public boolean isHorizontalRoad(int x, int y) {
        return layout[x][y] == 'H';
    }

    public boolean isVerticalRoad(int x, int y) {
        return layout[x][y] == 'V';
    }

    public boolean isCrossroad(int x, int y) {
        return layout[x][y] == 'X';
    }

    public boolean isEmpty(int x, int y) {
        return layout[x][y] == '.';
    }
}