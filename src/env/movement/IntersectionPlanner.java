package movement;

import java.util.List;

import core.Cell;
import core.Grid;
import core.Position;
import road.Road;

public class IntersectionPlanner {
    private final Grid grid;
    private final List<Road> roads;

    public IntersectionPlanner(Grid grid, List<Road> roads) {
        this.grid = grid;
        this.roads = roads;
    }

    public Position computeNext(Position currentPosition, String action) {
        if (currentPosition == null || action == null || !action.startsWith("intersection:"))
            return null;

        Cell currentCell = grid.getCell(currentPosition.getX(), currentPosition.getY());
        if (currentCell == null) {
            return null;
        }

        String parsedDirection = action.substring("intersection:".length()).trim().toLowerCase();
        String targetDirection = isDirectionAbsolute(parsedDirection) ? parsedDirection
                : rotateToRelative(currentCell.getDirection(), parsedDirection);

        int[][] offsets = orderedOffsetsFor(targetDirection);

        for (int[] off : offsets) {
            int nx = currentPosition.getX() + off[0];
            int ny = currentPosition.getY() + off[1];
            Position cand = new Position(nx, ny);

            if (!core.Utils.isValidPosition(cand, grid, roads))
                continue;

            Cell c = grid.getCell(nx, ny);
            if (c == null)
                continue;

            if (!targetDirection.equalsIgnoreCase(c.getDirection()))
                continue;

            return cand;
        }
        return null;
    }

    private boolean isDirectionAbsolute(String t) {
        return t.equals("north") || t.equals("south") || t.equals("east") || t.equals("west");
    }

    private String rotateToRelative(String absoluteDirection, String relativeDirection) {
        switch (absoluteDirection) {
            case "North":
                return switch (relativeDirection) {
                    case "right" -> "east";
                    case "left" -> "west";
                    case "straight" -> "north";
                    default -> null;
                };
            case "South":
                return switch (relativeDirection) {
                    case "right" -> "west";
                    case "left" -> "east";
                    case "straight" -> "south";
                    default -> null;
                };
            case "East":
                return switch (relativeDirection) {
                    case "right" -> "south";
                    case "left" -> "north";
                    case "straight" -> "east";
                    default -> null;
                };
            case "West":
                return switch (relativeDirection) {
                    case "right" -> "north";
                    case "left" -> "south";
                    case "straight" -> "west";
                    default -> null;
                };
            default:
                return null;
        }
    }

    private int[][] orderedOffsetsFor(String targetDir) {
        switch (targetDir) {
            case "east":
                return new int[][] { { +1, 0 }, { +1, +1 }, { +1, -1 } };
            case "west":
                return new int[][] { { -1, 0 }, { -1, -1 }, { -1, +1 } };
            case "south":
                return new int[][] { { 0, +1 }, { -1, +1 }, { +1, +1 } };
            case "north":
                return new int[][] { { 0, -1 }, { +1, -1 }, { -1, -1 } };
            default:
                return new int[0][];
        }
    }

}
