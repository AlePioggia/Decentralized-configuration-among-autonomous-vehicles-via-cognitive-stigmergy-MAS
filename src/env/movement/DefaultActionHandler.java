package movement;

import core.Position;

public class DefaultActionHandler implements ActionHandler {

    @Override
    public MovementResult execute(String agentId, String action, MovementManager movementManager) {
        Position currentPosition = movementManager.getCurrentPosition(agentId);

        return MovementResult.successResult(agentId, currentPosition, currentPosition, action);
    }

}
