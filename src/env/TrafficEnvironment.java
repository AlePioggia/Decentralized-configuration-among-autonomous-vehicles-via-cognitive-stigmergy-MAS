import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cartago.Artifact;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class TrafficEnvironment extends Artifact implements TurnDiscoveryListener {

    private MovementManager movementManager;
    private TurnDiscoveryService turnDiscoveryService;
    private PerceptionObserver perceptionObserver;

    private Grid grid;
    private Map<String, String> agentActions;
    private Map<String, Position> agentPositions;
    private List<Road> roads;

    private Timer timer;
    private int interval = 1000;
    private boolean running = false;

    public void init() {
        initializeData();
        initializeServices();
        setupEnvironment();
        startSimulation();
    }

    private void initializeData() {
        this.grid = new Grid(10, 10);
        this.agentActions = new HashMap<>();
        this.agentPositions = new HashMap<>();
        this.roads = new ArrayList<>();
    }

    private void initializeServices() {
        this.movementManager = new MovementManager(grid, agentPositions, roads);
        List<Turn> availableTurns = setupAvailableTurns();
        this.turnDiscoveryService = new TurnDiscoveryService(availableTurns);
        this.turnDiscoveryService.addListener(this);

        PerceptionObserver.PerceptionCallback callback = new PerceptionObserver.PerceptionCallback() {
            @Override
            public void defineObsProperty(String property, Object... args) {
                TrafficEnvironment.this.defineObsProperty(property, args);
            }

            @Override
            public void removeObsPropertyByTemplate(String property, Object... args) {
                try {
                    TrafficEnvironment.this.removeObsPropertyByTemplate(property, args);
                } catch (Exception e) {
                }
            }
        };
        this.perceptionObserver = new PerceptionObserver(callback);

        ActionHandlerFactory.registerHandler("follow", new FollowActionHandler());
        ActionHandlerFactory.registerHandler("turn", new TurnActionHandler(turnDiscoveryService));
        ActionHandlerFactory.registerHandler("wait", new DefaultActionHandler());
        ActionHandlerFactory.registerHandler("default", new DefaultActionHandler());
    }

    private void setupEnvironment() {
        setupRoads();
        spawnAgents();
        updatePerceptions();
    }

    private void startSimulation() {
        setupTimer();
    }

    @OPERATION
    public void explore(String agentId, int x, int y, String currentDirection) {
        Position currentPosition = new Position(x, y);
        turnDiscoveryService.exploreTurns(agentId, currentPosition);
    }

    @OPERATION
    void writeIntent(String agent, String action) {
        synchronized (agentActions) {
            this.agentActions.put(agent, action);
        }
        updateIntentions();
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
                executeActions(actionsCopy);
            }

            updatePerceptions();
            signal("step_completed");

        } finally {
            running = false;
        }
    }

    private void executeActions(Map<String, String> actions) {
        for (Map.Entry<String, String> entry : actions.entrySet()) {
            String agent = entry.getKey();
            String action = entry.getValue();

            MovementResult result = movementManager.executeAction(agent, action);

            System.out.println("[Result] " + result.toString());
        }
    }

    @Override
    public void onTurnDiscovered(String agentId, Turn turn) {
        perceptionObserver.notifyTurnAvailable(turn);

        signal("turn_discovered", agentId,
                turn.getFromPosition().getX(),
                turn.getFromPosition().getY(),
                turn.getToPosition().getX(),
                turn.getToPosition().getY());
    }

    private List<Turn> setupAvailableTurns() {
        List<Turn> turns = new ArrayList<>();
        turns.add(new Turn(new Position(2, 1), new Position(2, 2)));
        return turns;
    }

    private void setupRoads() {
        RoadFactory roadFactory = new BasicRoadFactoryImpl();
        Road mainRoad = roadFactory.createHorizontalRoad(0, 5, 0, 1);
        Road crossRoad = roadFactory.createVerticalRoad(2, 3, 2, 3);
        this.roads.addAll(List.of(mainRoad, crossRoad));

        this.roads.forEach(road -> {
            road.getLines().forEach(cell -> {
                this.grid.setCell(cell);
            });
        });
    }

    private void spawnAgents() {
        spawnAgent("vehicle1", new Position(0, 1));
        spawnAgent("vehicle2", new Position(4, 0));
    }

    private void spawnAgent(String agentId, Position position) {
        Cell cell = grid.getCell(position.getX(), position.getY());
        if (cell != null && !cell.isOccupied() && isInsideAnyRoad(position)) {
            cell.setOccupied(true);
            this.agentPositions.put(agentId, position);
        }
    }

    private boolean isInsideAnyRoad(Position position) {
        return this.roads.stream()
                .anyMatch(road -> road.getLines().contains(grid.getCell(position.getX(), position.getY())));
    }

    private void setupTimer() {
        timer = new Timer("TrafficEnvironmentTimer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    execInternalOp("step");
                } catch (Exception ex) {
                    System.err.println("Error in timer: " + ex.getMessage());
                }
            }
        }, interval, interval);

        defineObsProperty("interval", interval);
    }

    private void updatePerceptions() {
        perceptionObserver.updateAgentPositions(agentPositions, grid);
    }

    private void updateIntentions() {
        try {
            removeObsPropertyByTemplate("agent_intention", null, null, null, null);
        } catch (Exception e) {
        }

        synchronized (this.agentActions) {
            for (Map.Entry<String, String> entry : this.agentActions.entrySet()) {
                String agent = entry.getKey();
                String action = entry.getValue();
                Position currentPosition = agentPositions.get(agent);

                if (currentPosition != null) {
                    Position desiredPosition = calculateIntendedPosition(agent, action, currentPosition);

                    if (desiredPosition != null) {
                        defineObsProperty("agent_intention", agent,
                                desiredPosition.getX(),
                                desiredPosition.getY(),
                                action);
                    }
                }
            }
        }
    }

    private Position calculateIntendedPosition(String agent, String action, Position currentPosition) {
        if (action.equals("wait")) {
            return currentPosition;
        } else if (action.startsWith("turn:")) {
            try {
                String[] parts = action.substring(5).split(",");
                return new Position(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            } catch (Exception e) {
                return currentPosition;
            }
        } else {
            Cell cell = grid.getCell(currentPosition.getX(), currentPosition.getY());
            return computeNextPosition(cell);
        }
    }

    private Position computeNextPosition(Cell cell) {
        int x = cell.getPosition().getX();
        int y = cell.getPosition().getY();

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

    @OPERATION
    void readIntents(OpFeedbackParam<List<String>> intents) {
        List<String> agentActionsList = agentActions.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .toList();
        intents.set(agentActionsList);
    }

    @OPERATION
    public void stopSimulation() {
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    @OPERATION
    public void hasTrafficLightAt(int x, int y, OpFeedbackParam<Boolean> result) {
        result.set(false);
    }
}