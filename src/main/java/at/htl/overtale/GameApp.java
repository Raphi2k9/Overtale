package at.htl.overtale;

import at.htl.overtale.entity.EntityType;
import at.htl.overtale.entity.GameEntityFactory;
import at.htl.overtale.hud.DialogManager;
import at.htl.overtale.hud.OvertaleHud;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.CollisionHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    private OvertaleHud hud;
    private DialogManager dialogManager;
    private Entity player;
    private Entity npc;
    private int currentHP = 20;
    private int maxHP = 20;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Overtale");
        settings.setVersion("0.1");
        settings.setWidth(800);
        settings.setHeight(600);
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.W, () -> { if (!dialogManager.isActive()) player.translateY(-3); });
        onKey(KeyCode.S, () -> { if (!dialogManager.isActive()) player.translateY(3); });
        onKey(KeyCode.D, () -> { if (!dialogManager.isActive()) player.translateX(3); });
        onKey(KeyCode.A, () -> { if (!dialogManager.isActive()) player.translateX(-3); });

        onKeyDown(KeyCode.Z, () -> dialogManager.advance());

        onKeyDown(KeyCode.E, () -> {
            if (!dialogManager.isActive() && player.distanceBBox(npc) < 60) {
                dialogManager.startDialog(java.util.List.of(
                    "Howdy! I'm Flowey.",
                    "Flowey the Flower!",
                    "Down here, LOVE is shared through...",
                    "...little white friendliness pellets!"
                ));
            }
        });
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameEntityFactory());
        player = spawn("player", 400, 300);
        npc    = spawn("npc", 200, 250);

        //getGameTimer().runAtInterval(() -> spawnBullet(), Duration.seconds(1.5));
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(EntityType.PLAYER, EntityType.BULLET) {
                    @Override
                    protected void onCollisionBegin(Entity p, Entity bullet) {
                        bullet.removeFromWorld();
                        currentHP -= 2; // ✅ erst speichern
                        hud.updateHP(currentHP, maxHP);
                    }
                }
        );
    }

    @Override
    protected void initUI() {
        hud = new OvertaleHud();
        hud.build();
        dialogManager = new DialogManager(hud);
    }

    @Override
    protected void onUpdate(double tpf) {

    }

    private void spawnBullet() {
        int side = (int)(Math.random() * 4);
        double x, y;

        switch (side) {
            case 0 -> { x = Math.random() * 800; y = -10;  }
            case 1 -> { x = Math.random() * 800; y = 610;  }
            case 2 -> { x = -10;  y = Math.random() * 600; }
            default->{ x = 810;  y = Math.random() * 600;  }
        }

        Point2D direction = new Point2D(player.getX() - x, player.getY() - y)
                .normalize()
                .multiply(200);

        spawn("bullet", new SpawnData(x, y)
                .put("vx", direction.getX())
                .put("vy", direction.getY()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
