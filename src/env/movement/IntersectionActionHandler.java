package movement;

import core.Position;
import core.Utils;
import discovery.Intersection;
import discovery.IntersectionDiscoveryService;

public class IntersectionActionHandler implements ActionHandler {

    private final IntersectionPlanner intersectionPlanner;
    private final IntersectionDiscoveryService intersectionDiscoveryService;

    public IntersectionActionHandler(IntersectionPlanner intersectionPlanner,
            IntersectionDiscoveryService intersectionDiscoveryService) {
        this.intersectionPlanner = intersectionPlanner;
        this.intersectionDiscoveryService = intersectionDiscoveryService;
    }

    @Override
    public MovementResult execute(String agentId, String action, MovementManager movementManager) {
        Position current = movementManager.getCurrentPosition(agentId);
        if (current == null) {
            return MovementResult.failureResult(agentId, null, null, action);
        }

        Intersection requestedIntersection = this.intersectionDiscoveryService.getIntersectionByPosition(current);
        if (requestedIntersection == null
                || !this.intersectionDiscoveryService.hasIntersectionBeenDiscovered(requestedIntersection)) {
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