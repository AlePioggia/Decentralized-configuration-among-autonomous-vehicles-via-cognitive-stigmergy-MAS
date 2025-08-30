package movement;

import core.Cell;
import core.Position;
import core.Utils;

public class FollowActionHandler implements ActionHandler {
    @Override
    public MovementResult execute(String agent, String action, MovementManager movementManager) {
        Position currentPosition = movementManager.getCurrentPosition(agent);
        Cell currentCell = movementManager.getGrid().getCell(currentPosition.getX(), currentPosition.getY());

        Position nextPosition = Utils.computeNextPosition(currentCell, movementManager.getGrid());

        if (Utils.isValidPosition(nextPosition, movementManager.getGrid(), movementManager.getRoads())
                && movementManager.isIntendedPositionFree(agent, nextPosition)) {
            movementManager.executeMovement(agent, currentPosition, nextPosition);
            return MovementResult.successResult(agent, currentPosition, nextPosition, action);
        } else {
            return MovementResult.failureResult(agent, currentPosition, nextPosition, "Path blocked");
        }
    }
}
