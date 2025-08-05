public class MovementResult {
    private final boolean success;
    private final String agentId;
    private final Position fromPosition;
    private final Position toPosition;
    private final String action;

    public MovementResult(boolean success, String agentId, Position fromPosition, Position toPosition, String action) {
        this.success = success;
        this.agentId = agentId;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.action = action;
    }

    public static MovementResult successResult(String agentId, Position fromPosition, Position toPosition,
            String action) {
        return new MovementResult(true, agentId, fromPosition, toPosition, action);
    }

    public static MovementResult failureResult(String agentId, Position fromPosition, Position toPosition,
            String action) {
        return new MovementResult(false, agentId, fromPosition, toPosition, action);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getAgentId() {
        return this.agentId;
    }

    public Position getFromPosition() {
        return this.fromPosition;
    }

    public Position getToPosition() {
        return this.toPosition;
    }

    public String getAction() {
        return this.action;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (success ? 1231 : 1237);
        result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
        result = prime * result + ((fromPosition == null) ? 0 : fromPosition.hashCode());
        result = prime * result + ((toPosition == null) ? 0 : toPosition.hashCode());
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MovementResult other = (MovementResult) obj;
        if (success != other.success)
            return false;
        if (agentId == null) {
            if (other.agentId != null)
                return false;
        } else if (!agentId.equals(other.agentId))
            return false;
        if (fromPosition == null) {
            if (other.fromPosition != null)
                return false;
        } else if (!fromPosition.equals(other.fromPosition))
            return false;
        if (toPosition == null) {
            if (other.toPosition != null)
                return false;
        } else if (!toPosition.equals(other.toPosition))
            return false;
        if (action == null) {
            if (other.action != null)
                return false;
        } else if (!action.equals(other.action))
            return false;
        return true;
    }

}
