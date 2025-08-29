package road;

import java.util.List;
import java.util.Set;

import core.Position;

public class RoadLayout {
    private final List<Road> roads;
    private final Set<Set<Position>> intersectionFootprints;
    private final List<Position> trafficLightPositions;
    private final Set<Position[]> turns;

    public RoadLayout(List<Road> roads, Set<Set<Position>> intersectionFootprints, List<Position> trafficLightPositions,
            Set<Position[]> roadSegments) {
        this.roads = roads;
        this.intersectionFootprints = intersectionFootprints;
        this.trafficLightPositions = trafficLightPositions;
        this.turns = roadSegments;
    }

    public List<Road> getRoads() {
        return this.roads;
    }

    public Set<Set<Position>> getIntersectionFootprints() {
        return this.intersectionFootprints;
    }

    public List<Position> getTrafficLightPositions() {
        return this.trafficLightPositions;
    }

    public Set<Position[]> getTurns() {
        return this.turns;
    }

    @Override
    public String toString() {
        return "RoadLayout [roads=" + roads + ", intersectionFootprints=" + intersectionFootprints
                + ", trafficLightPositions=" + trafficLightPositions + ", turns=" + turns + "]\n";
    }

}
