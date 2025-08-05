package core;

public class Cell {
    private Position position;
    private String direction;
    private boolean isOccupied;

    public Cell(int x, int y) {
        this.position = new Position(x, y);
        this.direction = "East";
        this.isOccupied = false;
    }

    public Cell(int x, int y, String direction) {
        this.position = new Position(x, y);
        this.direction = direction;
        this.isOccupied = false;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setOccupied(boolean occupied) {
        this.isOccupied = occupied;
    }

    public String getDirection() {
        return this.direction;
    }

    public Boolean isOccupied() {
        return this.isOccupied;
    }

    public Position getPosition() {
        return this.position.getPosition();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        result = prime * result + ((direction == null) ? 0 : direction.hashCode());
        result = prime * result + (isOccupied ? 1231 : 1237);
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
        Cell other = (Cell) obj;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        if (direction == null) {
            if (other.direction != null)
                return false;
        } else if (!direction.equals(other.direction))
            return false;
        if (isOccupied != other.isOccupied)
            return false;
        return true;
    }

}
