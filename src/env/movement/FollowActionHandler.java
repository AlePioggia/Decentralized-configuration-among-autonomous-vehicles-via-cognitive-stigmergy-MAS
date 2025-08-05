package movement;

import core.Cell;
import core.Position;

public class FollowActionHandler implements ActionHandler {
    @Override
    public MovementResult execute(String agent, String action, MovementManager movementManager) {
        Position currentPosition = movementManager.getCurrentPosition(agent);
        Cell currentCell = movementManager.getGrid().getCell(currentPosition.getX(), currentPosition.getY());

        Position nextPosition = computeNextPosition(currentCell);

        if (movementManager.canMoveTo(nextPosition)) {
            movementManager.executeMovement(agent, currentPosition, nextPosition);
            return MovementResult.successResult(agent, currentPosition, nextPosition, action);
        } else {
            return MovementResult.failureResult(agent, currentPosition, nextPosition, "Path blocked");
        }
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
}
