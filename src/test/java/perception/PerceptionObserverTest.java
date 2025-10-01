package perception;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import cartago.ObsProperty;
import core.Cell;
import core.Grid;
import core.Position;
import discovery.Intersection;
import discovery.Turn;

public class PerceptionObserverTest {

    static class MockTestCallback implements PerceptionObserver.PerceptionCallback {
        List<String> definedProps = new ArrayList<>();
        List<String> updatedProps = new ArrayList<>();
        List<String> removedProps = new ArrayList<>();

        @Override
        public void defineObsProperty(String property, Object... args) {
            definedProps.add(property + Arrays.toString(args));
        }

        @Override
        public void removeObsPropertyByTemplate(String property, Object... args) {
            removedProps.add(property + Arrays.toString(args));
        }

        @Override
        public ObsProperty getObsProperty(String property) {
            return null;
        }

        @Override
        public void updateObsProperty(String property, Object... args) {
            updatedProps.add(property + Arrays.toString(args));
        }
    }

    @Test
    void testUpdateAgentPositions() {
        MockTestCallback callback = new MockTestCallback();
        PerceptionObserver observer = new PerceptionObserver(callback);

        Grid grid = new Grid(3, 3);
        Position pos = new Position(1, 1);
        Cell cell = grid.getCell(1, 1);
        cell.setOccupied(true);
        cell.setDirection("East");

        Map<String, Position> agentPositions = new HashMap<>();
        agentPositions.put("agent1", pos);

        // observer.updateAgentPositions(agentPositions, grid);

        assertTrue(callback.definedProps.stream().anyMatch(s -> s.contains("at")));
        assertTrue(callback.definedProps.stream().anyMatch(s -> s.contains("occupied")));
        assertTrue(callback.definedProps.stream().anyMatch(s -> s.contains("direction")));
        assertTrue(callback.updatedProps.stream().anyMatch(s -> s.contains("occupants")));
    }

    @Test
    void testNotifyTurnAvailable() {
        MockTestCallback callback = new MockTestCallback();
        PerceptionObserver observer = new PerceptionObserver(callback);

        Turn turn = new Turn(new Position(0, 0), new Position(1, 1));
        observer.notifyTurnAvailable(turn);

        assertTrue(callback.definedProps.stream().anyMatch(s -> s.contains("turn_available")));
    }

    @Test
    void testNotifyIntersectionAvailable() {
        MockTestCallback callback = new MockTestCallback();
        PerceptionObserver observer = new PerceptionObserver(callback);

        HashSet<Position> footprint = new HashSet<>();
        footprint.add(new Position(0, 0));
        footprint.add(new Position(1, 1));
        Intersection intersection = new Intersection(footprint);
        observer.notifyIntersectionAvailable(intersection);

        assertTrue(callback.definedProps.stream().anyMatch(s -> s.contains("intersection_available")));
    }

}
