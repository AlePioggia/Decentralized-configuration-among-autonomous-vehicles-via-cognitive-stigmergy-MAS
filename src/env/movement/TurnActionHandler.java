package movement;

import core.Position;
import discovery.Turn;
import discovery.TurnDiscoveryService;

public class TurnActionHandler implements ActionHandler {
    private final TurnDiscoveryService turnService;

    public TurnActionHandler(TurnDiscoveryService turnService) {
        this.turnService = turnService;
    }

    @Override
    public MovementResult execute(String agent, String action, MovementManager movementManager) {
        Position currentPosition = movementManager.getCurrentPosition(agent);
        Position destination = parseTurnAction(action);

        if (destination == null) {
            return MovementResult.failureResult(agent, currentPosition, null, "Invalid turn action format");
        }

        Turn requestedTurn = new Turn(currentPosition, destination);

        if (!turnService.isTurnDiscovered(requestedTurn)) {
            return MovementResult.failureResult(agent, currentPosition, destination, "Turn not discovered");
        }

        if (movementManager.canMoveTo(destination)) {
            movementManager.executeMovement(agent, currentPosition, destination);
            return MovementResult.successResult(agent, currentPosition, destination, action);
        } else {
            return MovementResult.failureResult(agent, currentPosition, destination, "Destination blocked");
        }
    }

    private Position parseTurnAction(String action) {
        try {
            String[] parts = action.substring(5).split(",");
            if (parts.length == 2) {
                int toX = Integer.parseInt(parts[0].trim());
                int toY = Integer.parseInt(parts[1].trim());
                return new Position(toX, toY);
            }
        } catch (Exception e) {
            System.err.println("Error parsing turn action: " + action);
        }
        return null;
    }

}
