import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import cartago.Artifact;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class TrafficEnvironment extends Artifact {

    private Grid grid;
    private Map<String, String> agentActions;
    private Map<String, Position> agentPositions;
    private Road road;
    private List<String> observableProperties;

    private Timer timer;
    private int interval = 1000;
    private boolean running = false;

    private List<Position> trafficLightPositions;

    public void init() {
        this.grid = new Grid(10, 10);
        this.agentActions = new HashMap<>();
        this.agentPositions = new HashMap<>();
        this.observableProperties = new ArrayList<>();
        this.trafficLightPositions = new ArrayList<>();

        setupEnvironmentRoads();
        setupSimulationTimer();
        setupTrafficLights();
    }

    private void setupEnvironmentRoads() {
        RoadFactory roadFactory = new BasicRoadFactoryImpl();
        this.road = roadFactory.create(0, 5, 1, 2);

        for (Cell cell : this.road.getLeftLine()) {
            this.grid.setCell(cell);
        }

        for (Cell cell : this.road.getRightLine()) {
            this.grid.setCell(cell);
        }

        spawnAgent("vehicle1", new Position(0, 1));
        spawnAgent("vehicle2", new Position(4, 2));
        updateObservableProperties();
    }

    private void setupSimulationTimer() {
        timer = new Timer("TrafficEnvironmentTimer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    execInternalOp("step");
                } catch (Exception ex) {
                    System.err.println("Error in TrafficEnvironment timer: " + ex.getMessage());
                }
            }
        }, interval, interval);

        defineObsProperty("interval", interval);
    }

    private void setupTrafficLights() {
        this.trafficLightPositions.add(new Position(2, 1));
        this.trafficLightPositions.add(new Position(2, 2));

        // for (Position pos : this.trafficLightPositions) {
        // defineObsProperty("traffic_light_position", pos.getX(), pos.getY());
        // }
    }

    @OPERATION
    public void hasTrafficLightAt(int x, int y, cartago.OpFeedbackParam<Boolean> result) {
        boolean hasLight = this.trafficLightPositions.stream()
                .anyMatch(pos -> pos.getX() == x && pos.getY() == y);
        result.set(hasLight);
    }

    @INTERNAL_OPERATION
    public void step() {
        if (running)
            return;
        try {
            running = true;
            Map<String, String> actionsCopy;
            synchronized (agentActions) {
                actionsCopy = new HashMap<>(this.agentActions);
                this.agentActions.clear();
            }

            if (!actionsCopy.isEmpty()) {
                handleStepLogic(actionsCopy);
            }

            signal("step_completed");
        } catch (Exception e) {
            System.err.println("Error during step execution: " + e.getMessage());
        } finally {
            running = false;
        }
    }

    private void handleStepLogic(Map<String, String> actionsCopy) {
        for (var entry : actionsCopy.entrySet()) {
            String agent = entry.getKey();
            String action = entry.getValue();
            Cell cell = grid.getCell(agentPositions.get(agent).getX(),
                    agentPositions.get(agent).getY());

            Position currentPosition = cell.getPosition();

            if (currentPosition != null) {
                Position nextPosition = computeNextPosition(cell, action);

                if (nextPosition != null && isPositionWithinBounds(nextPosition)
                        && grid.getCell(nextPosition.getX(), nextPosition.getY()) != null
                        && road.getLines().contains(grid.getCell(nextPosition.getX(),
                                nextPosition.getY()))
                        && !grid.getCell(nextPosition.getX(), nextPosition.getY()).isOccupied()) {

                    agentPositions.put(agent, nextPosition);
                    grid.getCell(currentPosition.getX(),
                            currentPosition.getY()).setOccupied(false);
                    grid.getCell(nextPosition.getX(), nextPosition.getY()).setOccupied(true);
                    System.out.println("Agent " + agent + " moved to position: " + nextPosition.getX() + ", "
                            + nextPosition.getY());
                } else {
                    System.out.println(
                            "Agent " + agent + " cannot move, next position is out of bounds or occupied.");
                }
            }
        }
        updateObservableProperties();
    }

    @OPERATION
    public void stopSimulation() {
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    private void spawnAgent(String agentId, Position position) {
        Cell cell = grid.getCell(position.getX(), position.getY());
        if (cell != null && !cell.isOccupied() && this.road.getLines().contains(cell)) {
            cell.setOccupied(true);
            this.agentPositions.put(agentId, position);
        }
    }

    private void updateIntentions() {
        try {
            removeObsPropertyByTemplate("agent_intention", null, null, null, null);
        } catch (IllegalArgumentException e) {
        }

        synchronized (this.agentActions) {
            for (Map.Entry<String, String> entry : this.agentActions.entrySet()) {
                String agent = entry.getKey();
                String action = entry.getValue();
                Position currentPosition = agentPositions.get(agent);

                if (currentPosition != null && action.equals("follow")) {
                    Cell currentCell = grid.getCell(currentPosition.getX(), currentPosition.getY());
                    Position desiredPosition = computeNextPosition(currentCell, action);

                    if (desiredPosition != null) {
                        defineObsProperty("agent_intention", agent, desiredPosition.getX(), desiredPosition.getY(),
                                action);
                        this.observableProperties.add("agent_intention");
                    }
                }
            }
        }
    }

    @OPERATION
    void writeIntent(String agent, String action) {
        synchronized (agentActions) {
            this.agentActions.put(agent, action);
        }
        updateIntentions();
    }

    @OPERATION
    void readIntents(OpFeedbackParam<List<String>> intents) {
        List<String> agentActionsList = collectIntents();
        intents.set(agentActionsList);
        System.out.println("Current agent intents: " + agentActions);
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

    // private void clearObservableProperties() {
    // for (String prop : new ArrayList<>(this.observableProperties)) {
    // removeObsProperty(prop);
    // }
    // this.observableProperties.clear();
    // }

    private void updateObservableProperties() {
        try {
            removeObsPropertyByTemplate("at", null, null, null);
            removeObsPropertyByTemplate("occupied", null, null);
            removeObsPropertyByTemplate("direction", null, null, null);
        } catch (Exception e) {
        }

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