package org.example;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WorldService {
    private static final int WORLD_SIZE = 100;
    // Every square is a list of units
    private final List<Unit>[][] grid;
    // Map to track unit location for faster moving
    private final Map<String, int[]> unitPositions = new ConcurrentHashMap<>();
    private final Map<String, Unit> units = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public WorldService() {
        grid = new List[WORLD_SIZE][WORLD_SIZE];
        for (int i = 0; i < WORLD_SIZE; i++) {
            for (int j = 0; j < WORLD_SIZE; j++) {
                grid[i][j] = new CopyOnWriteArrayList<>();
            }
        }
    }

    public List<Unit>[][] getGrid() {
        return grid;
    }

    public void addUnit(int x, int y, Unit unit) {
        if (x < 0 || x >= WORLD_SIZE || y < 0 || y >= WORLD_SIZE) {
            throw new IllegalArgumentException("Coordinates out of bounds: (" + x + "," + y + ")");
        }
        grid[x][y].add(unit);
        unitPositions.put(unit.id(), new int[]{x, y});
        units.put(unit.id(), unit);
    }

    public int[] moveUnit(String id, int dx, int dy) {
        int[] pos = unitPositions.get(id);
        if (pos == null) {
            throw new NoSuchElementException("Unit not found: " + id);
        }

        int oldX = pos[0];
        int oldY = pos[1];
        int newX = Math.max(0, Math.min(WORLD_SIZE - 1, oldX + dx));
        int newY = Math.max(0, Math.min(WORLD_SIZE - 1, oldY + dy));

        if (oldX != newX || oldY != newY) {
            Unit unit = units.get(id);
            grid[oldX][oldY].remove(unit);
            grid[newX][newY].add(unit);
            pos[0] = newX;
            pos[1] = newY;
        }

        return new int[]{newX, newY};
    }
}
