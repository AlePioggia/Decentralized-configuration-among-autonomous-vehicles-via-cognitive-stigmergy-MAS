import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class TrafficEnvironment extends Artifact {

    private Map<String, String> agentActions;

    void init() {
        agentActions = new HashMap<>();
    }

    @OPERATION
    void writeIntent(String agent, String action) {
        agentActions.put(agent, action);
        System.out.println("Agent " + agent + " intends to perform action: " + action);
    }

    @OPERATION
    void readIntents(OpFeedbackParam<List<String>> intents) {
        List<String> agentActionsList = collectIntents();
        intents.set(agentActionsList);
        System.out.println("Current agent intents: " + agentActions);
    }

    private List<String> collectIntents() {
        return agentActions.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.toList());
    }

}