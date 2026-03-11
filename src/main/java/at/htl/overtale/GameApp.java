package at.htl.overtale;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.CollisionHandler;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import javafx.geometry.Point2D;

import static com.almasb.fxgl.app.GameApplication.launch;
import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    private OvertaleHud hud;
    private Entity player;
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
        onKey(KeyCode.W, () -> getGameWorld().getSingleton(EntityType.PLAYER).translateY(-3));
        onKey(KeyCode.S, () -> getGameWorld().getSingleton(EntityType.PLAYER).translateY(3));
        onKey(KeyCode.D, () -> getGameWorld().getSingleton(EntityType.PLAYER).translateX(3));
        onKey(KeyCode.A, () -> getGameWorld().getSingleton(EntityType.PLAYER).translateX(-3));

    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameEntityFactory());

        player = spawn("player", 100, 100);
    }

    @Override
    protected void initPhysics() {
        // Collision handlers go here
        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(EntityType.PLAYER, EntityType.BULLET) {
                    @Override
                    protected void onCollisionBegin(Entity p, Entity bullet) {
                        bullet.removeFromWorld();
                        // HP abziehen
                        hud.updateHP(currentHP - 2, maxHP);
                    }
                }
        );
    }

    @Override
    protected void initUI() {
        // HUD, score text, health bar, etc.
        hud = new OvertaleHud();
        hud.build();
        hud.showDialog("Welcome to Overtale");
    }

    @Override
    protected void onUpdate(double tpf) {
        // Called every frame — tpf = time per frame
        getGameWorld().getEntitiesByType(EntityType.BULLET).forEach(bullet -> {
            double vx = bullet.getProperties().getDouble("vx");
            double vy = bullet.getProperties().getDouble("vy");
            bullet.translate(vx * tpf, vy * tpf);

            // Entfernen wenn außerhalb
            if (bullet.getX() < -20 || bullet.getX() > 820 ||
                    bullet.getY() < -20 || bullet.getY() > 620) {
                bullet.removeFromWorld();
            }
        });
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

        // Velocity direkt in SpawnData mitgeben
        spawn("bullet", new SpawnData(x, y)
                .put("vx", direction.getX())
                .put("vy", direction.getY()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}