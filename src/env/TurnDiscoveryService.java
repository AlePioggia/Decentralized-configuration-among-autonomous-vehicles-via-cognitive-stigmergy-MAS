import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TurnDiscoveryService {
    private final List<Turn> availableTurns;
    private final Set<Turn> discoveredTurns;
    private List<TurnDiscoveryListener> listeners;

    public TurnDiscoveryService(List<Turn> availableTurns) {
        this.availableTurns = availableTurns;
        this.discoveredTurns = new HashSet<>();
        this.listeners = new ArrayList<>();
    }

    public List<Turn> exploreTurns(String agentId, Position currentPosition) {
        List<Turn> newlyDiscoveredTurns = new ArrayList<>();
        List<Turn> possibleTurns = this.availableTurns.stream()
                .filter(turn -> turn.getFromPosition().equals(currentPosition))
                .collect(Collectors.toList());

        for (Turn turn : possibleTurns) {
            if (!this.discoveredTurns.contains(turn)) {
                this.discoveredTurns.add(turn);
                newlyDiscoveredTurns.add(turn);
                notifyTurnDiscovered(agentId, turn);
            }
        }

        return newlyDiscoveredTurns;
    }

    public boolean isTurnDiscovered(Turn turn) {
        return discoveredTurns.contains(turn);
    }

    public void addListener(TurnDiscoveryListener listener) {
        this.listeners.add(listener);
    }

    private void notifyTurnDiscovered(String agentId, Turn turn) {
        for (TurnDiscoveryListener listener : this.listeners) {
            listener.onTurnDiscovered(agentId, turn);
        }
    }

}
