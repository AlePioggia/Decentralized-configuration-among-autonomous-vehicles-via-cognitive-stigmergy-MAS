package traffic;

import java.util.Timer;
import cartago.Artifact;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import core.Position;
import core.Utils;

public class TrafficLight extends Artifact {

    private static final int LIGHT_DURATION = 5000;
    private boolean isGreenLight;
    private Position position;
    private Timer timer;

    public void init(int x, int y) {
        this.isGreenLight = true;
        this.position = new Position(x, y);

        defineObsProperty("light_state", x, y, "green");

        startTimer();
    }

    private void startTimer() {
        this.timer = Utils.createTimer("TrafficEnvironmentTimer", () -> {
            try {
                execInternalOp("toggleLight");
            } catch (Exception ex) {
                System.err.println("Error in timer: " + ex.getMessage());
            }
        }, LIGHT_DURATION, LIGHT_DURATION);
    }

    @INTERNAL_OPERATION
    public void toggleLight() {
        try {
            this.isGreenLight = !this.isGreenLight;

            try {
                removeObsPropertyByTemplate("light_state");
            } catch (IllegalArgumentException e) {
            }

            defineObsProperty("light_state", this.position.getX(), this.position.getY(),
                    this.isGreenLight ? "green" : "red");
            signal("light_state_changed", this.position.getX(), this.position.getY(),
                    this.isGreenLight ? "green" : "red");
        } catch (Exception e) {
            System.err.println("Error while toggling traffic light");
        }
    }

    @OPERATION
    public void stopSimulation() {
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    @OPERATION
    public boolean isGreenLight() {
        return this.isGreenLight;
    }

}
