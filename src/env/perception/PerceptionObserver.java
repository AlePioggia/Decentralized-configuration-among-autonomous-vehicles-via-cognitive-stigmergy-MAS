package perception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cartago.ObsProperty;
import core.Cell;
import core.Grid;
import core.Position;
import discovery.Intersection;
import discovery.Turn;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTermImpl;

public class PerceptionObserver {

    public interface PerceptionCallback {
        void defineObsProperty(String property, Object... args);

        void removeObsPropertyByTemplate(String property, Object... args);

        ObsProperty getObsProperty(String property);

        void updateObsProperty(String property, Object... args);
    }

    private final PerceptionCallback callback;

    public PerceptionObserver(PerceptionCallback callback) {
        this.callback = callback;
    }

    public void updateAgentPositions(Map<String, Position> agentPosition, Map<String, Position> agentIntentions,
            Map<String, Position> goalsPositions, Grid grid) {
        clearProperties();

        List<Object[]> occList = updateOccupantsProperty(grid);

        System.out.println("[JAVA] occupants: " + Arrays.deepToString(occList.toArray()));

        updateAgentIntentions(agentIntentions);

        updateGoalCells(goalsPositions);

        for (Map.Entry<String, Position> entry : agentPosition.entrySet()) {
            String agentId = entry.getKey();
            Position position = entry.getValue();

            this.callback.defineObsProperty("at", agentId, position.getX(), position.getY());
            this.callback.defineObsProperty("occupied", position.getX(), position.getY());

            Cell cell = grid.getCell(position.getX(), position.getY());
            if (cell != null && cell.getDirection() != null) {
                callback.defineObsProperty("direction", position.getX(), position.getY(), cell.getDirection());
            }
        }
    }

    private void updateGoalCells(Map<String, Position> goalsPositions) {
        try {
            callback.removeObsPropertyByTemplate("goal_cells", null, null, null);
        } catch (Exception e) {
        }
        ListTermImpl goalListTerm = new ListTermImpl();
        for (Map.Entry<String, Position> entry : goalsPositions.entrySet()) {
            String agentId = entry.getKey();
            Position position = entry.getValue();
            ListTermImpl cell = new ListTermImpl();
            cell.add(new StringTermImpl(agentId));
            cell.add(new NumberTermImpl(position.getX()));
            cell.add(new NumberTermImpl(position.getY()));
            goalListTerm.add(cell);
        }
        try {
            callback.updateObsProperty("goal_cells", goalListTerm);
        } catch (Exception e) {
            callback.defineObsProperty("goal_cells", goalListTerm);
        }
    }

    private List<Object[]> updateOccupantsProperty(Grid grid) {
        List<Object[]> occList = grid.getOccupiedCells();

        ListTermImpl occListTerm = new ListTermImpl();

        for (Object[] pos : grid.getOccupiedCells()) {
            int x = (int) pos[0];
            int y = (int) pos[1];
            ListTermImpl cell = new ListTermImpl();
            cell.add(new NumberTermImpl(x));
            cell.add(new NumberTermImpl(y));
            occListTerm.add(cell);
        }

        try {
            callback.updateObsProperty("occupants", occListTerm);
        } catch (Exception e) {
            callback.defineObsProperty("occupants", occListTerm);
        }
        return occList;
    }

    private void updateAgentIntentions(Map<String, Position> agentIntentions) {
        try {
            callback.removeObsPropertyByTemplate("intentions", null, null, null);
        } catch (Exception e) {
        }

        ListTermImpl intentListTerm = new ListTermImpl();

        for (Position pos : agentIntentions.values()) {
            ListTermImpl cell = new ListTermImpl();
            cell.add(new NumberTermImpl(pos.getX()));
            cell.add(new NumberTermImpl(pos.getY()));
            intentListTerm.add(cell);
        }

        try {
            callback.updateObsProperty("intentions", intentListTerm);
        } catch (Exception e) {
            callback.defineObsProperty("intentions", intentListTerm);
        }
    }

    public void notifyTurnAvailable(Turn turn) {
        if (turn == null)
            return;
        callback.defineObsProperty("turn_available",
                turn.getFromPosition().getX(),
                turn.getFromPosition().getY(),
                turn.getToPosition().getX(),
                turn.getToPosition().getY());
    }

    public void notifyIntersectionAvailable(Intersection intersection) {
        if (intersection == null)
            return;
        for (Position p : intersection.getFootPrint()) {
            callback.defineObsProperty("intersection_available", p.getX(), p.getY());
        }
    }

    private void clearProperties() {
        try {
            callback.removeObsPropertyByTemplate("at", null, null, null);
            callback.removeObsPropertyByTemplate("occupied", null, null);
            callback.removeObsPropertyByTemplate("direction", null, null, null);
        } catch (Exception e) {
        }
    }

}
