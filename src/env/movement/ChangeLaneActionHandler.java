package movement;

import core.Position;

public class ChangeLaneActionHandler implements ActionHandler {

    private ChangeLanePlanner planner;

    public ChangeLaneActionHandler(ChangeLanePlanner planner) {
        this.planner = planner;
    }

    @Override
    public MovementResult execute(String agent, String action, MovementManager movementManager) {
        String[] parts = action.split(":");
        if (parts.length != 2)
            return MovementResult.failureResult(agent, null, null, action);
        String side = parts[1];
        Position currentPosition = movementManager.getCurrentPosition(agent);
        String direction = movementManager.getCurrentDirection(agent);
        if (currentPosition == null || direction == null)
            return MovementResult.failureResult(agent, null, null, action);

        Position targetPosition = planner.changeLane(currentPosition, direction, side);
        if (targetPosition == null)
            return MovementResult.failureResult(agent, currentPosition, currentPosition, action);

        movementManager.executeMovement(agent, currentPosition, targetPosition);

        return MovementResult.successResult(agent, currentPosition, targetPosition, action);
    }
}
