package movement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import core.Cell;
import core.Grid;
import core.Position;
import discovery.Intersection;
import road.Road;

public class IntersectionPlanner {
    private final Grid grid;
    private final List<Road> roads;
    private final List<Intersection> allFootprints;

    public IntersectionPlanner(Grid grid, List<Road> roads) {
        this.grid = grid;
        this.roads = roads;
        this.allFootprints = new ArrayList<>();
    }

    public void setFootprints(List<Intersection> footprints) {
        this.allFootprints.clear();
        if (footprints != null) {
            this.allFootprints.addAll(footprints);
        }
    }

    public Position computeNext(Position currentPosition, String action) {
        if (currentPosition == null || action == null || !action.startsWith("intersection:")) {
            System.out.println("[DEBUG][Planner] Invalid input: currentPosition or action is null.");
            return null;
        }

        Cell currentCell = grid.getCell(currentPosition.getX(), currentPosition.getY());
        if (currentCell == null) {
            System.out.println("[DEBUG][Planner] Current cell is null at " + currentPosition);
            return null;
        }

        String parsedDirection = action.substring("intersection:".length()).trim().toLowerCase();
        String targetDirection = isDirectionAbsolute(parsedDirection) ? parsedDirection
                : rotateToRelative(currentCell.getDirection(), parsedDirection);

        int[][] offsets = orderedOffsetsFor(targetDirection);

        System.out.println("[DEBUG][Planner] " + action + " from " + currentPosition + " dir="
                + currentCell.getDirection() + " -> targetDir=" + targetDirection);

        for (int[] off : offsets) {
            int nx = currentPosition.getX() + off[0];
            int ny = currentPosition.getY() + off[1];
            Position cand = new Position(nx, ny);

            if (!core.Utils.isValidPosition(cand, grid, roads)) {
                System.out.println("[DEBUG][Planner] Candidate " + cand + " is not a valid position.");
                continue;
            }

            Cell c = grid.getCell(nx, ny);
            if (c == null) {
                System.out.println("[DEBUG][Planner] Candidate " + cand + " cell is null.");
                continue;
            }

            boolean isFootprint = isIntersectionFootprint(c);
            boolean dirMatch = c.getDirection() != null && targetDirection.equalsIgnoreCase(c.getDirection());

            System.out.println("[DEBUG][Planner] Checking candidate " + cand + " isFootprint=" + isFootprint + " dir="
                    + c.getDirection() + " dirMatch=" + dirMatch);

            if (isFootprint || dirMatch)
                return cand;
        }
        System.out.println("[DEBUG][Planner] No valid candidate found for " + action + " from " + currentPosition);
        return null;
    }

    // public String getBestIntersectionDirection(Position currentPosition, String
    // currentDirection, Position goalPosition,
    // Map<String, Position> agentPositions) {
    // String[] options = new String[] { "straight", "left", "right" };
    // String bestDir = null;
    // int bestDist = Integer.MAX_VALUE;

    // for (String opt : options) {
    // String absDir = rotateToRelative(currentDirection, opt);
    // if (absDir == null)
    // continue;

    // int[][] offsets = orderedOffsetsFor(absDir);
    // for (int[] off : offsets) {
    // int nx = currentPosition.getX() + off[0];
    // int ny = currentPosition.getY() + off[1];
    // Position nextPos = new Position(nx, ny);

    // if (!core.Utils.isValidPosition(nextPos, grid, roads))
    // continue;

    // Cell nextCell = grid.getCell(nx, ny);
    // if (nextCell == null || nextCell.isOccupied())
    // continue;

    // boolean intended = agentPositions.values().stream().anyMatch(pos ->
    // pos.equals(nextPos));
    // if (intended)
    // continue;

    // int dist = Math.abs(nx - goalPosition.getX()) + Math.abs(ny -
    // goalPosition.getY());
    // if (dist < bestDist) {
    // bestDist = dist;
    // bestDir = opt;
    // }

    // if ((nx == goalPosition.getX() || ny == goalPosition.getY()) && dist <
    // bestDist) {
    // bestDist = dist;
    // bestDir = opt;
    // }
    // }
    // }
    // return bestDir != null ? bestDir : "straight";
    // }

    public String getBestIntersectionDirection(Position currentPosition, String currentDirection,
            Position goalPosition,
            Map<String, Position> agentPositions) {
        String[] options = new String[] { "straight", "left", "right" };
        String bestDir = null;
        int minSteps = Integer.MAX_VALUE;

        for (String opt : options) {
            String absDir = rotateToRelative(currentDirection, opt);
            int[][] offsets = orderedOffsetsFor(absDir);
            if (offsets.length == 0)
                continue;
            int[] off = offsets[0];
            Position startPos = new Position(currentPosition.getX() + off[0], currentPosition.getY() + off[1]);

            int steps = bfsStepsToGoal(startPos, goalPosition, agentPositions);
            if (steps >= 0 && steps < minSteps) {
                minSteps = steps;
                bestDir = opt;
            }
        }
        return bestDir != null ? bestDir : "straight";
    }

    private int bfsStepsToGoal(Position start, Position goal, Map<String, Position> agentPositions) {
        Set<Position> visited = new HashSet<>();
        Queue<Position> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);
        int steps = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Position pos = queue.poll();
                if (pos.equals(goal))
                    return steps;
                for (int[] off : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } }) {
                    Position next = new Position(pos.getX() + off[0], pos.getY() + off[1]);
                    if (next.getX() < 0 || next.getX() >= grid.getWidth() ||
                            next.getY() < 0 || next.getY() >= grid.getHeight())
                        continue;
                    if (visited.contains(next))
                        continue;
                    Cell cell = grid.getCell(next.getX(), next.getY());
                    if (cell == null || cell.isOccupied() || !core.Utils.isValidPosition(next, grid, roads))
                        continue;
                    visited.add(next);
                    queue.add(next);
                }
            }
            steps++;
        }
        return -1;
    }

    private boolean isIntersectionFootprint(Cell c) {
        return this.allFootprints.stream()
                .anyMatch(footprint -> footprint.getFootPrint().contains(c.getPosition()));
    }

    private boolean isDirectionAbsolute(String t) {
        return t.equals("north") || t.equals("south") || t.equals("east") || t.equals("west");
    }

    public String rotateToRelative(String absoluteDirection, String relativeDirection) {
        switch (absoluteDirection.toLowerCase()) {
            case "north":
                return switch (relativeDirection) {
                    case "right" -> "east";
                    case "left" -> "west";
                    case "straight" -> "north";
                    default -> null;
                };
            case "south":
                return switch (relativeDirection) {
                    case "right" -> "west";
                    case "left" -> "east";
                    case "straight" -> "south";
                    default -> null;
                };
            case "east":
                return switch (relativeDirection) {
                    case "right" -> "south";
                    case "left" -> "north";
                    case "straight" -> "east";
                    default -> null;
                };
            case "west":
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
        switch (targetDir.toLowerCase()) {
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
