package at.htl.overtale;

import at.htl.overtale.component.items.Engelssegen;
import at.htl.overtale.component.items.GoldenerNektar;
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
import com.almasb.fxgl.time.TimerAction;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    private OvertaleHud _hud;
    private DialogManager _dialogManager;
    private InventoryHud _inventoryHud;
    private Inventory _inventory;
    private Entity _player;
    private Entity _npc;
    private Entity _enemy;
    private int _currentHP = 20;
    private int _maxHP = 40;

    private boolean _inDodgePhase = false;
    private TimerAction _dodgeTimerAction;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Overtale");
        settings.setVersion("0.1");
        settings.setWidth(800);
        settings.setHeight(600);
    }

    @Override
    protected void initInput() {
        // WASD: In der Ausweichphase → Herz bewegen; sonst → Spieler bewegen (nicht wenn HUD/Dialog offen)
        onKey(KeyCode.W, () -> {
            if (_inDodgePhase) {
                _hud.moveHeart(0, -3);
            } else if (!_dialogManager.isActive() && !_inventoryHud.isVisible() && !_hud.isHUDVisible()) {
                _player.translateY(-3);
            }
        });
        onKey(KeyCode.S, () -> {
            if (_inDodgePhase) {
                _hud.moveHeart(0, 3);
            } else if (!_dialogManager.isActive() && !_inventoryHud.isVisible() && !_hud.isHUDVisible()) {
                _player.translateY(3);
            }
        });
        onKey(KeyCode.D, () -> {
            if (_inDodgePhase) {
                _hud.moveHeart(3, 0);
            } else if (!_dialogManager.isActive() && !_inventoryHud.isVisible() && !_hud.isHUDVisible()) {
                _player.translateX(3);
            }
        });
        onKey(KeyCode.A, () -> {
            if (_inDodgePhase) {
                _hud.moveHeart(-3, 0);
            } else if (!_dialogManager.isActive() && !_inventoryHud.isVisible() && !_hud.isHUDVisible()) {
                _player.translateX(-3);
            }
        });

        // Inventar-Navigation (nur wenn Inventar sichtbar)
        onKeyDown(KeyCode.UP,    () -> { if (_inventoryHud.isVisible()) _inventoryHud.navigate(-1); });
        onKeyDown(KeyCode.DOWN,  () -> { if (_inventoryHud.isVisible()) _inventoryHud.navigate(+1); });

        // LEFT/RIGHT: HUD-Button-Navigation oder Inventar-Spalte (nicht in Ausweichphase)
        onKeyDown(KeyCode.LEFT, () -> {
            if (_inventoryHud.isVisible()) {
                _inventoryHud.navigate(-4);
            } else if (!_inDodgePhase && _hud.isBattleMenuVisible()) {
                int prev = (_hud.getSelectedButton() - 1 + 4) % 4;
                _hud.highlightButton(prev);
            }
        });
        onKeyDown(KeyCode.RIGHT, () -> {
            if (_inventoryHud.isVisible()) {
                _inventoryHud.navigate(+4);
            } else if (!_inDodgePhase && _hud.isBattleMenuVisible()) {
                int next = (_hud.getSelectedButton() + 1) % 4;
                _hud.highlightButton(next);
            }
        });

        // X schließt das Inventar → zurück zum HUD; außerhalb schließt HUD (nicht in Ausweichphase)
        onKeyDown(KeyCode.X, () -> {
            if (_inventoryHud.isVisible()) {
                _inventoryHud.hide();
                _hud.showHUD();
            } else if (!_inDodgePhase && _hud.isHUDVisible()) {
                _hud.hideAll();
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
                handleItemUse();
            } else if (!_inDodgePhase && _hud.isBattleMenuVisible()) {
                handleBattleMenuConfirm();
            } else if (!_inDodgePhase) {
                _dialogManager.advance();
            }
        });

        onKeyDown(KeyCode.E, () -> {
            if (_inventoryHud.isVisible()) {
                handleItemUse();
            } else if (!_inDodgePhase && _hud.isBattleMenuVisible()) {
                handleBattleMenuConfirm();
            } else if (!_dialogManager.isActive() && !_inDodgePhase) {
                // Welt-Interaktion
                if (_player.distanceBBox(_npc) < 60) {
                    _dialogManager.startDialog(java.util.List.of(
                        "Howdy! I'm Flowey.",
                        "Flowey the Flower!",
                        "Down here, LOVE is shared through...",
                        "...little white friendliness pellets!"
                    ));
                } else if (_player.distanceBBox(_enemy) < 60) {
                    _hud.showHUD();
                }
            }
        });
    }

    private void handleItemUse() {
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
    }

    private void handleBattleMenuConfirm() {
        int selected = _hud.getSelectedButton();
        if (selected == 0) {       // FIGHT
            startDodgePhase();
        } else if (selected == 2) { // ITEM
            _inventoryHud.show();
            _hud.hideAll();
        }
    }

    // --- Ausweichphase ---

    private void startDodgePhase() {
        _inDodgePhase = true;
        _hud.showBattleBoxOnly();
        _hud.showHeart();
        _dodgeTimerAction = getGameTimer().runAtInterval(this::spawnDodgeBullet, Duration.seconds(1.2));
        getGameTimer().runOnceAfter(this::endDodgePhase, Duration.seconds(6));
    }

    private void endDodgePhase() {
        if (!_inDodgePhase) return;
        _inDodgePhase = false;
        if (_dodgeTimerAction != null) {
            _dodgeTimerAction.expire();
            _dodgeTimerAction = null;
        }
        _hud.hideHeart();
        _hud.clearDodgeBullets();
        _hud.showHUD();
    }

    private void spawnDodgeBullet() {
        double innerX = OvertaleHud.BATTLE_INNER_X;
        double innerY = OvertaleHud.BATTLE_INNER_Y;
        double innerW = OvertaleHud.BATTLE_INNER_W;
        double innerH = OvertaleHud.BATTLE_INNER_H;

        int side = (int) (Math.random() * 4);
        double x, y;
        switch (side) {
            case 0 -> { x = innerX + Math.random() * innerW; y = innerY - 8; }           // oben
            case 1 -> { x = innerX + Math.random() * innerW; y = innerY + innerH + 1; }  // unten
            case 2 -> { x = innerX - 8;                      y = innerY + Math.random() * innerH; } // links
            default-> { x = innerX + innerW + 1;             y = innerY + Math.random() * innerH; } // rechts
        }

        double heartCX = _hud.getHeartX() + OvertaleHud.HEART_SIZE / 2.0;
        double heartCY = _hud.getHeartY() + OvertaleHud.HEART_SIZE / 2.0;
        Point2D dir = new Point2D(heartCX - x, heartCY - y).normalize().multiply(120);
        _hud.addDodgeBullet(x, y, dir.getX(), dir.getY());
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameEntityFactory());
        _player = spawn("player", 400, 300);
        _npc = spawn("npc", 200, 250);
        _enemy = spawn("enemy", 550, 300);

        _inventory = new Inventory();
        _inventory.addItem(new Engelssegen());
        _inventory.addItem(new Engelssegen());
        _inventory.addItem(new GoldenerNektar());
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(EntityType.PLAYER, EntityType.BULLET) {
                    @Override
                    protected void onCollisionBegin(Entity p, Entity bullet) {
                        if (_inDodgePhase) return; // in Ausweichphase: nur Herz-Kollision zählt
                        bullet.removeFromWorld();
                        _currentHP -= 2;
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

        _hud.updateHP(_currentHP, _maxHP);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (_inDodgePhase) {
            _hud.updateDodgeBullets(tpf);
            if (_hud.checkAndRemoveCollidingBullet()) {
                _currentHP -= 2;
                _hud.updateHP(_currentHP, _maxHP);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
