package metrics;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class SimulationMetrics {
    private Map<String, Integer> steps = new HashMap<>();
    private Map<String, Long> agentStartTimes = new HashMap<>();
    private Map<String, Long> agentEndTimes = new HashMap<>();
    private int totalSteps = 0;
    private int goalsReached = 0;
    private long startTime;
    private long endTime;

    public SimulationMetrics() {
        startTime = System.currentTimeMillis();
    }

    public void agentStarted(String agentId) {
        agentStartTimes.put(agentId, System.currentTimeMillis());
    }

    public void step(String agentId) {
        steps.put(agentId, steps.getOrDefault(agentId, 0) + 1);
        totalSteps++;
    }

    public void goalReached(String agentId) {
        goalsReached++;
        agentEndTimes.put(agentId, System.currentTimeMillis());
    }

    public void endSimulation() {
        endTime = System.currentTimeMillis();
    }

    public int getAgentSteps(String agentId) {
        return steps.getOrDefault(agentId, 0);
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getGoalsReached() {
        return goalsReached;
    }

    public long getElapsedTime() {
        return endTime - startTime;
    }

    public long getAgentElapsedTime(String agentId) {
        Long start = agentStartTimes.get(agentId);
        Long end = agentEndTimes.get(agentId);
        if (start != null && end != null) {
            return end - start;
        }
        return -1;
    }

    public double getStepStdDev() {
        List<Integer> values = new ArrayList<>(steps.values());
        double mean = values.stream().mapToInt(i -> i).average().orElse(0.0);
        double variance = values.stream().mapToDouble(i -> Math.pow(i - mean, 2)).sum() / values.size();
        return Math.sqrt(variance);
    }

    public double getTimeStdDev() {
        List<Long> times = new ArrayList<>();
        for (String agent : agentStartTimes.keySet()) {
            long t = getAgentElapsedTime(agent);
            if (t > 0)
                times.add(t);
        }
        double mean = times.stream().mapToLong(i -> i).average().orElse(0.0);
        double variance = times.stream().mapToDouble(i -> Math.pow(i - mean, 2)).sum() / times.size();
        return Math.sqrt(variance);
    }

    public double getStepEquityRatio() {
        if (steps.isEmpty())
            return 1.0;
        int min = steps.values().stream().min(Integer::compare).orElse(1);
        int max = steps.values().stream().max(Integer::compare).orElse(1);
        return min > 0 ? (double) max / min : 1.0;
    }

    public double getTimeEquityRatio() {
        List<Long> times = new ArrayList<>();
        for (String agent : agentStartTimes.keySet()) {
            long t = getAgentElapsedTime(agent);
            if (t > 0)
                times.add(t);
        }
        if (times.isEmpty())
            return 1.0;
        long min = times.stream().min(Long::compare).orElse(1L);
        long max = times.stream().max(Long::compare).orElse(1L);
        return min > 0 ? (double) max / min : 1.0;
    }

    public void printSummary() {
        System.out.println("Total steps: " + getTotalSteps());
        System.out.println("Goals reached: " + getGoalsReached());
        System.out.println("Elapsed time: " + getElapsedTime() + " ms");
        System.out.println("Step stddev: " + getStepStdDev());
        System.out.println("Time stddev: " + getTimeStdDev());
        System.out.println("Step equity ratio: " + getStepEquityRatio());
        System.out.println("Time equity ratio: " + getTimeEquityRatio());
    }
}