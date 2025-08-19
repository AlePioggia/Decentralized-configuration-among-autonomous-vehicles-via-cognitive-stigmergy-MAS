package movement;

import core.Position;
import core.Utils;

public class IntersectionActionHandler implements ActionHandler {

    private final IntersectionPlanner intersectionPlanner;

    public IntersectionActionHandler(IntersectionPlanner intersectionPlanner) {
        this.intersectionPlanner = intersectionPlanner;
    }

    @Override
    public MovementResult execute(String agentId, String action, MovementManager movementManager) {
        Position current = movementManager.getCurrentPosition(agentId);
        if (current == null) {
            return MovementResult.failureResult(agentId, null, null, action);
        }
        Position destination = this.intersectionPlanner.computeNext(current, action);
        if (destination == null || !Utils.isValidPosition(destination, movementManager.getGrid(),
                movementManager.getRoads()) || !movementManager.isIntendedPositionFree(agentId, destination)) {
            return MovementResult.failureResult(agentId, null, null,
                    "Couldn't reach destination, it may be occupied or nonexistant");
        }
        movementManager.executeMovement(agentId, current, destination);
        return MovementResult.successResult(agentId, current, destination, action);
    }
}