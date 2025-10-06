package movement;

import java.util.List;

import core.Cell;
import core.Grid;
import core.Position;
import discovery.Intersection;

public class ChangeLanePlanner {
    private final Grid grid;
    private final List<Intersection> allFootprints;

    public ChangeLanePlanner(Grid grid, List<Intersection> allFootprints) {
        this.grid = grid;
        this.allFootprints = allFootprints;
    }

    public Position changeLane(Position currentPosition, String currentDirection, String side) {
        int currentX = currentPosition.getX();
        int currentY = currentPosition.getY();
        int nextX = 0;
        int nextY = 0;
        if (currentPosition == null || currentDirection == null || side == null)
            return null;

        if (currentDirection.equalsIgnoreCase("east") || currentDirection.equalsIgnoreCase("west")) {
            nextY = side.equals("left") ? currentY - 1 : currentY + 1;
        } else if (currentDirection.equalsIgnoreCase("north") || currentDirection.equalsIgnoreCase("south")) {
            nextX = side.equals("left") ? currentX - 1 : currentX + 1;
        } else {
            return null;
        }

        Cell currentCell = grid.getCell(currentX, currentY);
        Cell targetCell = grid.getCell(nextX, nextY);
        if (currentCell == null || targetCell == null)
            return null;

        if (isIntersectionFootprint(currentCell.getPosition()) || isIntersectionFootprint(targetCell.getPosition()))
            return null;

        if (targetCell.isOccupied() || targetCell.getDirection() == null)
            return null;
        if (!currentDirection.equalsIgnoreCase(targetCell.getDirection()))
            return null;

        return new Position(nextX, nextY);
    }

    private boolean isIntersectionFootprint(Position position) {
        for (Intersection footprint : allFootprints) {
            if (footprint.getPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }

}
