public class Turn {
    private final Position fromPosition;
    private final Position toPosition;
    private String turnType;

    public Turn(final Position fromPosition, final Position toPosition) {
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.turnType = computeTurnType();
    }

    public String computeTurnType() {
        if (this.fromPosition.getX() < this.toPosition.getX()) {
            this.turnType = "east";
        } else if (this.fromPosition.getX() > this.toPosition.getX()) {
            this.turnType = "west";
        } else if (this.fromPosition.getY() < this.toPosition.getY()) {
            this.turnType = "north";
        } else if (this.fromPosition.getY() > this.toPosition.getY()) {
            this.turnType = "south";
        }
        return this.turnType;
    }

    public Position getFromPosition() {
        return this.fromPosition;
    }

    public Position getToPosition() {
        return this.toPosition;
    }

    public String getTurnType() {
        return this.turnType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fromPosition == null) ? 0 : fromPosition.hashCode());
        result = prime * result + ((toPosition == null) ? 0 : toPosition.hashCode());
        result = prime * result + ((turnType == null) ? 0 : turnType.hashCode());
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
        Turn other = (Turn) obj;
        if (fromPosition == null) {
            if (other.fromPosition != null)
                return false;
        } else if (!fromPosition.equals(other.fromPosition))
            return false;
        if (toPosition == null) {
            if (other.toPosition != null)
                return false;
        } else if (!toPosition.equals(other.toPosition))
            return false;
        if (turnType == null) {
            if (other.turnType != null)
                return false;
        } else if (!turnType.equals(other.turnType))
            return false;
        return true;
    }

}
