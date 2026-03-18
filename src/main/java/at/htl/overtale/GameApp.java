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

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    private OvertaleHud _hud;
    private DialogManager _dialogManager;
    private InventoryHud _inventoryHud;
    private Inventory _inventory;
    private Entity _player;
    private Entity _npc;
    private int _currentHP = 20;
    private int _maxHP = 20;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Overtale");
        settings.setVersion("0.1");
        settings.setWidth(800);
        settings.setHeight(600);
    }

    @Override
    protected void initInput() {
        onKey(KeyCode.W, () -> { if (!_dialogManager.isActive() && !_inventoryHud.isVisible()) _player.translateY(-3); });
        onKey(KeyCode.S, () -> { if (!_dialogManager.isActive() && !_inventoryHud.isVisible()) _player.translateY(3); });
        onKey(KeyCode.D, () -> { if (!_dialogManager.isActive() && !_inventoryHud.isVisible()) _player.translateX(3); });
        onKey(KeyCode.A, () -> { if (!_dialogManager.isActive() && !_inventoryHud.isVisible()) _player.translateX(-3); });

        // Inventar-Navigation (nur wenn Inventar sichtbar)
        onKeyDown(KeyCode.UP,    () -> { if (_inventoryHud.isVisible()) _inventoryHud.navigate(-1); });
        onKeyDown(KeyCode.DOWN,  () -> { if (_inventoryHud.isVisible()) _inventoryHud.navigate(+1); });

        // LEFT/RIGHT: HUD-Button-Navigation oder Inventar-Spalte
        onKeyDown(KeyCode.LEFT, () -> {
            if (_inventoryHud.isVisible()) {
                _inventoryHud.navigate(-4);
            } else if (_hud.isHUDVisible()) {
                int prev = (_hud.getSelectedButton() - 1 + 4) % 4;
                _hud.highlightButton(prev);
            }
        });
        onKeyDown(KeyCode.RIGHT, () -> {
            if (_inventoryHud.isVisible()) {
                _inventoryHud.navigate(+4);
            } else if (_hud.isHUDVisible()) {
                int next = (_hud.getSelectedButton() + 1) % 4;
                _hud.highlightButton(next);
            }
        });

        // X schließt das Inventar → zurück zum HUD
        onKeyDown(KeyCode.X, () -> {
            if (_inventoryHud.isVisible()) {
                _inventoryHud.hide();
                _hud.showHUD();
            }
        });

        // Q = Item wegwerfen
        onKeyDown(KeyCode.Q, () -> {
            if (_inventoryHud.isVisible()) {
                String msg = _inventoryHud.dropSelected();
                if (msg != null) {
                    _inventoryHud.hide();
                    _dialogManager.startDialog(java.util.List.of(msg), () -> _hud.showHUD());
                }
            }
        });

        onKeyDown(KeyCode.Z, () -> {
            if (_inventoryHud.isVisible()) {
                // Item benutzen
                int slot = _inventoryHud.getSelectedSlot();
                at.htl.overtale.component.items.Item item = _inventory.getItem(slot);
                if (item != null) {
                    int heal = item.getHealAmount();
                    String msg = _inventoryHud.useSelected();
                    if (heal > 0) {
                        _currentHP = Math.min(_currentHP + heal, _maxHP);
                        _hud.updateHP(_currentHP, _maxHP);
                    }
                    _inventoryHud.hide();
                    _dialogManager.startDialog(java.util.List.of(msg), () -> _hud.showHUD());
                }
            } else if (_hud.isHUDVisible()) {
                // HUD-Button bestätigen
                int selected = _hud.getSelectedButton();
                if (selected == 2) { // ITEM
                    _inventoryHud.show();
                    _hud.hideAll();
                }
            } else {
                _dialogManager.advance();
            }
        });

        onKeyDown(KeyCode.E, () -> {
            if (!_dialogManager.isActive() && _player.distanceBBox(_npc) < 60) {
                _dialogManager.startDialog(java.util.List.of(
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
        _player = spawn("player", 400, 300);
        _npc = spawn("npc", 200, 250);

        // Beispiel-Items ins Inventar legen
        _inventory = new Inventory();
        _inventory.addItem(new Engelssegen());
        _inventory.addItem(new Engelssegen());

        //getGameTimer().runAtInterval(() -> spawnBullet(), Duration.seconds(1.5));
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(EntityType.PLAYER, EntityType.BULLET) {
                    @Override
                    protected void onCollisionBegin(Entity p, Entity bullet) {
                        bullet.removeFromWorld();
                        _currentHP -= 2; // ✅ erst speichern
                        _hud.updateHP(_currentHP, _maxHP);
                    }
                }
        );
    }

    @Override
    protected void initUI() {
        _hud = new OvertaleHud();
        _hud.build();
        _dialogManager = new DialogManager(_hud);

        _inventoryHud = new InventoryHud(_inventory);
        _inventoryHud.build();
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

        Point2D direction = new Point2D(_player.getX() - x, _player.getY() - y)
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
