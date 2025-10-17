package traffic;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;

import cartago.Artifact;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.ObsProperty;
import cartago.OpFeedbackParam;
import core.Cell;
import core.Grid;
import core.Position;
import discovery.Intersection;
import discovery.IntersectionDiscoveryListener;
import discovery.IntersectionDiscoveryService;
import discovery.Turn;
import discovery.TurnDiscoveryListener;
import discovery.TurnDiscoveryService;
import metrics.Metrics;
import movement.ActionHandlerFactory;
import movement.ChangeLaneActionHandler;
import movement.ChangeLanePlanner;
import movement.DefaultActionHandler;
import movement.FollowActionHandler;
import movement.IntersectionActionHandler;
import movement.IntersectionPlanner;
import movement.MovementManager;
import movement.MovementResult;
import movement.TurnActionHandler;
import perception.PerceptionObserver;
import road.BasicRoadFactoryImpl;
import road.Road;
import road.RoadFactory;
import road.RoadLayout;
import road.RoadLayoutGenerator;
import core.Utils;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import view.MapPanel;

public class TrafficEnvironment extends Artifact implements TurnDiscoveryListener, IntersectionDiscoveryListener {

    private MovementManager movementManager;
    private TurnDiscoveryService turnDiscoveryService;
    private IntersectionDiscoveryService intersectionDiscoveryService;
    private PerceptionObserver perceptionObserver;

    private Grid grid;
    private Map<String, String> agentActions;
    private Map<String, Position> agentIntentions;
    private Map<String, Position> agentPositions;
    private List<Road> roads;
    private List<Intersection> allFootprints;
    private Map<String, Position> goalsPositions;
    private Map<String, Position> discoveredGoals;
    private MapPanel mapPanel;
    private Metrics metrics = new Metrics();

    private Timer timer;
    private int interval = 2000;
    private boolean running = false;

    public void init() {
        initializeData();
        initializeServices();
        setupEnvironment();
        startSimulation();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Traffic Map");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.mapPanel = new MapPanel(this);
            frame.add(this.mapPanel);
            frame.pack();
            frame.setVisible(true);
        });

    }

    public Map<String, Position> getGoals() {
        return this.goalsPositions;
    }

    public Grid getGrid() {
        return this.grid;
    }

    public List<Road> getRoads() {
        return this.roads;
    }

    public Map<String, Position> getAgentPositions() {
        return this.agentPositions;
    }

    private void initializeData() {
        int[] size = parseGridProp(System.getProperty("sim.grid"), 40, 40);
        this.grid = new Grid(size[0], size[1]);
        defineObsProperty("grid_width", grid.getWidth());
        defineObsProperty("grid_height", grid.getHeight());
        this.agentActions = new HashMap<>();
        this.agentPositions = new HashMap<>();
        this.agentIntentions = new HashMap<>();
        this.roads = new ArrayList<>();
        this.allFootprints = new ArrayList<>();
        this.goalsPositions = new HashMap<>();
        this.discoveredGoals = new HashMap<>();
    }

    private int[] parseGridProp(String s, int defW, int defH) {
        try {
            if (s == null)
                return new int[] { defW, defH };
            String[] parts = s.toLowerCase().split("x");
            int w = Integer.parseInt(parts[0].trim());
            int h = Integer.parseInt(parts[1].trim());
            return new int[] { w, h };
        } catch (Exception e) {
            return new int[] { defW, defH };
        }
    }

    private void initializeServices() {
        this.movementManager = new MovementManager(grid, agentPositions, roads);

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

            @Override
            public ObsProperty getObsProperty(String property) {
                return TrafficEnvironment.this.getObsProperty(property);
            }

            @Override
            public void updateObsProperty(String property, Object... args) {
                TrafficEnvironment.this.updateObsProperty(property, args);
            }
        };
        this.perceptionObserver = new PerceptionObserver(callback);

        ChangeLanePlanner changeLanePlanner = new ChangeLanePlanner(this.grid, this.allFootprints);

        ActionHandlerFactory.registerHandler("follow", new FollowActionHandler());
        // ActionHandlerFactory.registerHandler("turn", new
        // TurnActionHandler(turnDiscoveryService));
        ActionHandlerFactory.registerHandler("wait", new DefaultActionHandler());
        ActionHandlerFactory.registerHandler("change_lane", new ChangeLaneActionHandler(changeLanePlanner));
        ActionHandlerFactory.registerHandler("default", new DefaultActionHandler());
    }

    private void setupEnvironment() {
        boolean dynamic = Boolean.parseBoolean(System.getProperty("sym.dynamic", "true"));

        if (dynamic) {
            System.out.println("Started Dynamic setting: ");
            setupEnvironmentDynamicFromProps();
        } else {
            setupRoads();
            setupIntersections();
            spawnAgents();
        }
        updatePerceptions();
    }

    private void setupEnvironmentDynamicFromProps() {
        int vroads = getIntProp("sim.vroads", 5);
        int hroads = getIntProp("sim.hroads", 5);
        int spacing = getIntProp("sim.spacing", 3);
        long seed = getLongProp("sim.seed", 12345L);
        boolean randpos = Boolean.getBoolean("sim.randpos");

        RoadLayoutGenerator gen = new RoadLayoutGenerator();
        RoadLayout layout = gen.buildDeterministic(
                grid, grid.getWidth(), grid.getHeight(),
                vroads, hroads, spacing, seed, randpos);

        System.out.println("ROAD LAYOUT GENERATION: ");

        this.roads.clear();
        this.roads.addAll(layout.getRoads());
        this.roads.forEach(r -> r.getLines().forEach(cell -> this.grid.setCell(cell)));

        IntersectionPlanner planner = new IntersectionPlanner(this.grid, this.roads);
        this.movementManager.setIntersectionPlanner(planner);

        List<Intersection> intersections = layout.getIntersectionFootprints()
                .stream()
                .map(Intersection::new)
                .toList();

        this.allFootprints = intersections;
        planner.setFootprints(intersections);
        this.intersectionDiscoveryService = new IntersectionDiscoveryService(intersections);
        this.intersectionDiscoveryService.addListener(this);
        ActionHandlerFactory.registerHandler("intersection",
                new IntersectionActionHandler(planner, intersectionDiscoveryService));

        // List<Turn> turns = layout.getTurns().stream()
        // .map(p -> new Turn(p[0], p[1]))
        // .toList();
        // this.turnDiscoveryService = new TurnDiscoveryService(turns);
        // this.turnDiscoveryService.addListener(this);
        // ActionHandlerFactory.registerHandler("turn", new
        // TurnActionHandler(turnDiscoveryService));
    }

    private int getIntProp(String key, int def) {
        try {
            return Integer.parseInt(System.getProperty(key, Integer.toString(def)));
        } catch (Exception e) {
            return def;
        }
    }

    private long getLongProp(String key, long def) {
        try {
            return Long.parseLong(System.getProperty(key, Long.toString(def)));
        } catch (Exception e) {
            return def;
        }
    }

    private void setupIntersections() {
        IntersectionPlanner intersectionPlanner = new IntersectionPlanner(this.grid, this.roads);
        this.movementManager.setIntersectionPlanner(intersectionPlanner);

        List<Intersection> intersections = spawnIntersections();
        this.intersectionDiscoveryService = new IntersectionDiscoveryService(intersections);
        this.intersectionDiscoveryService.addListener(this);

        ActionHandlerFactory.registerHandler("intersection",
                new IntersectionActionHandler(intersectionPlanner, intersectionDiscoveryService));

    }

    private List<Intersection> spawnIntersections() {
        Set<Position> footprint = new HashSet<>();
        footprint.add(new Position(4, 1));
        footprint.add(new Position(5, 1));
        footprint.add(new Position(4, 0));
        footprint.add(new Position(5, 0));
        return List.of(new Intersection(footprint));
    }

    private void startSimulation() {
        setupTimer();
    }

    @OPERATION
    public void publishGridSize() {
        defineObsProperty("grid_width", grid.getWidth());
        defineObsProperty("grid_height", grid.getHeight());
    }

    @OPERATION
    public void explore(String agentId, int x, int y, String currentDirection) {
        Position currentPosition = new Position(x, y);
        if (this.turnDiscoveryService != null) {
            this.turnDiscoveryService.exploreTurns(agentId, currentPosition);
        }
        if (this.intersectionDiscoveryService != null) {
            this.intersectionDiscoveryService.exploreIntersectionFromCurrentPosition(agentId, currentPosition);
        }
    }

    @OPERATION
    public void getSimAgents(OpFeedbackParam<Integer> n) {
        n.set(getIntProp("sim.agents", 33));
    }

    @OPERATION
    public void getSimSeed(OpFeedbackParam<Long> seed) {
        seed.set(getLongProp("sim.seed", System.currentTimeMillis()));
    }

    @OPERATION
    void writeIntent(String agent, String action) {
        synchronized (agentActions) {
            this.agentActions.put(agent, action);
        }
        updateIntentions();
    }

    @OPERATION
    public void pickRandomFreeRoadCell(long seed, OpFeedbackParam<Integer> x, OpFeedbackParam<Integer> y) {
        List<Position> free = collectFreeRoadCells();
        if (free.isEmpty()) {
            x.set(-1);
            y.set(-1);
            return;
        }
        Random random = new Random(seed ^ System.nanoTime());
        Position position = free.get(random.nextInt(free.size()));
        x.set(position.getX());
        y.set(position.getY());
    }

    @OPERATION
    public void placeAgent(String agentId, int x, int y, OpFeedbackParam<Boolean> result) {
        Position position = new Position(x, y);
        Cell cell = grid.getCell(x, y);
        if (cell != null && cell.getDirection() != null && !cell.isOccupied() && isInsideAnyRoad(position)) {
            cell.setOccupied(true);
            this.agentPositions.put(agentId, position);
            result.set(true);
        } else {
            result.set(false);
        }
    }

    private List<Position> collectFreeRoadCells() {
        List<Position> freeRoadCells = new ArrayList<>();
        int width = this.grid.getWidth();
        int height = this.grid.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = grid.getCell(x, y);
                if (cell != null && !cell.isOccupied()
                        && isInsideAnyRoad(new Position(x, y)) && cell.getDirection() != null) {
                    freeRoadCells.add(new Position(x, y));
                }
            }
        }

        return freeRoadCells;
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
                movementManager.updateIntentions(actionsCopy);

                this.agentActions.clear();
            }

            if (!actionsCopy.isEmpty()) {
                executeActions(actionsCopy);
            }

            System.out.println("[JAVA] update perceptions");
            updatePerceptions();
            if (this.mapPanel != null) {
                this.mapPanel.repaint();
            }
            signal("step_completed");
        } finally {
            running = false;
        }
    }

    private void executeActions(Map<String, String> actions) {
        for (Map.Entry<String, String> entry : actions.entrySet()) {
            String agent = entry.getKey();
            String action = entry.getValue();

            metrics.step(agent);

            MovementResult result = movementManager.executeAction(agent, action);

            Position goal = goalsPositions.get(agent);
            Position current = agentPositions.get(agent);
            if (goal != null && current != null &&
                    (Math.abs(current.getX() - goal.getX()) + Math.abs(current.getY() - goal.getY()) <= 1)) {
                Cell cell = grid.getCell(current.getX(), current.getY());
                if (cell != null)
                    cell.setOccupied(false);

                agentPositions.remove(agent);

                goalsPositions.remove(agent);

                metrics.goalReached(agent);

                System.out.println("[INFO] Agent " + agent + " reached the goal and freed the cell.");
            }

            System.out.println("[result] " + result.toString());
        }
        if (goalsPositions.size() <= 3) {
            metrics.endSimulation();
            metrics.printSummary();
            stopSimulation();
            showMetricsDialog();
        }
    }

    private void showMetricsDialog() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            String summary = metrics.getSummaryString();
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    summary,
                    "Simulation Metrics",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });
    }

    @OPERATION
    public void isGoalReached(String agentId, int x, int y, int goalX, int goalY, OpFeedbackParam<Boolean> result) {
        int dist = Math.abs(x - goalX) + Math.abs(y - goalY);
        result.set(dist <= 1);
    }

    @Override
    public void onTurnDiscovered(String agentId, Turn turn) {
        this.perceptionObserver.notifyTurnAvailable(turn);

        signal("turn_discovered", agentId,
                turn.getFromPosition().getX(),
                turn.getFromPosition().getY(),
                turn.getToPosition().getX(),
                turn.getToPosition().getY());
    }

    @Override
    public void onIntersectionDiscovered(String agentId, Intersection intersection) {
        this.perceptionObserver.notifyIntersectionAvailable(intersection);

        signal("intersection_discovered", agentId,
                intersection.getPosition().getX(),
                intersection.getPosition().getY());
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

        Road intersectionAxis = roadFactory.createVerticalRoad(
                4, 5,
                0, 7);

        this.roads.clear();

        this.roads.addAll(List.of(mainRoad, crossRoad, intersectionAxis));

        this.roads.forEach(road -> {
            road.getLines().forEach(cell -> {
                this.grid.setCell(cell);
            });
        });
    }

    private void spawnAgents() {
        spawnAgent("vehicle1", new Position(0, 1));
        spawnAgent("vehicle2", new Position(4, 6));
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
        timer = Utils.createTimer("TrafficEnvironmentTimer", () -> {
            try {
                execInternalOp("step");
            } catch (Exception ex) {
                System.err.println("Error in timer: " + ex.getMessage());
            }
        }, interval, interval);

        defineObsProperty("interval", interval);
    }

    private void updatePerceptions() {
        perceptionObserver.updateAgentPositions(agentPositions, agentIntentions, goalsPositions, grid);
    }

    private void updateIntentions() {
        try {
            removeObsPropertyByTemplate("agent_intention", null, null, null, null);
            removeObsPropertyByTemplate("intentions", null, null, null, null);
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
        } else if (action.startsWith("intersection:")) {
            IntersectionPlanner planner = this.movementManager.getIntersectionPlanner();
            return (planner != null) ? planner.computeNext(currentPosition, action) : null;
        } else {
            Cell cell = grid.getCell(currentPosition.getX(), currentPosition.getY());
            return Utils.computeNextPosition(cell, grid);
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

    @OPERATION
    public void getBestIntersectionDirection(int x, int y, int goalX, int goalY, OpFeedbackParam<String> direction) {
        Position currentPosition = new Position(x, y);
        Position goalPosition = new Position(goalX, goalY);
        Cell currentCell = grid.getCell(x, y);
        if (currentCell == null || currentCell.getDirection() == null) {
            direction.set("straight");
            return;
        }
        IntersectionPlanner planner = movementManager.getIntersectionPlanner();
        String bestDir = planner.getBestIntersectionDirection(currentPosition, currentCell.getDirection(), goalPosition,
                agentPositions);
        direction.set(bestDir);
    }

    @OPERATION
    public void assignGoal() {
        var freeCells = collectFreeRoadCells();
        freeCells.removeAll(this.goalsPositions.values());
        Random rand = new Random(System.nanoTime());
        Position goal = freeCells.get(rand.nextInt(freeCells.size()));

        this.goalsPositions.put(getCurrentOpAgentId().toString(), goal);

        defineObsProperty("goal_cell", getCurrentOpAgentId().toString(), goal.getX(), goal.getY());

        if (this.mapPanel != null) {
            this.mapPanel.repaint();
        }
    }

    @OPERATION
    public void discoverGoal(String agentId, int x, int y) {
        Position position = new Position(x, y);
        this.discoveredGoals.put(agentId, position);
    }

    @OPERATION
    public void hasGoalBeenDiscovered(String agentId,
            OpFeedbackParam<Boolean> result,
            OpFeedbackParam<Integer> gx,
            OpFeedbackParam<Integer> gy) {
        Position p = this.discoveredGoals.get(agentId);
        boolean found = (p != null);
        result.set(found);
        if (found) {
            gx.set(p.getX());
            gy.set(p.getY());
        } else {
            gx.set(-1);
            gy.set(-1);
        }
    }

    public Map<String, Position> getDiscoveredGoals() {
        return this.discoveredGoals;
    }

    public boolean isAgentExploring(String agentId) {
        return !this.discoveredGoals.containsKey(agentId);
    }

    public Map<String, String> getAgentActions() {
        return this.agentActions;
    }

}