package at.htl.overtale.entity;

import at.htl.overtale.component.BossPattern;
import at.htl.overtale.component.items.Item;

import java.util.List;

public record BossData(
        String name,
        int maxHp,
        List<String> preDialog,
        List<String> actDialog,
        List<String> postDialog,
        BossPattern pattern,
        double fightDuration,
        double bulletInterval,
        double spawnX,
        double spawnY,
        String color,
        String stroke,
        Item reward,
        String[] phaseNames,
        double[] phaseDurations,
        double[] phaseIntervals
) {
    public boolean hasPhases() {
        return phaseNames != null && phaseNames.length > 0;
    }
}
