package movement;

public interface ActionHandler {
    public MovementResult execute(String agentId, String action, MovementManager movementManager);
}
