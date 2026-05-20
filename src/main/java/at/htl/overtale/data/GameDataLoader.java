package at.htl.overtale.data;

import at.htl.overtale.component.BossPattern;
import at.htl.overtale.component.items.ItemFactory;
import at.htl.overtale.entity.BossData;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Objects;

public class GameDataLoader {

    private static final Gson GSON = new Gson();

    public static BossData[] loadBosses() {
        BossConfig[] configs = load("assets/data/bosses.json", BossConfig[].class);
        return Arrays.stream(configs).map(GameDataLoader::toBossData).toArray(BossData[]::new);
    }

    public static DialogConfig loadDialogs() {
        return load("assets/data/dialogs.json", DialogConfig.class);
    }

    public static ChestConfig[] loadChests() {
        return load("assets/data/chests.json", ChestConfig[].class);
    }

    private static BossData toBossData(BossConfig c) {
        String[] phaseNames     = null;
        double[] phaseDurations = null;
        double[] phaseIntervals = null;
        if (c.phases != null && !c.phases.isEmpty()) {
            phaseNames     = c.phases.stream().map(p -> p.name).toArray(String[]::new);
            phaseDurations = c.phases.stream().mapToDouble(p -> p.duration).toArray();
            phaseIntervals = c.phases.stream().mapToDouble(p -> p.interval).toArray();
        }
        return new BossData(
                c.name, c.maxHp,
                c.preDialog, c.actDialog, c.postDialog,
                BossPattern.valueOf(c.pattern),
                c.fightDuration, c.bulletInterval,
                c.spawnX, c.spawnY,
                c.color, c.stroke,
                ItemFactory.create(c.reward),
                phaseNames, phaseDurations, phaseIntervals
        );
    }

    private static <T> T load(String path, Class<T> type) {
        try (Reader r = new InputStreamReader(
                Objects.requireNonNull(
                        GameDataLoader.class.getClassLoader().getResourceAsStream(path),
                        "Resource not found: " + path))) {
            return GSON.fromJson(r, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load: " + path, e);
        }
    }
}
