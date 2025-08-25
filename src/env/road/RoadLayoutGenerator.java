package road;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import core.Cell;
import core.Grid;

public class RoadLayoutGenerator {

    private RoadLayout build(Grid grid, SimulationEnvironmentParams params) {
        return params.getGenerationType() == SimulationEnvironmentParams.GenerationType.DETERMINISTIC
                ? buildDeterministicLayout(grid, params)
                : buildRandomizedLayout(grid, params);
    }

    private RoadLayout buildDeterministicLayout(Grid grid, SimulationEnvironmentParams params) {
        return null;
    }

    private RoadLayout buildRandomizedLayout(Grid grid, SimulationEnvironmentParams params) {
        return null;
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

}
