package discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import core.Position;

public class Intersection {

    private final Position position;
    private final Map<String, Position> exits;
    private final Set<Position> footprint;

    public Intersection(Position center) {
        this.position = center;
        HashSet<Position> fp = new HashSet<>();
        fp.add(center);
        this.footprint = Collections.unmodifiableSet(fp);
        this.exits = computeExits();
    }

    public Intersection(Set<Position> footprint) {
        if (footprint == null) {
            throw new IllegalArgumentException("Footprint cannot be null");
        }
        this.footprint = Collections.unmodifiableSet(new HashSet<>(footprint));
        this.position = this.footprint.iterator().next();
        this.exits = computeExits();
    }

    private Map<String, Position> computeExits() {
        Map<String, Position> exits = new HashMap<>();
        exits.put("north", new Position(this.position.getX(), this.position.getY() - 1));
        exits.put("south", new Position(this.position.getX(), this.position.getY() + 1));
        exits.put("east", new Position(this.position.getX() + 1, this.position.getY()));
        exits.put("west", new Position(this.position.getX() - 1, this.position.getY()));
        return exits;
    }

    public Position getPosition() {
        return this.position;
    }

    public List<Position> getExitsPosition() {
        return this.exits.values().stream().collect(Collectors.toList());
    }

    public Map<String, Position> getExits() {
        return this.exits;
    }

    public Set<Position> getFootPrint() {
        return this.footprint;
    }

    public boolean contains(Position p) {
        return p != null && footprint.contains(p);
    }

    public boolean isNear(Position p, int manhattan) {
        if (p == null)
            return false;
        for (Position c : footprint) {
            int dx = Math.abs(c.getX() - p.getX());
            int dy = Math.abs(c.getY() - p.getY());
            if (dx + dy <= manhattan)
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        result = prime * result + ((exits == null) ? 0 : exits.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Intersection other = (Intersection) obj;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        if (exits == null) {
            if (other.exits != null)
                return false;
        } else if (!exits.equals(other.exits))
            return false;
        return true;
    }

}
