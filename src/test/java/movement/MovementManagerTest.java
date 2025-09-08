package movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import core.Grid;
import core.Position;

public class MovementManagerTest {

    @Test
    public void testExecuteMovement() {
        Grid grid = new Grid(5, 5);
        Position from = new Position(1, 1);
        Position to = new Position(2, 2);

        grid.getCell(from.getX(), from.getY()).setOccupied(true);

        Map<String, Position> agentPositions = new HashMap<>();
        agentPositions.put("agent", from);

        MovementManager manager = new MovementManager(grid, agentPositions, new ArrayList<>());

        manager.executeMovement("agent", from, to);

        assertFalse(grid.getCell(from.getX(), from.getY()).isOccupied(), "Start cell should be free");
        assertTrue(grid.getCell(to.getX(), to.getY()).isOccupied(),
                "Destination cell should be occupied");
        assertEquals(to, manager.getCurrentPosition("agent"), "Agent position should be updated");
    }

    @Test
    public void testIsIntendedPositionFree() {
        Grid grid = new Grid(5, 5);
        Position pos1 = new Position(1, 1);
        Position pos2 = new Position(2, 2);

        grid.getCell(pos1.getX(), pos1.getY()).setOccupied(true);
        grid.getCell(pos2.getX(), pos2.getY()).setOccupied(false);

        Map<String, Position> agentPositions = new HashMap<>();
        agentPositions.put("agent1", pos1);
        agentPositions.put("agent2", pos2);

        MovementManager manager = new MovementManager(grid, agentPositions, new ArrayList<>());

        assertTrue(manager.isIntendedPositionFree("agent1", new Position(3, 3)),
                "Destination should be free");
        assertFalse(manager.isIntendedPositionFree("agent1", pos1),
                "Agent should not move to its own position");
    }

}
