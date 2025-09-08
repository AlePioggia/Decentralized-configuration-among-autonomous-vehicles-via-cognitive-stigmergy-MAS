package core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Timer;

public class UtilsTest {
    @Test
    public void testComputeNextPosition() {
        Grid grid = new Grid(5, 5);
        Cell current = new Cell(0, 0, "East");
        Position expectedPosition = new Position(1, 0);
        Position actualPosition = Utils.computeNextPosition(current, grid);
        assertEquals(expectedPosition, actualPosition);
    }

    @Test
    public void testParseTurnAction() {
        String action = "turn: 0, 1";
        Position expectedPosition = new Position(0, 1);
        Position actualPosition = Utils.parseTurnAction(action);
        assertEquals(expectedPosition, actualPosition);
    }

    @Test
    public void testCreateTimer() {
        Timer timer = Utils.createTimer("TestTimer", () -> System.out.println("Timer triggered"), 2, 2);
        assertNotNull(timer);
    }

}
