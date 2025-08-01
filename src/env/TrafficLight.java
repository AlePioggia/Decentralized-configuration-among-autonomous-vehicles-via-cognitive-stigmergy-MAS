import java.util.Timer;
import java.util.TimerTask;
import cartago.Artifact;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;

public class TrafficLight extends Artifact {

    private static final int LIGHT_DURATION = 2000;
    private boolean isGreenLight;
    private Position position;
    private Timer timer;

    public void init(int x, int y) {
        this.isGreenLight = true;
        this.position = new Position(x, y);
        startTimer();
    }

    private void startTimer() {
        this.timer = new Timer("TrafficLightTimer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                execInternalOp("toggleLight");
            }
        }, LIGHT_DURATION, LIGHT_DURATION);
    }

    @INTERNAL_OPERATION
    public void toggleLight() {
        try {
            this.isGreenLight = !this.isGreenLight;
            removeObsPropertyByTemplate("light_state");
            defineObsProperty("light_state", this.position.getX(), this.position.getY(), this.isGreenLight);
            signal("light_state_changed", this.position.getX(), this.position.getY(), this.isGreenLight);
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
