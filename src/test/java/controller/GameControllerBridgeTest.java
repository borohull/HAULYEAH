package controller;

import model.Game;
import model.GameState;
import model.Player;
import model.Position;
import model.enums.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GameController Bridge Span Tests")
class GameControllerBridgeTest {

    @Test
    @DisplayName("Should stop a bridge chain once its max consecutive span is exceeded")
    void testBridgeSpanLimitOnConsecutiveTiles() {
        Game game = new Game(20, 20);
        Player player = new Player("Player 1", 100000);
        GameState state = new GameState(game, player);
        GameController controller = new GameController(state);

        AtomicReference<String> message = new AtomicReference<>();
        controller.setOnBridgeLimitReached(message::set);

        for (int x = 5; x <= 8; x++) {
            game.getTile(x, 5).setType(TileType.WATER);
        }

        controller.onBuildBridge(new Position(5, 5), "Wooden Bridge");
        controller.onBuildBridge(new Position(6, 5), "Wooden Bridge");
        controller.onBuildBridge(new Position(7, 5), "Wooden Bridge");

        assertEquals(3, game.getBridges().size(), "Three consecutive bridge tiles should be allowed");
        assertEquals(TileType.BRIDGE, game.getTile(5, 5).getType());
        assertEquals(TileType.BRIDGE, game.getTile(6, 5).getType());
        assertEquals(TileType.BRIDGE, game.getTile(7, 5).getType());
        assertNull(message.get(), "No warning should be shown before the limit is exceeded");

        controller.onBuildBridge(new Position(8, 5), "Wooden Bridge");

        assertEquals(3, game.getBridges().size(), "The bridge should not extend past its max span");
        assertEquals(TileType.WATER, game.getTile(8, 5).getType(), "The blocked tile should remain water");
        assertTrue(message.get() != null && message.get().contains("at most 3 consecutive bridge tiles"),
                "A warning should be shown when the limit is exceeded");
    }
}

