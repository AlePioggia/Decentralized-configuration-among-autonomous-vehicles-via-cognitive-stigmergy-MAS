package discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import core.Position;

public class IntersectionDiscoveryServiceTest {

    @Test
    public void testExploreIntersectionFromCurrentPosition() {
        Intersection intersection1 = new Intersection(new Position(1, 1));
        Intersection intersection2 = new Intersection(new Position(2, 2));

        List<Intersection> available = new ArrayList<>();
        available.add(intersection1);
        available.add(intersection2);

        IntersectionDiscoveryService service = new IntersectionDiscoveryService(available);

        List<Intersection> notified = new ArrayList<>();
        service.addListener((agentId, intersection) -> notified.add(intersection));

        List<Intersection> discovered = service.exploreIntersectionFromCurrentPosition("agent", new Position(1, 1));

        assertTrue(discovered.contains(intersection1));
        assertFalse(discovered.contains(intersection2));
        assertTrue(service.hasIntersectionBeenDiscovered(intersection1));
        assertEquals(1, notified.size());
        assertEquals(intersection1, notified.get(0));
    }

    @Test
    public void testHasIntersectionBeenDiscovered() {
        Intersection intersection = new Intersection(new Position(3, 3));
        List<Intersection> available = List.of(intersection);
        IntersectionDiscoveryService service = new IntersectionDiscoveryService(available);

        assertFalse(service.hasIntersectionBeenDiscovered(intersection));

        service.exploreIntersectionFromCurrentPosition("agent", new Position(3, 3));
        assertTrue(service.hasIntersectionBeenDiscovered(intersection));
    }

}
