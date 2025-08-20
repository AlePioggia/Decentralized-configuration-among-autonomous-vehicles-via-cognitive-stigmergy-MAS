package discovery;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import core.Position;

import java.util.ArrayList;
import java.util.HashSet;

public class IntersectionDiscoveryService {

    private List<Intersection> availableIntersections;
    private Set<Intersection> discoveredIntersections;
    private List<IntersectionDiscoveryListener> listeners;

    public IntersectionDiscoveryService(List<Intersection> availableIntersections) {
        this.availableIntersections = availableIntersections;
        this.discoveredIntersections = new HashSet<>();
        this.listeners = new ArrayList<>();
    }

    public List<Intersection> exploreIntersectionFromCurrentPosition(String agentId, Position currentPosition) {
        List<Intersection> newlyDiscoveredIntersections = new ArrayList<>();
        List<Intersection> availableIntersections = this.availableIntersections.stream()
                .filter(intersection -> intersection.getPosition().equals(currentPosition)
                        || intersection.isNear(currentPosition, 1))
                .collect(Collectors.toList());

        for (Intersection intersection : availableIntersections) {
            if (!this.discoveredIntersections.contains(intersection)) {
                this.discoveredIntersections.add(intersection);
                newlyDiscoveredIntersections.add(intersection);
                notifyIntersectionDiscovered(agentId, intersection);
            }
        }
        return newlyDiscoveredIntersections;
    }

    public void addListener(IntersectionDiscoveryListener listener) {
        this.listeners.add(listener);
    }

    public void notifyIntersectionDiscovered(String agentId, Intersection intersection) {
        for (IntersectionDiscoveryListener listener : this.listeners) {
            listener.onIntersectionDiscovered(agentId, intersection);
        }
    }

    public Intersection getIntersectionByPosition(Position position) {
        return this.availableIntersections.stream()
                .filter(i -> i.contains(position) || i.isNear(position, 1))
                .findFirst()
                .orElse(null);
    }

    public boolean hasIntersectionBeenDiscovered(Intersection intersection) {
        return this.discoveredIntersections.contains(intersection);
    }

}
