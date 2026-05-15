package model;

import model.enums.CargoType;
import model.enums.Direction;
import model.enums.LightState;
import model.enums.TileType;
import model.enums.TimeSpeed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Additional Model Coverage Tests")
class ModelCoverageTest {

    @Test
    @DisplayName("TimeSpeed exposes multipliers, labels, and cycles")
    void timeSpeedValuesAndCycle() {
        assertEquals(0, TimeSpeed.PAUSED.getMultiplier());
        assertEquals(1, TimeSpeed.X1.getMultiplier());
        assertEquals(2, TimeSpeed.X2.getMultiplier());
        assertEquals(4, TimeSpeed.X4.getMultiplier());

        for (TimeSpeed speed : TimeSpeed.values()) {
            assertNotNull(speed.getLabel());
            assertFalse(speed.getLabel().isBlank());
        }

        assertEquals(TimeSpeed.X1, TimeSpeed.PAUSED.next());
        assertEquals(TimeSpeed.X2, TimeSpeed.X1.next());
        assertEquals(TimeSpeed.X4, TimeSpeed.X2.next());
        assertEquals(TimeSpeed.PAUSED, TimeSpeed.X4.next());
    }

    @Test
    @DisplayName("Simple enum values are exposed")
    void simpleEnumValues() {
        assertArrayEquals(
                new LightState[] { LightState.RED, LightState.GREEN },
                LightState.values());
        assertArrayEquals(
                new model.enums.BridgeType[] {
                        model.enums.BridgeType.WOODEN,
                        model.enums.BridgeType.STEEL,
                        model.enums.BridgeType.SUSPENSION
                },
                model.enums.BridgeType.values());
    }

    @Test
    @DisplayName("CityTemplate validates and exposes all tile symbols")
    void cityTemplateExposesSymbols() {
        CityTemplate template = new CityTemplate("Grid", "BH", "VX", "..");

        assertEquals("Grid", template.getName());
        assertEquals(2, template.getWidth());
        assertEquals(3, template.getHeight());

        assertEquals('B', template.getTileSymbol(0, 0));
        assertTrue(template.isBuilding(0, 0));
        assertFalse(template.isBuilding(1, 0));

        assertTrue(template.isHorizontalRoad(1, 0));
        assertTrue(template.isRoad(1, 0));
        assertFalse(template.isVerticalRoad(1, 0));

        assertTrue(template.isVerticalRoad(0, 1));
        assertTrue(template.isRoad(0, 1));
        assertFalse(template.isHorizontalRoad(0, 1));

        assertTrue(template.isCrossroad(1, 1));
        assertTrue(template.isRoad(1, 1));
        assertTrue(template.isEmpty(0, 2));
        assertFalse(template.isRoad(0, 2));
    }

    @Test
    @DisplayName("CityTemplate rejects invalid rows")
    void cityTemplateRejectsInvalidRows() {
        assertThrows(IllegalArgumentException.class, () -> new CityTemplate("Empty"));
        assertThrows(IllegalArgumentException.class, () -> new CityTemplate("Null", (String[]) null));
        assertThrows(IllegalArgumentException.class, () -> new CityTemplate("Ragged", "BB", "B"));
        assertThrows(IllegalArgumentException.class, () -> new CityTemplate("Bad", "BZ"));
    }

    @Test
    @DisplayName("Tile tree count clamps and changes forest state")
    void tileTreeCountAndBuildableRules() {
        Tile tile = new Tile(2, 3);

        assertEquals(new Position(2, 3), tile.getPosition());
        assertNull(tile.getEntityId());
        assertNull(tile.getEntityName());
        assertEquals(0, tile.getTreeCount());
        assertFalse(tile.hasTrees());
        assertTrue(tile.isBuildable());

        tile.setTreeCount(2);
        assertEquals(2, tile.getTreeCount());
        assertEquals(TileType.FOREST, tile.getType());
        assertTrue(tile.hasTrees());
        assertTrue(tile.isBuildable());

        tile.setTreeCount(99);
        assertEquals(4, tile.getTreeCount());
        tile.addTree();
        assertEquals(4, tile.getTreeCount());

        tile.setTreeCount(-5);
        assertEquals(0, tile.getTreeCount());
        assertEquals(TileType.EMPTY, tile.getType());
        assertFalse(tile.hasTrees());

        tile.setType(TileType.ROAD);
        tile.setTreeCount(3);
        assertEquals(TileType.ROAD, tile.getType());
        assertFalse(tile.isBuildable());
        assertTrue(tile.toString().contains("Tile["));
    }

    @Test
    @DisplayName("TrafficLight with no directions leaves approaches green")
    void trafficLightWithoutDirections() {
        TrafficLight light = new TrafficLight("tl-empty", new Position(1, 1), List.of());

        assertEquals("tl-empty", light.getId());
        assertEquals(new Position(1, 1), light.getPosition());
        assertTrue(light.getActiveDirections().isEmpty());
        assertNull(light.getCurrentGreenDirection());
        assertEquals(LightState.GREEN, light.getStateFor(Direction.NORTH));

        light.tick(50.0);
        light.cycleToNextPhase();
        light.restoreState(5, -10.0);

        assertEquals(0, light.getCurrentPhaseIndex());
        assertEquals(10.0, light.getPhaseTimer());
    }

    @Test
    @DisplayName("TrafficLight cycles, clamps durations, and restores state")
    void trafficLightCyclesAndRestores() {
        TrafficLight light = new TrafficLight(
                "tl",
                new Position(4, 4),
                List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH));

        assertEquals(Direction.NORTH, light.getCurrentGreenDirection());
        assertEquals(LightState.GREEN, light.getStateFor(Direction.NORTH));
        assertEquals(LightState.RED, light.getStateFor(Direction.EAST));
        assertEquals(LightState.GREEN, light.getStateFor(Direction.WEST));
        assertEquals(10.0, light.getGreenDuration(Direction.NORTH));

        light.setGreenDuration(Direction.EAST, 3.5);
        light.setGreenDuration(Direction.SOUTH, 0.25);
        light.setGreenDuration(Direction.WEST, 99.0);
        assertEquals(3.5, light.getGreenDuration(Direction.EAST));
        assertEquals(1.0, light.getGreenDuration(Direction.SOUTH));
        assertEquals(10.0, light.getGreenDuration(Direction.WEST));

        light.tick(2.0);
        assertTrue(light.getPhaseTimer() < 10.0);

        light.cycleToNextPhase();
        assertEquals(Direction.EAST, light.getCurrentGreenDirection());
        assertEquals(3.5, light.getPhaseTimer());

        light.tick(4.0);
        assertEquals(Direction.SOUTH, light.getCurrentGreenDirection());
        assertEquals(1.0, light.getPhaseTimer());

        light.restoreState(-1, 50.0);
        assertEquals(Direction.SOUTH, light.getCurrentGreenDirection());
        assertEquals(1.0, light.getPhaseTimer());

        light.restoreState(100, -3.0);
        assertEquals(Direction.EAST, light.getCurrentGreenDirection());
        assertEquals(0.0, light.getPhaseTimer());
    }

    @Test
    @DisplayName("Map entities expose footprint geometry")
    void mapEntityFootprintAndDerivedEntities() {
        Facility facility = new Facility(
                "fac",
                "Factory",
                10,
                20,
                2,
                3,
                List.of(CargoType.COAL),
                List.of(CargoType.WOOD));

        assertEquals("fac", facility.getId());
        assertEquals("Factory", facility.getName());
        assertEquals(new Position(10, 20), facility.getOrigin());
        assertEquals(2, facility.getWidth());
        assertEquals(3, facility.getHeight());
        assertEquals(6, facility.getTiles().size());
        assertEquals(new Position(11, 21), facility.getCenter());
        assertTrue(facility.containsPosition(10, 20));
        assertTrue(facility.containsPosition(11, 22));
        assertFalse(facility.containsPosition(12, 22));
        assertFalse(facility.containsPosition(11, 23));
        assertEquals(List.of(CargoType.COAL), facility.getProduces());
        assertEquals(List.of(CargoType.WOOD), facility.getConsumes());
        assertEquals(CargoType.COAL, facility.getPrimaryProduction());

        Facility emptyProducer = new Facility("empty", "Empty", 0, 0, 1, 1, List.of(), List.of());
        assertNull(emptyProducer.getPrimaryProduction());
    }

    @Test
    @DisplayName("Stop orientation cycles clockwise")
    void stopOrientationCycles() {
        Stop stop = new Stop("stop", 5, 6, "Depot");

        assertEquals("stop", stop.getId());
        assertEquals(new Position(5, 6), stop.getPosition());
        assertEquals("Depot", stop.getName());
        assertFalse(stop.isInsideCity());
        assertEquals(Direction.EAST, stop.getOrientation());

        stop.setInsideCity(true);
        assertTrue(stop.isInsideCity());

        stop.cycleOrientation();
        assertEquals(Direction.SOUTH, stop.getOrientation());
        stop.cycleOrientation();
        assertEquals(Direction.WEST, stop.getOrientation());
        stop.cycleOrientation();
        assertEquals(Direction.NORTH, stop.getOrientation());
        stop.cycleOrientation();
        assertEquals(Direction.EAST, stop.getOrientation());
    }

    @Test
    @DisplayName("Bridge exposes span, orientation, and type metadata")
    void bridgeMetadata() {
        Bridge horizontal = new Bridge(
                "bridge-h",
                "Horizontal",
                2,
                3,
                4,
                Bridge.Orientation.HORIZONTAL,
                Bridge.BridgeType.STEEL);

        assertEquals(4, horizontal.getLength());
        assertEquals(Bridge.Orientation.HORIZONTAL, horizontal.getOrientation());
        assertEquals(Bridge.BridgeType.STEEL, horizontal.getBridgeType());
        assertEquals(4, horizontal.getWidth());
        assertEquals(1, horizontal.getHeight());

        Bridge vertical = new Bridge(
                "bridge-v",
                "Vertical",
                2,
                3,
                5,
                Bridge.Orientation.VERTICAL,
                Bridge.BridgeType.SUSPENSION);

        assertEquals(1, vertical.getWidth());
        assertEquals(5, vertical.getHeight());
        assertEquals(100, Bridge.BridgeType.WOODEN.getCost());
        assertEquals(3, Bridge.BridgeType.WOODEN.getMaxSpan());
        assertEquals(1, Bridge.BridgeType.WOODEN.getMaxSpeedMultiplier());
        assertEquals(500, Bridge.BridgeType.STEEL.getCost());
        assertEquals(10, Bridge.BridgeType.STEEL.getMaxSpan());
        assertEquals(2, Bridge.BridgeType.STEEL.getMaxSpeedMultiplier());
        assertEquals(1000, Bridge.BridgeType.SUSPENSION.getCost());
        assertEquals(6, Bridge.BridgeType.SUSPENSION.getMaxSpan());
        assertEquals(4, Bridge.BridgeType.SUSPENSION.getMaxSpeedMultiplier());
    }
}
