package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class GameController {
    private final WorldService worldService;

    public GameController(WorldService worldService) {
        this.worldService = worldService;
    }

    @GetMapping("/game/world")
    public List<Unit>[][] getWorld() {
        return worldService.getGrid();
    }

    @PostMapping("/game/add")
    public void addUnit(@RequestParam int x, @RequestParam int y, @RequestBody Unit unit) {
        worldService.addUnit(x, y, unit);
    }

    @PostMapping("/game/move")
    public Map<String, Object> moveUnit(@RequestParam String id, @RequestParam int dx, @RequestParam int dy) {
        int[] newPos = worldService.moveUnit(id, dx, dy);
        return Map.of("id", id, "x", newPos[0], "y", newPos[1]);
    }
}
