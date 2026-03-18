package at.htl.overtale;

import at.htl.overtale.component.items.Engelssegen;
import at.htl.overtale.component.items.Inventory;
import at.htl.overtale.entity.EntityType;
import at.htl.overtale.entity.GameEntityFactory;
import at.htl.overtale.hud.DialogManager;
import at.htl.overtale.hud.InventoryHud;
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
    private InventoryHud inventoryHud;
    private Inventory inventory;
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
        onKey(KeyCode.W, () -> { if (!dialogManager.isActive() && !inventoryHud.isVisible()) player.translateY(-3); });
        onKey(KeyCode.S, () -> { if (!dialogManager.isActive() && !inventoryHud.isVisible()) player.translateY(3); });
        onKey(KeyCode.D, () -> { if (!dialogManager.isActive() && !inventoryHud.isVisible()) player.translateX(3); });
        onKey(KeyCode.A, () -> { if (!dialogManager.isActive() && !inventoryHud.isVisible()) player.translateX(-3); });

        // Inventar-Navigation (nur wenn Inventar sichtbar)
        onKeyDown(KeyCode.UP,    () -> { if (inventoryHud.isVisible()) inventoryHud.navigate(-1); });
        onKeyDown(KeyCode.DOWN,  () -> { if (inventoryHud.isVisible()) inventoryHud.navigate(+1); });

        // LEFT/RIGHT: HUD-Button-Navigation oder Inventar-Spalte
        onKeyDown(KeyCode.LEFT, () -> {
            if (inventoryHud.isVisible()) {
                inventoryHud.navigate(-4);
            } else if (hud.isHUDVisible()) {
                int prev = (hud.getSelectedButton() - 1 + 4) % 4;
                hud.highlightButton(prev);
            }
        });
        onKeyDown(KeyCode.RIGHT, () -> {
            if (inventoryHud.isVisible()) {
                inventoryHud.navigate(+4);
            } else if (hud.isHUDVisible()) {
                int next = (hud.getSelectedButton() + 1) % 4;
                hud.highlightButton(next);
            }
        });

        // X schließt das Inventar → zurück zum HUD
        onKeyDown(KeyCode.X, () -> {
            if (inventoryHud.isVisible()) {
                inventoryHud.hide();
                hud.showHUD();
            }
        });

        // Q = Item wegwerfen
        onKeyDown(KeyCode.Q, () -> {
            if (inventoryHud.isVisible()) {
                String msg = inventoryHud.dropSelected();
                if (msg != null) {
                    inventoryHud.hide();
                    dialogManager.startDialog(java.util.List.of(msg), () -> hud.showHUD());
                }
            }
        });

        onKeyDown(KeyCode.Z, () -> {
            if (inventoryHud.isVisible()) {
                // Item benutzen
                int slot = inventoryHud.getSelectedSlot();
                at.htl.overtale.component.items.Item item = inventory.getItem(slot);
                if (item != null) {
                    int heal = item.getHealAmount();
                    String msg = inventoryHud.useSelected();
                    if (heal > 0) {
                        currentHP = Math.min(currentHP + heal, maxHP);
                        hud.updateHP(currentHP, maxHP);
                    }
                    inventoryHud.hide();
                    dialogManager.startDialog(java.util.List.of(msg), () -> hud.showHUD());
                }
            } else if (hud.isHUDVisible()) {
                // HUD-Button bestätigen
                int selected = hud.getSelectedButton();
                if (selected == 2) { // ITEM
                    inventoryHud.show();
                    hud.hideAll();
                }
            } else {
                dialogManager.advance();
            }
        });

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

        // Beispiel-Items ins Inventar legen
        inventory = new Inventory();
        inventory.addItem(new Engelssegen());
        inventory.addItem(new Engelssegen());

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

        inventoryHud = new InventoryHud(inventory);
        inventoryHud.build();
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
