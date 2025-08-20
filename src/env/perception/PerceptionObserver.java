package perception;

import java.util.Map;

import core.Cell;
import core.Grid;
import core.Position;
import discovery.Intersection;
import discovery.Turn;

public class PerceptionObserver {

    public interface PerceptionCallback {
        void defineObsProperty(String property, Object... args);

        void removeObsPropertyByTemplate(String property, Object... args);
    }

    private final PerceptionCallback callback;

    public PerceptionObserver(PerceptionCallback callback) {
        this.callback = callback;
    }

    public void updateAgentPositions(Map<String, Position> agentPosition, Grid grid) {
        clearProperties();

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
