package core;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import road.Road;

public class Utils {

    public static Position computeNextPosition(Cell cell, Grid grid) {
        int x = cell.getPosition().getX();
        int y = cell.getPosition().getY();

        switch (cell.getDirection()) {
            case "North":
                return wrapPosition(x, y - 1, grid);
            case "South":
                return wrapPosition(x, y + 1, grid);
            case "East":
                return wrapPosition(x + 1, y, grid);
            case "West":
                return wrapPosition(x - 1, y, grid);
            default:
                return wrapPosition(x, y, grid);
        }
    }

    private static Position wrapPosition(int x, int y, Grid grid) {
        int wrappedX = (x + grid.getWidth()) % grid.getWidth();
        int wrappedY = (y + grid.getHeight()) % grid.getHeight();
        return new Position(wrappedX, wrappedY);
    }

    public static Position parseTurnAction(String action) {
        try {
            String[] parts = action.substring(5).split(",");
            if (parts.length == 2) {
                int toX = Integer.parseInt(parts[0].trim());
                int toY = Integer.parseInt(parts[1].trim());
                return new Position(toX, toY);
            }
        } catch (Exception e) {
            System.err.println("Error parsing turn action: " + action);
        }
        return null;
    }

    public static boolean isValidPosition(Position position, Grid grid, List<Road> roads) {
        return isPositionWithinBounds(position, grid) &&
                grid.getCell(position.getX(), position.getY()) != null &&
                isInsideAnyRoad(position, grid, roads) &&
                !grid.getCell(position.getX(), position.getY()).isOccupied();
    }

    public static boolean isPositionWithinBounds(Position position, Grid grid) {
        return position.getX() >= 0 && position.getX() < grid.getWidth() &&
                position.getY() >= 0 && position.getY() < grid.getHeight();
    }

    public static boolean isInsideAnyRoad(Position position, Grid grid, List<Road> roads) {
        return roads.stream()
                .anyMatch(road -> road.getLines().contains(grid.getCell(position.getX(), position.getY())));
    }

    public static Timer createTimer(String name, Runnable task, long delay, long period) {
        Timer timer = new Timer(name, true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, delay, period);
        return timer;
    }
}
