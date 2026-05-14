package model;

/**
 * A blueprint for a city layout, expressed as a grid of single-character symbols.
 *
 * <p>Symbols:
 * <ul>
 *   <li>{@code B} — building tile</li>
 *   <li>{@code H} — horizontal internal road</li>
 *   <li>{@code V} — vertical internal road</li>
 *   <li>{@code X} — crossroad intersection</li>
 *   <li>{@code .} — empty (undeveloped) tile</li>
 * </ul>
 *
 * <p>Templates are defined once inside {@link MapGenerator#initializeCityTemplates()} and
 * consumed by {@link MapGenerator#createCityFromTemplate} to stamp a {@link City} at
 * a specific world position.
 */
public class CityTemplate {
    /** City name that will be assigned to instances created from this template. */
    private final String name;
    /** Width of the template grid in tiles. */
    private final int width;
    /** Height of the template grid in tiles. */
    private final int height;
    /** 2-D character grid indexed as {@code layout[x][y]}. */
    private final char[][] layout;

    /**
     * Creates a template from an array of row strings.
     * All rows must have the same length. Each character must be one of
     * {@code B}, {@code H}, {@code V}, {@code X}, or {@code .}.
     *
     * @param name display name of the city
     * @param rows one string per row, left-to-right
     * @throws IllegalArgumentException if rows are empty, have unequal lengths, or contain
     *                                  unsupported symbols
     */
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

    /** Returns the city name encoded in this template. */
    public String getName() { return name; }

    /** Returns the width of this template in tiles. */
    public int getWidth() { return width; }

    /** Returns the height of this template in tiles. */
    public int getHeight() { return height; }

    /**
     * Returns the raw symbol character at grid coordinates (x, y).
     *
     * @param x column index (0-based)
     * @param y row index (0-based)
     */
    public char getTileSymbol(int x, int y) { return layout[x][y]; }

    /** Returns {@code true} if the tile at (x, y) is a building ({@code 'B'}). */
    public boolean isBuilding(int x, int y) { return layout[x][y] == 'B'; }

    /** Returns {@code true} if the tile at (x, y) is any road type ({@code H}, {@code V}, or {@code X}). */
    public boolean isRoad(int x, int y) {
        char c = layout[x][y];
        return c == 'H' || c == 'V' || c == 'X';
    }

    /** Returns {@code true} if the tile at (x, y) is a horizontal road ({@code 'H'}). */
    public boolean isHorizontalRoad(int x, int y) { return layout[x][y] == 'H'; }

    /** Returns {@code true} if the tile at (x, y) is a vertical road ({@code 'V'}). */
    public boolean isVerticalRoad(int x, int y) { return layout[x][y] == 'V'; }

    /** Returns {@code true} if the tile at (x, y) is a crossroad ({@code 'X'}). */
    public boolean isCrossroad(int x, int y) { return layout[x][y] == 'X'; }

    /** Returns {@code true} if the tile at (x, y) is an empty cell ({@code '.'}). */
    public boolean isEmpty(int x, int y) { return layout[x][y] == '.'; }
}