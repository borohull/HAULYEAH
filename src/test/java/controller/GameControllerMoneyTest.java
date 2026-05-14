package controller;

import model.Game;
import model.GameState;
import model.Player;
import model.Position;
import model.Road;
import model.Vehicle;
import model.enums.TileType;
import model.enums.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameController Money Limit Tests")
class GameControllerMoneyTest {

    @Test
    @DisplayName("Buying a vehicle without enough money should warn, allow the purchase, and bankrupt the player")
    void testBuyVehicleOverBudgetShowsWarningAndBankrupts() {
        Game game = new Game(20, 20);
        game.getTile(0, 0).setType(TileType.ROAD);
        game.getRoads().add(new Road("road-1", 0, 0, Road.RoadType.HORIZONTAL));
        Player player = new Player("Player 1", 100);
        GameState state = new GameState(game, player);
        GameController controller = new GameController(state);

        AtomicReference<String> warning = new AtomicReference<>();
        AtomicBoolean bankrupt = new AtomicBoolean(false);
        controller.setOnInsufficientFunds(warning::set);
        controller.setOnBankrupt(() -> bankrupt.set(true));

        Vehicle vehicle = controller.onBuyVehicleDirect(VehicleType.CITY_BUS);

        assertNotNull(vehicle, "The vehicle purchase should still go through");
        assertEquals(1, game.getVehicles().size(), "The vehicle should be added to the game");
        assertTrue(player.getLedger().getCurrentCapital() < 0, "The balance should go negative");
        assertTrue(player.getLedger().isBankrupt(), "Negative capital should count as bankrupt");
        assertTrue(bankrupt.get(), "Bankruptcy should be triggered immediately");
        assertTrue(warning.get() != null && warning.get().contains("Not enough money"),
                "A warning should be shown before the purchase goes through");
    }

    @Test
    @DisplayName("Building a road without enough money should warn, allow the build, and bankrupt the player")
    void testBuildRoadOverBudgetShowsWarningAndBankrupts() {
        Game game = new Game(20, 20);
        Player player = new Player("Player 1", 100);
        GameState state = new GameState(game, player);
        GameController controller = new GameController(state);

        AtomicReference<String> warning = new AtomicReference<>();
        AtomicBoolean bankrupt = new AtomicBoolean(false);
        controller.setOnInsufficientFunds(warning::set);
        controller.setOnBankrupt(() -> bankrupt.set(true));

        controller.onBuildRoad(new Position(5, 5));

        assertEquals(TileType.ROAD, game.getTile(5, 5).getType(), "The road should still be built");
        assertEquals(1, game.getRoads().size(), "A road should be added to the game");
        assertTrue(player.getLedger().getCurrentCapital() < 0, "The balance should go negative");
        assertTrue(player.getLedger().isBankrupt(), "Negative capital should count as bankrupt");
        assertTrue(bankrupt.get(), "Bankruptcy should be triggered immediately");
        assertTrue(warning.get() != null && warning.get().contains("Not enough money"),
                "A warning should be shown before the build goes through");
    }
}


