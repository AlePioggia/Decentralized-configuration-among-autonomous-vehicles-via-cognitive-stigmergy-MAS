import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private List<Road> roads;
    private List<String> observableProperties;

    private Timer timer;
    private int interval = 1000;
    private boolean running = false;

    private List<Position> trafficLightPositions;
    private List<Turn> availableTurns;
    private Set<Turn> discoveredTurns;

    public void init() {
        this.grid = new Grid(10, 10);
        this.agentActions = new HashMap<>();
        this.agentPositions = new HashMap<>();
        this.observableProperties = new ArrayList<>();
        this.trafficLightPositions = new ArrayList<>();
        this.roads = new ArrayList<>();
        this.availableTurns = new ArrayList<>();
        this.discoveredTurns = new HashSet<>();

        setupEnvironmentRoads();
        setupSimulationTimer();
        // setupTrafficLights();
        setupTurns();
    }

    private void setupTurns() {
        Turn rightTurn = new Turn(new Position(2, 1), new Position(2, 2));
        this.availableTurns.add(rightTurn);
    }

    private void setupEnvironmentRoads() {
        RoadFactory roadFactory = new BasicRoadFactoryImpl();
        Road mainRoad = roadFactory.createHorizontalRoad(0, 5, 0, 1);
        Road crossRoad = roadFactory.createVerticalRoad(2, 3, 2, 3);
        this.roads.addAll(List.of(mainRoad, crossRoad));

        this.roads.forEach(road -> {
            road.getLines().forEach(cell -> {
                this.grid.setCell(cell);
            });
        });

        spawnAgent("vehicle1", new Position(0, 1));
        spawnAgent("vehicle2", new Position(4, 0));
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
    }

    @OPERATION
    public void explore(String agentId, int x, int y, String currentDirection) {
        Position currentPosition = new Position(x, y);
        System.out.println(
                "currently discovering turns from position: " + currentPosition.getX() + ", " + currentPosition.getY());

        List<Turn> possibleTurns = this.availableTurns.stream()
                .filter(turn -> {
                    Position fromPosition = turn.getFromPosition();
                    System.out
                            .println("Checking turn to position: " + fromPosition.getX() + ", " + fromPosition.getY());
                    System.out.println("Current position: " + currentPosition.getX() + ", " + currentPosition.getY());
                    // Check if the turn's toPosition matches the provided x and y
                    return fromPosition.getX() == x && fromPosition.getY() == y;
                })
                .collect(Collectors.toList());

        for (Turn turn : possibleTurns) {
            if (!this.discoveredTurns.contains(turn)) {
                this.discoveredTurns.add(turn);

                defineObsProperty("turn_available",
                        turn.getFromPosition().getX(),
                        turn.getFromPosition().getY(),
                        turn.getToPosition().getX(),
                        turn.getToPosition().getY());

                signal("turn_discovered", agentId,
                        turn.getFromPosition().getX(),
                        turn.getFromPosition().getY(),
                        turn.getToPosition().getX(),
                        turn.getToPosition().getY());
            }
        }

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
            Position nextPosition = null;

            if (currentPosition != null) {

                // action handling
                if (action.equals("follow")) {
                    nextPosition = computeNextPosition(cell, action);
                } else if (action.startsWith("turn:")) {
                    System.out.println("[handleStepLogic] Processing turn action for agent [handleStepLogic]");
                    System.out.println("[handleStepLogic] Action: " + action);
                    nextPosition = parseTurnAction(agent, action);
                    if (nextPosition != null) {
                        Turn turn = new Turn(currentPosition, nextPosition);
                        if (!this.discoveredTurns.contains(turn)) {
                            nextPosition = computeNextPosition(cell, action);
                        }
                    }
                } else if (action.equals("wait")) {
                    nextPosition = currentPosition;
                } else {
                    nextPosition = currentPosition;
                }

                // action execution
                if (nextPosition != null && !nextPosition.equals(currentPosition)) {
                    if (isPositionWithinBounds(nextPosition)
                            && grid.getCell(nextPosition.getX(), nextPosition.getY()) != null
                            && isInsideAnyRoad(nextPosition)
                            && !grid.getCell(nextPosition.getX(), nextPosition.getY()).isOccupied()) {
                        System.out.println("executing action: " + action + " for agent: " + agent);

                        agentPositions.put(agent, nextPosition);
                        grid.getCell(currentPosition.getX(),
                                currentPosition.getY()).setOccupied(false);
                        grid.getCell(nextPosition.getX(), nextPosition.getY()).setOccupied(true);
                        System.out.println("Agent " + agent + " moved to position: " + nextPosition.getX() + ", "
                                + nextPosition.getY());
                    }
                } else {
                    System.out.println("No movement for agent: " + agent);
                }
            }
        }
        updateObservableProperties();
    }

    private boolean isInsideAnyRoad(Position position) {
        return this.roads.stream()
                .anyMatch(road -> road.getLines().contains(grid.getCell(position.getX(), position.getY())));
    }

    @OPERATION
    public void stopSimulation() {
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    private void spawnAgent(String agentId, Position position) {
        Cell cell = grid.getCell(position.getX(), position.getY());
        if (cell != null && !cell.isOccupied() && isInsideAnyRoad(cell.getPosition())) {
            cell.setOccupied(true);
            this.agentPositions.put(agentId, position);
        }
    }

    @OPERATION
    void writeIntent(String agent, String action) {
        System.out.println("Write intent called: " + agent + " -> " + action);

        synchronized (agentActions) {
            this.agentActions.put(agent, action);
            System.out.println("Actions map size: " + this.agentActions.size());
        }
        updateIntentions();
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
                Cell currentCell = grid.getCell(currentPosition.getX(), currentPosition.getY());
                Position desiredPosition = null;

                if (currentPosition != null) {
                    if (action.equals("follow")) {

                        if (computeNextPosition(currentCell, action) != null) {
                            desiredPosition = computeNextPosition(currentCell, action);
                            defineObsProperty("agent_intention", agent, desiredPosition.getX(), desiredPosition.getY(),
                                    action);
                            this.observableProperties.add("agent_intention");
                        }
                    } else if (action.startsWith("turn:")) {
                        Position destination = parseTurnAction(agent, action);

                        if (destination != null) {
                            Turn turn = new Turn(currentPosition, destination);
                            if (this.discoveredTurns.contains(turn)) {
                                desiredPosition = turn.getToPosition();
                            } else {
                                desiredPosition = computeNextPosition(currentCell, action);
                            }
                        }
                    } else if (action.equals("wait")) {
                        desiredPosition = currentPosition;
                    }

                    if (desiredPosition != null) {
                        defineObsProperty("agent_intention", agent,
                                desiredPosition.getX(),
                                desiredPosition.getY(),
                                action);
                        this.observableProperties.add("agent_intention");
                    }
                }
            }
        }
    }

    private Position parseTurnAction(String agentId, String action) {
        try {
            String[] parts = action.substring(5).split(",");
            if (parts.length == 2) {
                int toX = Integer.parseInt(parts[0]);
                int toY = Integer.parseInt(parts[1]);
                return new Position(toX, toY);
            }
        } catch (Exception e) {

        } finally {

        }
        return null;
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