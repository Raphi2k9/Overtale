package at.htl.overtale;

import at.htl.overtale.component.BossComponent;
import at.htl.overtale.component.BossPattern;
import at.htl.overtale.component.LootChestComponent;
import at.htl.overtale.component.items.*;
import at.htl.overtale.entity.BossData;
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
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    // ── Bosses ────────────────────────────────────────────────────────────────

    private static final BossData[] BOSSES = {
        new BossData(
            "Finsternisgeist", 20,
            List.of(
                "So... ein weiterer Sterblicher betritt mein Reich.",
                "Diese Finsternis ist alles, was ich kenne.",
                "Du wirst bereuen, hierher gekommen zu sein!"
            ),
            List.of(
                "Der Finsternisgeist schießt Kugeln aus zufälligen Richtungen.",
                "Weiche aus und überlebe die Zeit, um ihm Schaden zuzufügen!"
            ),
            List.of(
                "Unmöglich... du hast mich besiegt.",
                "Meine Kraft... gehört nun dir.",
                "Die Finsternis... weicht zurück..."
            ),
            BossPattern.RANDOM_TARGET, 7.0, 1.4,
            640, 200,
            new Engelssegen()
        ),
        new BossData(
            "Schattenwächter", 40,
            List.of(
                "Ich habe von deinem Sieg gegen den Finsternisgeist gehört.",
                "Beeindruckend... aber ich bin eine völlig andere Herausforderung.",
                "Die Schatten gehorchen mir!",
                "Regen aus Dunkelheit... ANGRIFF!"
            ),
            List.of(
                "Der Schattenwächter lässt Kugeln von oben regnen.",
                "Bewege das Herz seitwärts, um den fallenden Kugeln auszuweichen!"
            ),
            List.of(
                "Du... bist stärker als ich dachte.",
                "Diese Klinge war mein wertvollster Besitz.",
                "Nimm sie... sie wird dir auf deinem weiteren Weg dienen."
            ),
            BossPattern.RAIN, 9.0, 0.75,
            200, 580,
            new HeiligesSchwert()
        ),
        new BossData(
            "Der Richter", 60,
            List.of(
                "Hier endet deine Reise, Sterblicher.",
                "Ich bin der Richter dieser Welt.",
                "Zwei meiner Brüder hast du bezwungen... beeindruckend.",
                "Aber kein Sterblicher hat je mein Urteil überstanden.",
                "GERICHTSTAG!"
            ),
            List.of(
                "Der Richter greift von allen vier Seiten gleichzeitig an!",
                "Finde die Lücken zwischen den Kugeln und weiche aus!"
            ),
            List.of(
                "Du... hast mein Urteil widerlegt.",
                "In all meinen Jahren... nie hat jemand das geschafft.",
                "Die Welt ist frei von meiner Last.",
                "Geh, Held. Du hast deine Würde bewiesen.",
                "LEVEL 1 ABGESCHLOSSEN!"
            ),
            BossPattern.CROSSFIRE, 12.0, 0.55,
            650, 580,
            new HeiliigeSchriftrolle()
        )
    };

    // ── State ─────────────────────────────────────────────────────────────────

    private OvertaleHud      _hud;
    private DialogManager    _dialogManager;
    private InventoryHud     _inventoryHud;
    private Inventory        _inventory;
    private Entity           _player;
    private Entity           _npc;
    private List<Entity>     _bossEntities = new ArrayList<>();
    private List<Entity>     _chests       = new ArrayList<>();

    private int _currentHP    = 20;
    private int _maxHP        = 40;
    private int _enemyHP      = 0;
    private int _enemyMaxHP   = 0;
    private int _currentBossIndex = -1;
    private int _bossesDefeated   = 0;
    private int _bonusDamage      = 0;

    private boolean     _inDodgePhase    = false;
    private TimerAction _dodgeTimerAction;

    // ── Settings ──────────────────────────────────────────────────────────────

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Overtale");
        settings.setVersion("0.1");
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    protected void initInput() {
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

        onKeyDown(KeyCode.UP,   () -> { if (_inventoryHud.isVisible()) _inventoryHud.navigate(-1); });
        onKeyDown(KeyCode.DOWN, () -> { if (_inventoryHud.isVisible()) _inventoryHud.navigate(+1); });

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

        onKeyDown(KeyCode.X, () -> {
            if (_inventoryHud.isVisible()) {
                _inventoryHud.hide();
                _hud.showHUD();
            } else if (!_inDodgePhase && _hud.isHUDVisible()) {
                _hud.hideAll();
            }
        });

        onKeyDown(KeyCode.Q, () -> {
            if (_inventoryHud.isVisible()) {
                String msg = _inventoryHud.dropSelected();
                if (msg != null) {
                    _inventoryHud.hide();
                    _dialogManager.startDialog(List.of(msg), () -> _hud.showHUD());
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
                Entity nearChest = getNearbyChest();
                Entity nearBoss  = getNearbyBoss();
                if (nearChest != null) {
                    handleOpenChest(nearChest);
                } else if (nearBoss != null) {
                    startBossFight(nearBoss);
                } else if (_npc != null && _player.distanceBBox(_npc) < 60) {
                    _dialogManager.startDialog(getNpcDialog());
                }
            }
        });
    }

    // ── Game init ─────────────────────────────────────────────────────────────

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameEntityFactory());
        setLevelFromMap("TestMap1.tmx");

        _player = spawn("player", 420, 380);
        _npc    = spawn("npc",    370, 380);

        // Boss 1 – purple, north-east
        _bossEntities.add(spawn("boss", new SpawnData(640, 200)
                .put("color", Color.web("#6B1FA3"))
                .put("stroke", Color.web("#CE93D8"))
                .put("bossIndex", 0)));

        // Boss 2 – dark blue, south-west
        _bossEntities.add(spawn("boss", new SpawnData(200, 580)
                .put("color", Color.web("#1A237E"))
                .put("stroke", Color.web("#82B1FF"))
                .put("bossIndex", 1)));

        // Boss 3 – dark slate with gold border, south-east
        _bossEntities.add(spawn("boss", new SpawnData(650, 580)
                .put("color", Color.web("#37474F"))
                .put("stroke", Color.web("#FFD700"))
                .put("bossIndex", 2)));

        // Two starter chests with weak healing items (no starting inventory)
        _chests.add(spawn("lootChest", new SpawnData(520, 280)
                .put("items", new Item[]{new Sonnenessenz()})));
        _chests.add(spawn("lootChest", new SpawnData(320, 500)
                .put("items", new Item[]{new Engelstraene()})));

        _inventory = new Inventory();

        getGameScene().getViewport().bindToEntity(_player, getAppWidth() / 2, getAppHeight() / 2);
        getGameScene().getViewport().setLazy(true);
        getGameScene().getViewport().setBounds(0, 0, 30 * 32, 30 * 32);
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(EntityType.PLAYER, EntityType.BULLET) {
                    @Override
                    protected void onCollisionBegin(Entity p, Entity bullet) {
                        if (_inDodgePhase) return;
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
        _hud.updateEnemyHP(0, 1);

        // Intro dialog on game start
        _dialogManager.startDialog(List.of(
            "Willkommen, Fremder...",
            "Diese Welt liegt im Dunkeln. Drei mächtige Wächter halten sie gefangen.",
            "Suche den Ratgeber (gelbe Figur) in deiner Nähe für mehr Informationen.",
            "Drücke E zum Interagieren."
        ));
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

    // ── Boss fight ────────────────────────────────────────────────────────────

    private void startBossFight(Entity bossEntity) {
        BossComponent bc = bossEntity.getComponent(BossComponent.class);
        _currentBossIndex = bc.getBossIndex();
        BossData boss = BOSSES[_currentBossIndex];

        _enemyMaxHP = boss.maxHp();
        _enemyHP    = boss.maxHp();

        _hud.setEnemyName(boss.name());
        _hud.updateEnemyHP(_enemyHP, _enemyMaxHP);

        _dialogManager.startDialog(boss.preDialog(), () -> _hud.showHUD());
    }

    private void startDodgePhase() {
        _inDodgePhase = true;
        _hud.showBattleBoxOnly();
        _hud.showHeart();

        BossData boss = BOSSES[_currentBossIndex];
        _dodgeTimerAction = getGameTimer().runAtInterval(
                this::spawnBulletsForPattern, Duration.seconds(boss.bulletInterval()));
        getGameTimer().runOnceAfter(this::endDodgePhase, Duration.seconds(boss.fightDuration()));
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

        int baseDamage = 5;
        _enemyHP = Math.max(0, _enemyHP - baseDamage - _bonusDamage);
        _bonusDamage = 0;
        _hud.updateEnemyHP(_enemyHP, _enemyMaxHP);

        if (_enemyHP <= 0) {
            handleBossDefeated();
        } else {
            _hud.showHUD();
        }
    }

    private void handleBossDefeated() {
        BossData boss      = BOSSES[_currentBossIndex];
        Entity bossEntity  = _bossEntities.get(_currentBossIndex);
        double chestX      = bossEntity.getX() + 24;
        double chestY      = bossEntity.getY() + 24;

        bossEntity.getComponent(BossComponent.class).setDefeated();
        bossEntity.removeFromWorld();
        _bossesDefeated++;

        Entity chest = spawn("lootChest",
                new SpawnData(chestX, chestY).put("items", new Item[]{boss.reward()}));
        _chests.add(chest);

        int finishedIdx   = _currentBossIndex;
        _currentBossIndex = -1;

        _hud.hideAll();
        _dialogManager.startDialog(boss.postDialog(), () -> {
            if (_bossesDefeated == BOSSES.length) {
                showVictory();
            }
        });
    }

    private void showVictory() {
        _dialogManager.startDialog(List.of(
            "Du hast alle drei Wächter besiegt!",
            "Das Licht kehrt in diese Welt zurück.",
            "Du bist ein wahrer Held dieser Welt.",
            "... Ende von Level 1 ..."
        ));
    }

    // ── Bullet patterns ───────────────────────────────────────────────────────

    private void spawnBulletsForPattern() {
        if (_currentBossIndex < 0) return;
        switch (BOSSES[_currentBossIndex].pattern()) {
            case RANDOM_TARGET -> spawnRandomTargetBullet();
            case RAIN          -> spawnRainBullets();
            case CROSSFIRE     -> spawnCrossfireBullets();
        }
    }

    /** One bullet from a random side, aimed at the heart. */
    private void spawnRandomTargetBullet() {
        double innerX = OvertaleHud.BATTLE_INNER_X;
        double innerY = OvertaleHud.BATTLE_INNER_Y;
        double innerW = OvertaleHud.BATTLE_INNER_W;
        double innerH = OvertaleHud.BATTLE_INNER_H;

        int side = (int)(Math.random() * 4);
        double x, y;
        switch (side) {
            case 0  -> { x = innerX + Math.random() * innerW; y = innerY - 8; }
            case 1  -> { x = innerX + Math.random() * innerW; y = innerY + innerH + 1; }
            case 2  -> { x = innerX - 8;                      y = innerY + Math.random() * innerH; }
            default -> { x = innerX + innerW + 1;             y = innerY + Math.random() * innerH; }
        }
        Point2D dir = aimAtHeart(x, y, 130);
        _hud.addDodgeBullet(x, y, dir.getX(), dir.getY());
    }

    /** 2-3 bullets fall straight down from random X positions at the top. */
    private void spawnRainBullets() {
        double innerX = OvertaleHud.BATTLE_INNER_X;
        double innerY = OvertaleHud.BATTLE_INNER_Y;
        double innerW = OvertaleHud.BATTLE_INNER_W;

        int count = 2 + (int)(Math.random() * 2);
        for (int i = 0; i < count; i++) {
            double x = innerX + Math.random() * innerW;
            _hud.addDodgeBullet(x, innerY - 8, 0, 165);
        }
    }

    /** 4 bullets from all sides simultaneously, each aimed at the heart. */
    private void spawnCrossfireBullets() {
        double innerX = OvertaleHud.BATTLE_INNER_X;
        double innerY = OvertaleHud.BATTLE_INNER_Y;
        double innerW = OvertaleHud.BATTLE_INNER_W;
        double innerH = OvertaleHud.BATTLE_INNER_H;

        double[][] origins = {
            {innerX + Math.random() * innerW, innerY - 8},
            {innerX + Math.random() * innerW, innerY + innerH + 1},
            {innerX - 8,          innerY + Math.random() * innerH},
            {innerX + innerW + 1, innerY + Math.random() * innerH}
        };
        for (double[] o : origins) {
            Point2D dir = aimAtHeart(o[0], o[1], 195);
            _hud.addDodgeBullet(o[0], o[1], dir.getX(), dir.getY());
        }
    }

    private Point2D aimAtHeart(double fromX, double fromY, double speed) {
        double hcx = _hud.getHeartX() + OvertaleHud.HEART_SIZE / 2.0;
        double hcy = _hud.getHeartY() + OvertaleHud.HEART_SIZE / 2.0;
        return new Point2D(hcx - fromX, hcy - fromY).normalize().multiply(speed);
    }

    // ── Battle menu ───────────────────────────────────────────────────────────

    private void handleBattleMenuConfirm() {
        switch (_hud.getSelectedButton()) {
            case 0 -> startDodgePhase();                   // FIGHT
            case 1 -> handleActButton();                   // ACT
            case 2 -> { _inventoryHud.show(); _hud.hideAll(); } // ITEM
            case 3 -> _hud.hideAll();                      // MERCY
        }
    }

    private void handleActButton() {
        if (_currentBossIndex < 0) return;
        BossData boss = BOSSES[_currentBossIndex];
        _hud.hideAll();
        _dialogManager.startDialog(boss.actDialog(), () -> _hud.showHUD());
    }

    // ── Item use ──────────────────────────────────────────────────────────────

    private void handleItemUse() {
        int slot = _inventoryHud.getSelectedSlot();
        Item item = _inventory.getItem(slot);
        if (item == null) return;

        int heal = item.getHealAmount();
        int dmg  = item.getDamageAmount();
        String msg = _inventoryHud.useSelected();

        if (heal > 0) {
            _currentHP = Math.min(_currentHP + heal, _maxHP);
            _hud.updateHP(_currentHP, _maxHP);
        }
        if (dmg > 0) {
            _bonusDamage += dmg;
        }
        _inventoryHud.hide();
        _dialogManager.startDialog(List.of(msg), () -> _hud.showHUD());
    }

    // ── Chest ─────────────────────────────────────────────────────────────────

    private void handleOpenChest(Entity chest) {
        LootChestComponent lcc = chest.getComponent(LootChestComponent.class);
        List<Item> loot = lcc.open();

        // Grey out the chest visually
        javafx.scene.Node view = chest.getViewComponent().getChildren().get(0);
        if (view instanceof javafx.scene.layout.StackPane sp) {
            javafx.scene.shape.Rectangle rect =
                    (javafx.scene.shape.Rectangle) sp.getChildren().get(0);
            rect.setFill(Color.web("#555555"));
            rect.setStroke(Color.GRAY);
            javafx.scene.text.Text label =
                    (javafx.scene.text.Text) sp.getChildren().get(1);
            label.setText("");
        }

        if (loot.isEmpty()) {
            _dialogManager.startDialog(List.of("Die Truhe ist leer."));
            return;
        }

        List<String> messages = new ArrayList<>();
        for (Item item : loot) {
            if (_inventory.addItem(item)) {
                messages.add("Du hast erhalten: " + item.getName());
            } else {
                messages.add("Inventar voll! " + item.getName() + " passt nicht rein.");
            }
        }
        _inventoryHud.refresh();
        _dialogManager.startDialog(messages);
    }

    // ── NPC dialog ────────────────────────────────────────────────────────────

    private List<String> getNpcDialog() {
        return switch (_bossesDefeated) {
            case 0 -> List.of(
                "Willkommen! Ich bin der Hüter dieses Ortes.",
                "Drei Wächter bedrohen diese Welt:",
                "Der Finsternisgeist (lila) im Norden...",
                "Der Schattenwächter (blau) im Südwesten...",
                "...und Der Richter (gold) im Südosten.",
                "Drücke E in ihrer Nähe, um den Kampf zu beginnen.",
                "In goldenen Truhen findest du nützliche Items."
            );
            case 1 -> List.of(
                "Gut gemacht! Den Finsternisgeist hast du bezwungen.",
                "Zwei Wächter warten noch. Der Schattenwächter ist schneller.",
                "Pass auf die fallenden Kugeln auf und bewege dich seitwärts!"
            );
            case 2 -> List.of(
                "Zwei von drei! Fast am Ziel.",
                "Der Richter ist der mächtigste von allen.",
                "Er greift von allen vier Seiten gleichzeitig an.",
                "Nutze alle Items die du hast – du wirst sie brauchen."
            );
            default -> List.of(
                "Du hast alle drei Wächter besiegt!",
                "Das Licht kehrt in diese Welt zurück.",
                "Du bist ein wahrhaftiger Held."
            );
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Entity getNearbyChest() {
        for (Entity chest : _chests) {
            LootChestComponent lcc = chest.getComponent(LootChestComponent.class);
            if (!lcc.isOpened() && _player.distanceBBox(chest) < 60) {
                return chest;
            }
        }
        return null;
    }

    private Entity getNearbyBoss() {
        for (Entity boss : _bossEntities) {
            BossComponent bc = boss.getComponent(BossComponent.class);
            if (!bc.isDefeated() && boss.isActive() && _player.distanceBBox(boss) < 60) {
                return boss;
            }
        }
        return null;
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        launch(args);
    }
}
