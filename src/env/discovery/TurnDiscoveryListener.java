package discovery;

import java.util.List;

import core.Position;

public interface TurnDiscoveryListener {
    void onTurnDiscovered(String agentId, Turn turn);

    default void onPositionFullyExplored(String agentId, Position position, List<Turn> discoveredTurns) {
    };
}