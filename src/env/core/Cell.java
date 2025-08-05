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
}
