package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Position class.
 * Tests coordinate handling and equality.
 */
@DisplayName("Position Tests")
class PositionTest {

    @Test
    @DisplayName("Position should store x and y coordinates")
    void testPositionCreation() {
        Position pos = new Position(5, 10);
        assertEquals(5, pos.getX(), "X coordinate should be 5");
        assertEquals(10, pos.getY(), "Y coordinate should be 10");
    }

    @Test
    @DisplayName("Equal positions should be equal")
    void testPositionEquality() {
        Position pos1 = new Position(3, 4);
        Position pos2 = new Position(3, 4);
        assertEquals(pos1, pos2, "Positions with same coordinates should be equal");
    }

    @Test
    @DisplayName("Different positions should not be equal")
    void testPositionInequality() {
        Position pos1 = new Position(3, 4);
        Position pos2 = new Position(3, 5);
        assertNotEquals(pos1, pos2, "Positions with different coordinates should not be equal");
    }

    @Test
    @DisplayName("Position should handle negative coordinates")
    void testNegativeCoordinates() {
        Position pos = new Position(-5, -10);
        assertEquals(-5, pos.getX());
        assertEquals(-10, pos.getY());
    }
}

