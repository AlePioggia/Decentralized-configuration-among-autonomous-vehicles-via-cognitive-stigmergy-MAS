import java.util.List;

public interface TurnDiscoveryListener {
    void onTurnDiscovered(String agentId, Turn turn);

    default void onPositionFullyExplored(String agentId, Position position, List<Turn> discoveredTurns) {
    };
}