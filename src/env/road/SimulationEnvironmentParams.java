package road;

public class SimulationEnvironmentParams {
    public enum GenerationType {
        DETERMINISTIC, RANDOM
    };

    private final int width;
    private final int height;
    private final GenerationType generationType;
    private final int verticalRoadsNumber;
    private final int horizontalRoadsNumber;
    private final int maxVerticalRoadsNumber;
    private final int maxHorizontalRoadsNumber;
    private final int minSpacingBetweenRoads;
    private final boolean randomizedPositions;
    private final long seed;

    private SimulationEnvironmentParams(int width, int height, GenerationType generationType, int verticalRoadsNumber,
            int horizontalRoadsNumber, int maxVerticalRoadsNumber, int maxHorizontalRoadsNumber,
            int minSpacingBetweenRoads,
            boolean randomizedPositions, long seed) {
        this.width = width;
        this.height = height;
        this.generationType = generationType;
        this.verticalRoadsNumber = verticalRoadsNumber;
        this.horizontalRoadsNumber = horizontalRoadsNumber;
        this.maxVerticalRoadsNumber = maxVerticalRoadsNumber;
        this.maxHorizontalRoadsNumber = maxHorizontalRoadsNumber;
        this.minSpacingBetweenRoads = minSpacingBetweenRoads;
        this.randomizedPositions = randomizedPositions;
        this.seed = seed;
    }

    private static SimulationEnvironmentParams generateDeterministicEnvironmentParams(int width, int height,
            int vertical,
            int horizontal, int minSpacing) {
        return new SimulationEnvironmentParams(width, height, GenerationType.DETERMINISTIC,
                Math.max(1, vertical), Math.max(1, horizontal),
                0, 0, minSpacing,
                false, 0L);
    }

    private static SimulationEnvironmentParams generateDeterministicEnvironmentParamsWithRandomPositions(int width,
            int height,
            int vertical,
            int horizontal, int minSpacing, long seed) {
        return new SimulationEnvironmentParams(width, height, GenerationType.DETERMINISTIC,
                Math.max(1, vertical), Math.max(1, horizontal),
                0, 0, minSpacing,
                true, seed);
    }

    private static SimulationEnvironmentParams randomized(int width, int height, int maxVertical, int maxHorizontal,
            int minSpacing, long seed) {
        return new SimulationEnvironmentParams(
                width, height, GenerationType.RANDOM,
                0, 0,
                Math.max(1, maxVertical), Math.max(1, maxHorizontal),
                Math.max(1, minSpacing),
                true, seed);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public GenerationType getGenerationType() {
        return generationType;
    }

    public int getVerticalRoadsNumber() {
        return verticalRoadsNumber;
    }

    public int getHorizontalRoadsNumber() {
        return horizontalRoadsNumber;
    }

    public int getMaxVerticalRoadsNumber() {
        return maxVerticalRoadsNumber;
    }

    public int getMaxHorizontalRoadsNumber() {
        return maxHorizontalRoadsNumber;
    }

    public int getMinSpacingBetweenRoads() {
        return minSpacingBetweenRoads;
    }

    public boolean isRandomizedPositions() {
        return randomizedPositions;
    }

    public long getSeed() {
        return seed;
    }

}
