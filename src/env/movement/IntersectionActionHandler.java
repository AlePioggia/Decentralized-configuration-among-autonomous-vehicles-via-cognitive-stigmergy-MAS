package movement;

import discovery.IntersectionDiscoveryService;

public class IntersectionActionHandler implements ActionHandler {

    private final IntersectionDiscoveryService discoveryService;

    public IntersectionActionHandler(IntersectionDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public MovementResult execute(String agentId, String action, MovementManager movementManager) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }
}
