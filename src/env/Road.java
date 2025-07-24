import java.util.List;

public class Road {
    private final String roadName;
    private List<Cell> leftLine;
    private List<Cell> rightLine;

    public Road(String roadName, List<Cell> leftLine, List<Cell> rightLine) {
        this.roadName = roadName;
        this.leftLine = leftLine;
        this.rightLine = rightLine;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setLines(List<Cell> leftLine, List<Cell> rightLine) {
        this.leftLine = leftLine;
        this.rightLine = rightLine;
    }

    public void setLeftLine(List<Cell> leftLine) {
        this.leftLine = leftLine;
    }

    public void setRightLine(List<Cell> rightLine) {
        this.rightLine = rightLine;
    }

    public List<Cell> getLeftLine() {
        return leftLine;
    }

    public List<Cell> getRightLine() {
        return rightLine;
    }
}
