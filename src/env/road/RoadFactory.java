package road;

public interface RoadFactory {
    public Road createHorizontalRoad(int x, int length, int y1, int y2);

    public Road createVerticalRoad(int x1, int x2, int y, int length);

    public Road createHorizontalRoadFromTo(int startX, int endX, int y1, int y2);

    public Road createVerticalRoadFromTo(int x1, int x2, int startY, int endY);
}