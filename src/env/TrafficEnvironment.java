import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class TrafficEnvironment extends Artifact {

    private Grid grid;
    private Map<String, String> agentActions;
    private Map<String, Position> agentPositions;
    private Road road;
    private List<String> observableProperties;

    void init() {
        this.grid = new Grid(10, 10);
        this.agentActions = new HashMap<>();
        this.agentPositions = new HashMap<>();
        this.observableProperties = new ArrayList<>();

        RoadFactory roadFactory = new BasicRoadFactoryImpl();
        this.road = roadFactory.create(0, 5, 1, 2);

        for (Cell cell : this.road.getLeftLine()) {
            this.grid.setCell(cell);
        }

        for (Cell cell : this.road.getRightLine()) {
            this.grid.setCell(cell);
        }

        spawnAgent("vehicle1", new Position(0, 1));
        // spawnAgent("vehicle2", new Position(4, 2));
        updateObservableProperties();
    }

    private void spawnAgent(String agentId, Position position) {
        Cell cell = grid.getCell(position.getX(), position.getY());
        if (cell != null && !cell.isOccupied() && this.road.getLines().contains(cell)) {
            cell.setOccupied(true);
            this.agentPositions.put(agentId, position);
        }
    }

    @OPERATION
    void writeIntent(String agent, String action) {
        agentActions.put(agent, action);
        System.out.println("Agent " + agent + " intends to perform action: " + action);
    }

    @OPERATION
    void readIntents(OpFeedbackParam<List<String>> intents) {
        List<String> agentActionsList = collectIntents();
        intents.set(agentActionsList);
        System.out.println("Current agent intents: " + agentActions);
    }

    @OPERATION
    void step() {
        for (var entry : agentActions.entrySet()) {
            String agent = entry.getKey();
            String action = entry.getValue();
            Cell cell = grid.getCell(agentPositions.get(agent).getX(), agentPositions.get(agent).getY());

            Position currentPosition = cell.getPosition();

            if (currentPosition != null) {
                Position nextPosition = computeNextPosition(cell, action);

                if (nextPosition != null && isPositionWithinBounds(nextPosition)
                        && grid.getCell(nextPosition.getX(), nextPosition.getY()) != null
                        && road.getLines().contains(grid.getCell(nextPosition.getX(), nextPosition.getY()))) {

                    agentPositions.put(agent, nextPosition);
                    grid.getCell(currentPosition.getX(), currentPosition.getY()).setOccupied(false);
                    grid.getCell(nextPosition.getX(), nextPosition.getY()).setOccupied(true);
                    System.out.println("Agent " + agent + " moved to position: " + nextPosition);
                } else {
                    System.out.println(
                            "Agent " + agent + " cannot move, next position is out of bounds or occupied.");
                }
            }
            agentActions.clear();
            updateObservableProperties();
        }
    }

    private Boolean isPositionWithinBounds(Position position) {
        return position.getX() >= 0 && position.getX() < grid.getWidth()
                && position.getY() >= 0 && position.getY() < grid.getHeight();
    }

    private Position computeNextPosition(Cell cell, String action) {
        int x = cell.getPosition().getX();
        int y = cell.getPosition().getY();

        if (action.equals("follow")) {
            switch (cell.getDirection()) {
                case "North":
                    return new Position(x, y - 1);
                case "South":
                    return new Position(x, y + 1);
                case "East":
                    return new Position(x + 1, y);
                case "West":
                    return new Position(x - 1, y);
                default:
                    return cell.getPosition();
            }
        }
        return cell.getPosition();
    }

    private List<String> collectIntents() {
        return agentActions.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.toList());
    }

    private void clearObservableProperties() {
        for (String prop : this.observableProperties) {
            removeObsProperty(prop);
        }
        this.observableProperties.clear();
    }

    private void updateObservableProperties() {
        clearObservableProperties();

        for (Map.Entry<String, Position> entry : agentPositions.entrySet()) {
            String agent = entry.getKey();
            Position pos = entry.getValue();

            Cell cell = grid.getCell(pos.getX(), pos.getY());
            if (cell.getDirection() != null) {
                var direction = cell.getDirection();
                defineObsProperty("direction", pos.getX(), pos.getY(), direction.toString());
                this.observableProperties.add("direction");
            }
            defineObsProperty("at", agent, pos.getX(), pos.getY());
            defineObsProperty("occupied", pos.getX(), pos.getY());

            this.observableProperties.add("at");
            this.observableProperties.add("occupied");
        }
    }

}