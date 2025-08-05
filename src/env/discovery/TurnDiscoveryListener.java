package discovery;

import java.util.List;

import core.Position;

public interface TurnDiscoveryListener {
    public void onTurnDiscovered(String agentId, Turn turn);

    public default void onPositionFullyExplored(String agentId, Position position, List<Turn> discoveredTurns) {
    };
}