package movement;

import core.Position;
import discovery.Turn;
import discovery.TurnDiscoveryService;
import core.Utils;

public class TurnActionHandler implements ActionHandler {
    private final TurnDiscoveryService turnService;

    public TurnActionHandler(TurnDiscoveryService turnService) {
        this.turnService = turnService;
    }

    @Override
    public MovementResult execute(String agent, String action, MovementManager movementManager) {
        Position currentPosition = movementManager.getCurrentPosition(agent);
        Position destination = Utils.parseTurnAction(action);

        if (destination == null) {
            return MovementResult.failureResult(agent, currentPosition, null, "Invalid turn action format");
        }

        Turn requestedTurn = new Turn(currentPosition, destination);

        if (!turnService.isTurnDiscovered(requestedTurn)) {
            return MovementResult.failureResult(agent, currentPosition, destination, "Turn not discovered");
        }

        if (Utils.isValidPosition(destination, movementManager.getGrid(), movementManager.getRoads())) {
            movementManager.executeMovement(agent, currentPosition, destination);
            return MovementResult.successResult(agent, currentPosition, destination, action);
        } else {
            return MovementResult.failureResult(agent, currentPosition, destination, "Destination blocked");
        }
    }

}
