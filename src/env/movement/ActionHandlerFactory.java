package movement;

import java.util.HashMap;
import java.util.Map;

public class ActionHandlerFactory {

    private static final Map<String, ActionHandler> handlers = new HashMap<>();

    public static void registerHandler(String action, ActionHandler handler) {
        handlers.put(action, handler);
    }

    public static ActionHandler getHandler(String action) {
        if (action.equals("follow")) {
            return handlers.get("follow");
        } else if (action.startsWith("turn:")) {
            return handlers.get("turn");
        } else if (action.equals("wait")) {
            return handlers.get("wait");
        } else if (action.startsWith("intersection:")) {
            return handlers.get("intersection");
        } else {
            return handlers.get("default");
        }
    }

}
