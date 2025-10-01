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
            return MovementResult.failureResult(agentId, null, null, action + " (current position is null)");
        }

        Intersection requestedIntersection = this.intersectionDiscoveryService.getIntersectionByPosition(current);
        if (requestedIntersection == null) {
            return MovementResult.failureResult(agentId, current, null, action + " (not at intersection)");
        }
        if (!this.intersectionDiscoveryService.hasIntersectionBeenDiscovered(requestedIntersection)) {
            return MovementResult.failureResult(agentId, current, null, action + " (intersection not discovered)");
        }

        Position destination = this.intersectionPlanner.computeNext(current, action);
        if (destination == null) {
            return MovementResult.failureResult(agentId, current, null, action + " (destination is null)");
        }
        if (!Utils.isValidPosition(destination, movementManager.getGrid(), movementManager.getRoads())) {
            return MovementResult.failureResult(agentId, current, destination,
                    action + " (destination not a valid road cell)");
        }
        // if (!movementManager.isIntendedPositionFree(agentId, destination)) {
        // System.out.println("[DEBUG] " + agentId + " action=" + action + " from " +
        // current + ": destination "
        // + destination + " is occupied or intended.");
        // return MovementResult.failureResult(agentId, current, destination,
        // action + " (destination occupied or intended)");
        // }

        movementManager.executeMovement(agentId, current, destination);
        return MovementResult.successResult(agentId, current, destination, action);
    }
}