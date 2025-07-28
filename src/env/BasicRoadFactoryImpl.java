import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class BasicRoadFactoryImpl implements RoadFactory {
    @Override
    public Road create(int x, int length, int y1, int y2) {
        List<Cell> mainLane = new ArrayList<>();
        List<Cell> secondLane = new ArrayList<>();

        IntStream.range(x, x + length).forEach(i -> {
            Cell mainCell = new Cell(i, y1, "East");
            Cell secondCell = new Cell(i, y2, "West");
            mainLane.add(mainCell);
            secondLane.add(secondCell);
        });

        return new Road("roadName", mainLane, secondLane);
    }
}