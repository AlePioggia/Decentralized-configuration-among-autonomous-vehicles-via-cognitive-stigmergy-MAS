package movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Cell;
import core.Grid;
import core.Position;
import road.Road;
import core.Utils;

public class MovementManager {
    private final Grid grid;
    private Map<String, Position> agentPositions;
    private final List<Road> roads;
    private Map<String, Position> agentIntentions;
    private IntersectionPlanner intersectionPlanner;

    public MovementManager(Grid grid) {
        this.grid = grid;
        this.agentPositions = new HashMap<>();
        this.roads = new ArrayList<>();
        this.intersectionPlanner = new IntersectionPlanner(grid, roads);
        this.agentIntentions = new HashMap<>();
    }

    public MovementManager(Grid grid, Map<String, Position> agentPositions, List<Road> roads) {
        this.grid = grid;
        this.agentPositions = agentPositions;
        this.roads = roads;
        this.intersectionPlanner = new IntersectionPlanner(grid, roads);
        this.agentIntentions = new HashMap<>();
    }

    public MovementManager(Grid grid, Map<String, Position> agentPositions, List<Road> roads,
            Map<String, Position> agentIntentions) {
        this.grid = grid;
        this.agentPositions = agentPositions;
        this.roads = roads;
        this.intersectionPlanner = new IntersectionPlanner(grid, roads);
        this.agentIntentions = agentIntentions;
    }

    public MovementResult executeAction(String agentId, String action) {
        Position currentPosition = this.agentPositions.get(agentId);
        if (currentPosition == null) {
            return MovementResult.failureResult(agentId, currentPosition, currentPosition, action);
        }
        ActionHandler handler = ActionHandlerFactory.getHandler(action);
        if (handler == null) {
            return MovementResult.failureResult(agentId, currentPosition, currentPosition, action);
        }

        return handler.execute(agentId, action, this);
    }

    public void updateIntentions(Map<String, String> agentActions) {
        agentIntentions.clear();
        for (Map.Entry<String, String> entry : agentActions.entrySet()) {
            String agentId = entry.getKey();
            String action = entry.getValue();
            Position current = agentPositions.get(agentId);
            if (current == null)
                continue;

            Position intendedDestination = null;

            if ("wait".equals(action)) {
                intendedDestination = current;
            } else if (action.startsWith("turn:")) {
                intendedDestination = Utils.parseTurnAction(action);
            } else if (action.startsWith("intersection:")) {
                intendedDestination = (this.intersectionPlanner != null)
                        ? this.intersectionPlanner.computeNext(current, action)
                        : null;
            } else if ("follow".equals(action)) {
                Cell cell = grid.getCell(current.getX(), current.getY());
                intendedDestination = computeNextPosition(cell);
            }
            if (intendedDestination != null && !intendedDestination.equals(current)) {
                agentIntentions.put(agentId, intendedDestination);
            }
        }
    }

    public boolean isIntendedPositionFree(String agentId, Position position) {
        return this.agentIntentions
                .entrySet()
                .stream()
                .noneMatch(entry -> !entry.getKey().equals(agentId) && entry.getValue().equals(position))
                && !grid.getCell(position.getX(), position.getY()).isOccupied();
    }

    private Position computeNextPosition(Cell cell) {
        int x = cell.getPosition().getX();
        int y = cell.getPosition().getY();

        switch (cell.getDirection()) {
            case "North":
                return new Position(x, y - 1);
            case "South":
                return new Position(x, y + 1);
            case "East":
                return new Position(x + 1, y);
            case "West":
                return new Position(x - 1, y);
            default:
                return cell.getPosition();
        }
    }

    public List<Road> getRoads() {
        return this.roads;
    }

    public void executeMovement(String agent, Position from, Position to) {
        grid.getCell(from.getX(), from.getY()).setOccupied(false);
        grid.getCell(to.getX(), to.getY()).setOccupied(true);
        agentPositions.put(agent, to);
    }

    public Position getCurrentPosition(String agent) {
        return agentPositions.get(agent);
    }

    public Grid getGrid() {
        return grid;
    }

    public IntersectionPlanner getIntersectionPlanner() {
        return this.intersectionPlanner;
    }

}
