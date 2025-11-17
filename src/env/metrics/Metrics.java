package metrics;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Metrics {
    private Map<String, Integer> steps = new HashMap<>();
    private Map<String, Long> agentStartTimes = new HashMap<>();
    private Map<String, Long> agentEndTimes = new HashMap<>();
    private int numInterations = 0;
    private int totalSteps = 0;
    private int goalsReached = 0;
    private long startTime;
    private long endTime;

    public Metrics() {
        startTime = System.currentTimeMillis();
    }

    public void agentStarted(String agentId) {
        agentStartTimes.put(agentId, System.currentTimeMillis());
    }

    public void updateTotalSteps(String agentId) {
        steps.put(agentId, steps.getOrDefault(agentId, 0) + 1);
        totalSteps++;
    }

    public void iteration() {
        numInterations++;
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
        for (String agent : agentEndTimes.keySet()) {
            long t = getAgentElapsedTime(agent);
            if (t > 0) {
                times.add(t);
            }
        }
        int n = times.size();
        if (n == 0)
            return 0.0;
        double mean = times.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);
        double sumSq = times.stream().mapToDouble(v -> {
            double d = v - mean;
            return d * d;
        }).sum();
        double variance = sumSq / n;
        return Math.sqrt(variance);
    }

    public double getTimeStdDevSample() {
        List<Long> times = new ArrayList<>();
        for (String agent : agentEndTimes.keySet()) {
            long t = getAgentElapsedTime(agent);
            if (t > 0)
                times.add(t);
        }
        int n = times.size();
        if (n <= 1)
            return 0.0;
        double mean = times.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);
        double sumSq = times.stream().mapToDouble(v -> {
            double d = v - mean;
            return d * d;
        }).sum();
        return Math.sqrt(sumSq / (n - 1));
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

    public String getSummaryString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total steps: ").append(getTotalSteps()).append("\n");
        sb.append("Goals reached: ").append(getGoalsReached()).append("\n");
        sb.append("Elapsed time: ").append(getElapsedTime()).append(" ms\n");
        sb.append("Step stddev: ").append(getStepStdDev()).append("\n");
        sb.append("Time stddev: ").append(getTimeStdDev()).append("\n");
        sb.append("Step equity ratio: ").append(getStepEquityRatio()).append("\n");
        sb.append("Time equity ratio: ").append(getTimeEquityRatio()).append("\n");
        sb.append("Number of iterations: ").append(getNumInterations()).append("\n");
        return sb.toString();
    }

    public int getNumInterations() {
        return numInterations;
    }
}