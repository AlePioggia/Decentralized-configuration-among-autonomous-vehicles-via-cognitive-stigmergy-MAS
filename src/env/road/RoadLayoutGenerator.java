package road;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import core.Cell;
import core.Grid;
import core.Position;

public class RoadLayoutGenerator {

    private RoadLayout build(Grid grid, SimulationEnvironmentParams params) {
        return params.getGenerationType() == SimulationEnvironmentParams.GenerationType.DETERMINISTIC
                ? buildDeterministicLayout(grid, params)
                : buildRandomizedLayout(grid, params);
    }

    private RoadLayout buildDeterministicLayout(Grid grid, SimulationEnvironmentParams params) {
        int width = params.getWidth();
        int height = params.getHeight();
        int spacing = Math.max(1, params.getMinSpacingBetweenRoads());
        int nVertical = Math.max(1, params.getVerticalRoadsNumber());
        int nHorizontal = Math.max(1, params.getHorizontalRoadsNumber());
        boolean randomizedPositions = params.isRandomizedPositions();
        Random random = new Random(params.getSeed());

        List<Integer> vBases = selectBases(1, Math.max(1, width - 2), nVertical, spacing, randomizedPositions,
                random);
        List<Integer> hBases = selectBases(spacing, Math.max(1, height - 2), nHorizontal, spacing, randomizedPositions,
                random);

        if (vBases.isEmpty())
            vBases = List.of(Math.max(1, (width - 2) / 2));
        if (hBases.isEmpty())
            hBases = List.of(Math.max(1, (height - 2) / 2));

        return buildLayout(grid, width, height, vBases, hBases, random);
    }

    private RoadLayout buildRandomizedLayout(Grid grid, SimulationEnvironmentParams params) {
        int width = params.getWidth();
        int height = params.getHeight();
        int spacing = Math.max(1, params.getMinSpacingBetweenRoads());
        Random rnd = new Random(params.getSeed());

        int nVertical = 1 + rnd.nextInt(Math.max(1, params.getMaxVerticalRoadsNumber()));
        int nHorizontal = 1 + rnd.nextInt(Math.max(1, params.getMaxHorizontalRoadsNumber()));
        boolean randomizedPositions = params.isRandomizedPositions();

        List<Integer> vBases = selectBases(1, Math.max(1, width - 2), nVertical, spacing, randomizedPositions, rnd);
        List<Integer> hBases = selectBases(1, Math.max(1, height - 2), nHorizontal, spacing, randomizedPositions, rnd);

        if (vBases.isEmpty())
            vBases = List.of(Math.max(1, (width - 2) / 2));
        if (hBases.isEmpty())
            hBases = List.of(Math.max(1, (height - 2) / 2));

        return buildLayout(grid, width, height, vBases, hBases, rnd);
    }

    private RoadLayout buildLayout(Grid grid, int width, int heigh, List<Integer> verticalBases,
            List<Integer> horizontalBases, Random random) {
        List<Road> roads = new ArrayList<>();
        RoadFactory roadFactory = new BasicRoadFactoryImpl();

        for (int x : verticalBases) {
            Road verticalRoad = roadFactory.createVerticalRoad(x, x + 1, 0, heigh);
            roads.add(verticalRoad);
            IntStream.range(0, heigh).forEach(y -> {
                grid.getCell(x, y).setDirection("North");
                grid.getCell(x + 1, y).setDirection("South");
            });
        }

        for (int y : horizontalBases) {
            Road horizontalRoad = roadFactory.createHorizontalRoad(0, width, y, y + 1);
            roads.add(horizontalRoad);
            IntStream.range(0, width).forEach(x -> {
                grid.getCell(x, y).setDirection("West");
                grid.getCell(x, y + 1).setDirection("East");
            });
        }

        Set<Set<Position>> footprints = new LinkedHashSet<>();
        for (int x : verticalBases) {
            for (int y : horizontalBases) {
                Set<Position> footprint = new LinkedHashSet<>();
                footprint.addAll(List.of(new Position(x, y), new Position(x + 1, y), new Position(x, y + 1),
                        new Position(x + 1, y + 1)));
                footprints.add(footprint);
                patchCenterDirections(grid, x, y);
            }
        }

        int minDistFromIntersections = 2;
        Set<Position[]> standaloneTurns = generateStandaloneTurns(grid, width, heigh, verticalBases, horizontalBases,
                footprints, minDistFromIntersections, random, roads);

        if (!areRoadsAllConnected(grid)) {
            int cy = Math.max(1, (heigh - 2) / 2);
            roads.add(roadFactory.createHorizontalRoad(0, width, cy, cy + 1));
            IntStream.range(0, width).forEach(x -> {
                grid.getCell(x, cy).setDirection("West");
                grid.getCell(x, cy + 1).setDirection("East");
            });
            for (int x : verticalBases) {
                Set<Position> footprint = new LinkedHashSet<>();
                footprint.addAll(List.of(new Position(x, 0), new Position(x + 1, 0), new Position(x, 1),
                        new Position(x + 1, 1)));
                if (footprints.add(footprint)) {
                    patchCenterDirections(grid, x, cy);
                }
            }
            standaloneTurns = generateStandaloneTurns(grid, width, heigh, verticalBases, horizontalBases, footprints,
                    minDistFromIntersections, random, roads);
        }

        return new RoadLayout(roads, footprints, null, standaloneTurns);
    }

    private Set<Position[]> generateStandaloneTurns(Grid grid, int width, int height,
            List<Integer> verticalBases, List<Integer> horizontalBases,
            Set<Set<Position>> footprints,
            int minDistFromIntersections, Random random, List<Road> outerRoads) {
        Set<Position> intersectionCells = new LinkedHashSet<>();
        footprints.forEach(footprint -> {
            intersectionCells.addAll(footprint);
        });
        Set<Position> forbidden = expandForbidden(intersectionCells, width, height, minDistFromIntersections);

        Set<String> usedKeys = new LinkedHashSet<>();
        Set<Position[]> turns = new LinkedHashSet<>();

        for (int x : verticalBases) {
            List<Integer> ys = pickIndicesRandom(random, 2, Math.max(1, height - 3), 2, 3);
            for (int y : ys) {
                Position from = new Position(x, y);
                Position to = new Position(x + 1, y);
                if (!inBounds(to, width, height)) {
                    continue;
                }
                if (forbidden.contains(from) || forbidden.contains(to)) {
                    continue;
                }
                Cell startingPointCell = grid.getCell(from.getX(), from.getY());
                if (startingPointCell == null
                        || (!"North".equals(startingPointCell.getDirection())
                                && !"South".equals(startingPointCell.getDirection())))
                    continue;

                Road horizontalTurn = createHorizontalTurn(grid, x, Math.min(x + 2, width - 1), y,
                        Math.min(y + 1, height - 1));
                if (horizontalTurn != null) {
                    String keyFromStartToEnd = key(from, to);
                    if (usedKeys.add(keyFromStartToEnd)) {
                        outerRoads.add(horizontalTurn);
                        turns.add(new Position[] { from, to });
                    }
                }
            }
        }

        for (int y : horizontalBases) {
            List<Integer> xs = pickIndicesRandom(random, 2, Math.min(1, height - 3), 2, 3);
            for (int x : xs) {
                Position from = new Position(x, y);
                Position to = new Position(x, y + 1);
                if (!inBounds(to, width, height)) {
                    continue;
                }
                if (forbidden.contains(from) || forbidden.contains(to)) {
                    continue;
                }
                Cell startingPointCell = grid.getCell(from.getX(), from.getY());
                if (startingPointCell == null || !"East".equals(startingPointCell.getDirection())) {
                    continue;
                }
                Road spur = createVerticalTurn(grid, x, Math.min(x + 1, width - 1), y, Math.min(y + 2, height - 1));
                if (spur != null) {
                    outerRoads.add(spur);
                    String k = key(from, to);
                    if (usedKeys.add(k)) {
                        turns.add(new Position[] { from, to });
                    }
                }
            }
        }

        return turns;
    }

    private Road createHorizontalTurn(Grid grid, int startX, int endX, int y1, int y2) {
        if (endX <= startX || y2 <= y1 || startX < 0 || endX + 1 >= grid.getWidth() || y1 < 0
                || y2 + 1 >= grid.getHeight()) {
            return null;
        }
        RoadFactory roadFactory = new BasicRoadFactoryImpl();
        IntStream.range(startX, endX + 1).forEach(x -> {
            grid.getCell(x, y1).setDirection("West");
            grid.getCell(x, y2).setDirection("East");
        });
        return roadFactory.createHorizontalRoad(startX, endX + 1, y1, y2);
    }

    private Road createVerticalTurn(Grid grid, int startY, int endY, int x1, int x2) {
        if (startY <= endY || x1 < 0 || x2 + 1 >= grid.getWidth() || startY < 0 || endY + 1 >= grid.getHeight()) {
            return null;
        }
        RoadFactory roadFactory = new BasicRoadFactoryImpl();
        IntStream.range(startY, endY).forEach(y -> {
            grid.getCell(x1, y).setDirection("North");
            grid.getCell(x2, y).setDirection("South");
        });
        return roadFactory.createVerticalRoad(x1, x2, startY, endY + 1);
    }

    private static Set<Position> expandForbidden(Set<Position> seeds, int width, int height, int minDist) {
        Set<Position> forb = new LinkedHashSet<>(seeds);
        if (minDist <= 0)
            return forb;
        for (Position s : seeds) {
            for (int dx = -minDist; dx <= minDist; dx++) {
                for (int dy = -minDist; dy <= minDist; dy++) {
                    int nx = s.getX() + dx, ny = s.getY() + dy;
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                        if (Math.abs(dx) + Math.abs(dy) <= minDist) {
                            forb.add(new Position(nx, ny));
                        }
                    }
                }
            }
        }
        return forb;
    }

    private static boolean inBounds(Position position, int width, int height) {
        return position.getX() >= 0 && position.getX() < width && position.getY() >= 0 && position.getY() < height;
    }

    private static String key(Position a, Position b) {
        return a.getX() + "," + a.getY() + "|" + b.getX() + "," + b.getY();
    }

    private static void patchCenterDirections(Grid grid, int x, int y) {
        grid.getCell(x, y).setDirection("North");
        grid.getCell(x, y + 1).setDirection("North");
        grid.getCell(x + 1, y).setDirection("West");
        grid.getCell(x + 1, y + 1).setDirection("East");
    }

    private List<Integer> selectBases(int min, int max, int count, int spacing,
            boolean randomized, Random rnd) {
        return randomized
                ? pickIndicesRandom(rnd, min, max, count, spacing + 2)
                : evenlySpacedIndices(min, max, count, spacing);
    }

    private static List<Integer> pickIndicesRandom(Random rnd, int min, int max, int n, int spacing) {
        if (max < min || n <= 0)
            return List.of();
        List<Integer> pool = new ArrayList<>();
        for (int i = min; i <= max; i++)
            pool.add(i);
        Collections.shuffle(pool, rnd);
        List<Integer> picked = new ArrayList<>();
        for (int idx : pool) {
            boolean ok = picked.stream().allMatch(p -> Math.abs(p - idx) >= spacing);
            if (ok)
                picked.add(idx);
            if (picked.size() == n)
                break;
        }
        Collections.sort(picked);
        return picked;
    }

    private static List<Integer> evenlySpacedIndices(int min, int max, int n, int spacing) {
        if (max < min || n <= 0)
            return List.of();
        List<Integer> res = new ArrayList<>();
        int range = (max - min) + 1;
        int step = Math.max(spacing, Math.max(1, range / n));
        int idx = min + (step / 2);
        while (idx <= max && res.size() < n) {
            res.add(idx);
            idx += step;
        }
        for (int i = min; res.size() < n && i <= max; i++) {
            int ii = i;
            boolean ok = res.stream().allMatch(p -> Math.abs(p - ii) >= spacing);
            if (ok)
                res.add(ii);
        }
        Collections.sort(res);
        return res;
    }

    private static boolean areRoadsAllConnected(Grid grid) {
        int w = grid.getWidth();
        int h = grid.getHeight();
        boolean[][] road = new boolean[w][h];

        int[] startAndTotal = markRoadCellsAndFindStart(grid, road);
        int sx = startAndTotal[0];
        int sy = startAndTotal[1];
        int total = startAndTotal[2];

        if (total == 0)
            return true;

        int seen = bfsReachableCount(road, sx, sy);

        return seen == total;
    }

    private static int[] markRoadCellsAndFindStart(Grid grid, boolean[][] road) {
        int w = road.length;
        int h = w > 0 ? road[0].length : 0;
        int total = 0;
        int sx = -1, sy = -1;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Cell c = grid.getCell(x, y);
                if (c != null && c.getDirection() != null) {
                    road[x][y] = true;
                    total++;
                    if (sx == -1) {
                        sx = x;
                        sy = y;
                    }
                }
            }
        }
        return new int[] { sx, sy, total };
    }

    private static int bfsReachableCount(boolean[][] road, int sx, int sy) {
        int w = road.length;
        int h = w > 0 ? road[0].length : 0;
        if (sx < 0 || sy < 0 || w == 0 || h == 0)
            return 0;

        boolean[][] vis = new boolean[w][h];
        java.util.Deque<int[]> dq = new java.util.ArrayDeque<>();
        dq.add(new int[] { sx, sy });
        vis[sx][sy] = true;
        int seen = 1;

        int[] dx = { 1, -1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };

        while (!dq.isEmpty()) {
            int[] p = dq.poll();
            for (int i = 0; i < 4; i++) {
                int nx = p[0] + dx[i], ny = p[1] + dy[i];
                if (nx >= 0 && nx < w && ny >= 0 && ny < h && road[nx][ny] && !vis[nx][ny]) {
                    vis[nx][ny] = true;
                    seen++;
                    dq.add(new int[] { nx, ny });
                }
            }
        }
        return seen;
    }

    private static <T> List<T> union(List<T> a, List<T> b) {
        List<T> outerList = new ArrayList<>(a.size() + b.size());
        outerList.addAll(a);
        outerList.addAll(b);
        return outerList;
    }

}
