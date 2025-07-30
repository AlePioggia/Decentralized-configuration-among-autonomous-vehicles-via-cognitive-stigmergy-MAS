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

        spawnAgent("me", new Position(0, 1));
        // spawnAgent("agent2", new Position(0, 2));
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

            if (action.equals("forward")) {
                Position currentPosition = agentPositions.get(agent);
                if (currentPosition != null) {
                    Position nextPosition = new Position(currentPosition.getX() + 1, currentPosition.getY());

                    if (nextPosition != null & grid.getCell(nextPosition.getX(), nextPosition.getY()) != null
                            && road.getLines().contains(grid.getCell(nextPosition.getX(), nextPosition.getY()))) {
                        agentPositions.put(agent, nextPosition);

                        grid.getCell(currentPosition.getX(), currentPosition.getY()).setOccupied(false);
                        grid.getCell(nextPosition.getX(), nextPosition.getY()).setOccupied(true);
                        System.out.println("Agent " + agent + " moved to position: " + nextPosition);
                    } else {
                        System.out.println(
                                "Agent " + agent + " cannot move forward, next position is out of bounds or occupied.");
                    }
                }
            }
            agentActions.clear();
            updateObservableProperties();
        }
    }

    private List<String> collectIntents() {
        return agentActions.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.toList());
    }

    private void clearObservableProperties() {
        for (String prop : observableProperties) {
            removeObsProperty(prop);
        }
        observableProperties.clear();
    }

    private void updateObservableProperties() {
        clearObservableProperties();

        for (Map.Entry<String, Position> entry : agentPositions.entrySet()) {
            String agent = entry.getKey();
            Position pos = entry.getValue();

            defineObsProperty("at", agent, pos.getX(), pos.getY());
            defineObsProperty("occupied", pos.getX(), pos.getY());
        }
    }

}