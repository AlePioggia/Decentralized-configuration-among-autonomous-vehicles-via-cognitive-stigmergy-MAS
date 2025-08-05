public interface ActionHandler {
    MovementResult execute(String agentId, String action, MovementManager movementManager);
}
