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
            this.turnType = "Right";
        } else if (this.fromPosition.getX() > this.toPosition.getX()) {
            this.turnType = "Left";
        } else {
            this.turnType = "Straight";
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
}
