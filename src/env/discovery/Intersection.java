package discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import core.Position;

public class Intersection {

    private final Position position;
    private final Map<String, Position> exits;

    public Intersection(Position position) {
        this.position = position;
        this.exits = new HashMap<>();
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
