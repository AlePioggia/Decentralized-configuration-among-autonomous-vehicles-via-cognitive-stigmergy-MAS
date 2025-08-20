package discovery;

import java.util.List;

import core.Position;

public interface IntersectionDiscoveryListener {
    public void onIntersectionDiscovered(String agentId, Intersection intersection);

    public default void onIntersectionPositionFullyExplored(String agentId, Position position,
            List<Intersection> discoveredIntersections) {
    };
}
