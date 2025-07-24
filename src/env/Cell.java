public class Cell {
    private Position position;
    private String agentId;
    private String direction;

    public Cell(int x, int y) {
        this.position = new Position(x, y);
        this.direction = "North";
    }

    public Cell(int x, int y, String agentId) {
        this.position = new Position(x, y);
        this.agentId = agentId;
        this.direction = "North";
    }

    public Cell(int x, int y, String agentId, String direction) {
        this.position = new Position(x, y);
        this.agentId = agentId;
        this.direction = direction;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }

    public Boolean isOccupied() {
        return agentId != null && !agentId.isEmpty();
    }

    public Position getPosition() {
        return position.getPosition();
    }

    public String getAgentId() {
        return agentId;
    }
}
