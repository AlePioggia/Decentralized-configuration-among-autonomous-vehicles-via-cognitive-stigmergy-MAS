package movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Cell;
import core.Grid;
import core.Position;
import road.Road;

public class MovementManager {
    private final Grid grid;
    private Map<String, Position> agentPositions;
    private final List<Road> roads;

    public MovementManager(Grid grid) {
        this.grid = grid;
        this.agentPositions = new HashMap<>();
        this.roads = new ArrayList<>();
    }

    public MovementManager(Grid grid, Map<String, Position> agentPositions, List<Road> roads) {
        this.grid = grid;
        this.agentPositions = agentPositions;
        this.roads = roads;
    }

    public MovementResult executeAction(String agentId, String action) {
        Position currentPosition = this.agentPositions.get(agentId);

        if (currentPosition != null) {
            if (action.equals("follow")) {
                return executeFollowAction(agentId, currentPosition);
            } else if (action.startsWith("turn:")) {
                return executeTurnAction(agentId, currentPosition, action);
            } else if (action.equals("wait")) {
                return MovementResult.failureResult(agentId, currentPosition, null, "Waiting action executed");
            } else {
                return MovementResult.failureResult(agentId, currentPosition, null, "Unknown action: " + action);
            }
        }
        return MovementResult.failureResult(agentId, null, null, "Agent not found");
    }

    private MovementResult executeFollowAction(String agent, Position currentPosition) {
        Cell currentCell = grid.getCell(currentPosition.getX(), currentPosition.getY());
        Position nextPosition = computeNextPosition(currentCell);

        if (canMoveTo(nextPosition)) {
            executeMovement(agent, currentPosition, nextPosition);
            return MovementResult.successResult(agent, currentPosition, nextPosition, agent);
        } else {
            return MovementResult.failureResult(agent, currentPosition, nextPosition, "Path blocked");
        }
    }

    private MovementResult executeTurnAction(String agent, Position currentPosition, String action) {
        Position destination = parseTurnDestination(action);

        if (destination == null) {
            return MovementResult.failureResult(agent, currentPosition, null, "Invalid turn action format");
        }

        if (canMoveTo(destination)) {
            executeMovement(agent, currentPosition, destination);
            return MovementResult.successResult(agent, currentPosition, destination, action);
        } else {
            return MovementResult.failureResult(agent, currentPosition, destination, "Turn destination blocked");
        }
    }

    private Position parseTurnDestination(String action) {
        try {
            String[] parts = action.substring(5).split(",");
            if (parts.length == 2) {
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                return new Position(x, y);
            }
        } catch (Exception e) {
            System.err.println("Error parsing turn action: " + action);
        }
        return null;
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

    public boolean canMoveTo(Position position) {
        return isPositionWithinBounds(position)
                && grid.getCell(position.getX(), position.getY()) != null
                && isInsideAnyRoad(position)
                && !grid.getCell(position.getX(), position.getY()).isOccupied();
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

    private boolean isInsideAnyRoad(Position position) {
        return roads.stream()
                .anyMatch(road -> road.getLines().contains(grid.getCell(position.getX(), position.getY())));
    }

    private boolean isPositionWithinBounds(Position position) {
        return position.getX() >= 0 && position.getX() < grid.getWidth()
                && position.getY() >= 0 && position.getY() < grid.getHeight();
    }

}
