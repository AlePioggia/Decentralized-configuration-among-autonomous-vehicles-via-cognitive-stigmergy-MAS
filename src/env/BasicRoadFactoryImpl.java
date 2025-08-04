import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class BasicRoadFactoryImpl implements RoadFactory {
    @Override
    public Road createHorizontalRoad(int x, int length, int y1, int y2) {
        List<Cell> mainLane = new ArrayList<>();
        List<Cell> secondLane = new ArrayList<>();

        IntStream.range(x, x + length).forEach(i -> {
            Cell mainCell = new Cell(i, y1, "West");
            Cell secondCell = new Cell(i, y2, "East");
            mainLane.add(mainCell);
            secondLane.add(secondCell);
        });

        return new Road("roadName", mainLane, secondLane);
    }

    @Override
    public Road createVerticalRoad(int x1, int x2, int y, int length) {
        List<Cell> mainLane = new ArrayList<>();
        List<Cell> secondLane = new ArrayList<>();

        IntStream.range(y, y + length).forEach(i -> {
            Cell mainCell = new Cell(x1, i, "North");
            Cell secondCell = new Cell(x2, i, "South");
            mainLane.add(mainCell);
            secondLane.add(secondCell);
        });

        return new Road("roadName", mainLane, secondLane);
    }
}