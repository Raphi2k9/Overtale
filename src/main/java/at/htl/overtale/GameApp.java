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
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    // ── NPC dialogs ───────────────────────────────────────────────────────────

    private static final List<String> SANS_DIALOG = List.of(
        "Heya. Ich bin Sans. Sans der Skelett.",
        "Du solltest vorsichtig sein hier oben... die Archons spielen nicht.",
        "Halo-Donut gefällig? Kostet nur ein bisschen SOUL."
    );

    private static final List<String> PAPYRUS_DIALOG = List.of(
        "NYYYYEH HEH HEH! ICH BIN DER GROSSE PAPYRUS!",
        "Ich trainiere gerade zum Engel! Ist das nicht wunderbar?!",
        "Sei vorsichtig — der Richter wartet oben auf dich!"
    );

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
            416, 1376,
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
            416, 576,
            new HeiligesSchwert()
        ),
        new BossData(
            "Der Richter", 30,
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
            416, 192,
            new HeiliigeSchriftrolle()
        )
    };

    // ── Der Richter phase data ────────────────────────────────────────────────

    private static final String[] RICHTER_PHASE_NAMES = {
        "Urteil",
        "Kreuzgericht",
        "Heiliger Regen",
        "Mauern des Himmels",
        "Göttliches Gericht"
    };
    private static final double[] RICHTER_DURATIONS = {5.0, 4.0, 5.0, 6.0, 6.0};
    private static final double[] RICHTER_INTERVALS = {0.6, 0.4, 0.15, 0.5, 0.4};

    // ── State ─────────────────────────────────────────────────────────────────

    private OvertaleHud      _hud;
    private DialogManager    _dialogManager;
    private InventoryHud     _inventoryHud;
    private Inventory        _inventory;
    private Entity           _player;
    private Entity           _sans;
    private Entity           _papyrus;
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
    private boolean     _isGameOver      = false;
    private int         _dodgeRound      = 0;
    private double      _richterCorridorOffset = 110;
    private TimerAction _dodgeTimerAction;
    private TimerAction _richterBoltTimerAction;
    private List<Node>  _gameOverNodes   = List.of();

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
                } else if (_sans != null && _player.distanceBBox(_sans) < 60) {
                    _dialogManager.startDialog(SANS_DIALOG);
                } else if (_papyrus != null && _player.distanceBBox(_papyrus) < 60) {
                    _dialogManager.startDialog(PAPYRUS_DIALOG);
                }
            }
        });

        onKey(KeyCode.R, () -> {
            if (!_isGameOver) return;
            _gameOverNodes.forEach(n -> getGameScene().removeUINode(n));
            _isGameOver = false;
            _currentHP  = _maxHP;
            getGameController().startNewGame();
        });
    }

    // ── Game init ─────────────────────────────────────────────────────────────

    @Override
    protected void initGame() {
        _bossEntities.clear();
        _chests.clear();
        _currentBossIndex      = -1;
        _bossesDefeated        = 0;
        _enemyHP               = 0;
        _enemyMaxHP            = 0;
        _bonusDamage           = 0;
        _inDodgePhase          = false;
        _dodgeRound            = 0;
        _dodgeTimerAction      = null;
        _richterBoltTimerAction = null;

        getGameWorld().addEntityFactory(new GameEntityFactory());
        setLevelFromMap("TestMap1.tmx");

        // col 13, row 47 → center of entry corridor, near bottom
        _player  = spawn("player", 416, 1504);
        // col 5, row 25 → left side of horizontal corridor
        _sans    = spawn("npc", 160, 800);
        // col 21, row 25 → right side of horizontal corridor
        _papyrus = spawn("npc", 672, 800);

        // Finsternisgeist – purple, entry corridor (col 13, row 43)
        _bossEntities.add(spawn("boss", new SpawnData(416, 1376)
                .put("color", Color.web("#6B1FA3"))
                .put("stroke", Color.web("#CE93D8"))
                .put("bossIndex", 0)));

        // Schattenwächter – dark blue, second vertical corridor mid-ascent (col 13, row 18)
        _bossEntities.add(spawn("boss", new SpawnData(416, 576)
                .put("color", Color.web("#1A237E"))
                .put("stroke", Color.web("#82B1FF"))
                .put("bossIndex", 1)));

        // Der Richter – dark slate with gold border, upper platform (col 13, row 6)
        _bossEntities.add(spawn("boss", new SpawnData(416, 192)
                .put("color", Color.web("#37474F"))
                .put("stroke", Color.web("#FFD700"))
                .put("bossIndex", 2)));

        // Chest 1 – entry corridor early reward (col 13, row 40)
        _chests.add(spawn("lootChest", new SpawnData(416, 1280)
                .put("items", new Item[]{new Sonnenessenz()})));
        // Chest 2 – left end of horizontal corridor (col 3, row 26)
        _chests.add(spawn("lootChest", new SpawnData(96, 832)
                .put("items", new Item[]{new Engelstraene()})));
        // Chest 3 – right end of horizontal corridor (col 22, row 26)
        _chests.add(spawn("lootChest", new SpawnData(704, 832)
                .put("items", new Item[]{new GoldenerNektar()})));

        _inventory = new Inventory();

        getGameScene().getViewport().bindToEntity(_player, getAppWidth() / 2, getAppHeight() / 2);
        getGameScene().getViewport().setLazy(true);
        // map is 30×50 tiles → 960×1600 px
        getGameScene().getViewport().setBounds(0, 0, 30 * 32, 50 * 32);
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
                        if (_currentHP < 0) _currentHP = 0;
                        _hud.updateHP(_currentHP, _maxHP);
                        checkPlayerDead();
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
                if (_currentHP < 0) _currentHP = 0;
                _hud.updateHP(_currentHP, _maxHP);
                checkPlayerDead();
            } else if (_hud.checkAndRemoveCollidingBolt()) {
                _currentHP -= 5;
                if (_currentHP < 0) _currentHP = 0;
                _hud.updateHP(_currentHP, _maxHP);
                checkPlayerDead();
            }
        }
    }

    // ── Boss fight ────────────────────────────────────────────────────────────

    private void startBossFight(Entity bossEntity) {
        BossComponent bc = bossEntity.getComponent(BossComponent.class);
        _currentBossIndex = bc.getBossIndex();
        _dodgeRound = 0;
        BossData boss = BOSSES[_currentBossIndex];

        _enemyMaxHP = boss.maxHp();
        _enemyHP    = boss.maxHp();

        _hud.setEnemyName(boss.name());
        _hud.updateEnemyHP(_enemyHP, _enemyMaxHP);

        _dialogManager.startDialog(boss.preDialog(), () -> _hud.showHUD());
    }

    private void startDodgePhase() {
        if (_currentBossIndex == 2) {
            int phase = richterPhaseIndex();
            String announce = "Runde " + (_dodgeRound + 1) + " — " + RICHTER_PHASE_NAMES[phase] + "...";
            _hud.showAnnouncement(announce);
            getGameTimer().runOnceAfter(this::beginRichterDodge, Duration.seconds(1.5));
        } else {
            beginStandardDodge();
        }
    }

    private void beginStandardDodge() {
        _inDodgePhase = true;
        _hud.showBattleBoxOnly();
        _hud.showHeart();
        BossData boss = BOSSES[_currentBossIndex];
        _dodgeTimerAction = getGameTimer().runAtInterval(
                this::spawnBulletsForPattern, Duration.seconds(boss.bulletInterval()));
        getGameTimer().runOnceAfter(this::endDodgePhase, Duration.seconds(boss.fightDuration()));
    }

    private void beginRichterDodge() {
        if (_isGameOver) return;
        _inDodgePhase = true;
        _hud.showBattleBoxOnly();
        _hud.showHeart();

        int phase = richterPhaseIndex();
        _dodgeTimerAction = getGameTimer().runAtInterval(
                this::spawnRichterPhase, Duration.seconds(RICHTER_INTERVALS[phase]));
        getGameTimer().runOnceAfter(this::endDodgePhase, Duration.seconds(RICHTER_DURATIONS[phase]));

        if (phase == 3) {
            _richterCorridorOffset = (OvertaleHud.BATTLE_INNER_H - 80) / 2.0;
            getGameTimer().runOnceAfter(
                    () -> _richterCorridorOffset = 30.0, Duration.seconds(2.0));
            getGameTimer().runOnceAfter(
                    () -> _richterCorridorOffset = OvertaleHud.BATTLE_INNER_H - 80 - 30,
                    Duration.seconds(4.0));
        }
        if (phase == 4) {
            _richterBoltTimerAction = getGameTimer().runAtInterval(
                    this::spawnRichterBolt, Duration.seconds(1.5));
        }
    }

    private void stopDodgePhase() {
        if (!_inDodgePhase) return;
        _inDodgePhase = false;
        if (_dodgeTimerAction != null) {
            _dodgeTimerAction.expire();
            _dodgeTimerAction = null;
        }
        if (_richterBoltTimerAction != null) {
            _richterBoltTimerAction.expire();
            _richterBoltTimerAction = null;
        }
        _hud.hideHeart();
        _hud.clearDodgeBullets();
    }

    private void endDodgePhase() {
        stopDodgePhase();
        checkPlayerDead();
        if (_isGameOver) return;

        if (_currentBossIndex == 2) _dodgeRound++;

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

    // ── Der Richter phase system ──────────────────────────────────────────────

    /** Phase 0 at round 0, then cycles 1→4 for rounds 1, 2, 3, 4, 5→1, 6→2, … */
    private int richterPhaseIndex() {
        return (_dodgeRound == 0) ? 0 : ((_dodgeRound - 1) % 4) + 1;
    }

    private void spawnRichterPhase() {
        if (!_inDodgePhase) return;
        switch (richterPhaseIndex()) {
            case 0 -> spawnRichterUrteil();
            case 1 -> spawnRichterKreuzgericht();
            case 2 -> spawnRichterHeiligerRegen();
            case 3 -> spawnRichterMauern();
            case 4 -> spawnRichterGoettlichesGericht();
        }
    }

    /** Round 0 — Urteil: 4 bullets from all sides aimed at heart, speed 120. */
    private void spawnRichterUrteil() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double[][] origins = {
            {ix + Math.random() * iw, iy - 8},
            {ix + Math.random() * iw, iy + ih + 1},
            {ix - 8,      iy + Math.random() * ih},
            {ix + iw + 1, iy + Math.random() * ih}
        };
        for (double[] o : origins) {
            Point2D dir = aimAtHeart(o[0], o[1], 120);
            _hud.addDodgeBullet(o[0], o[1], dir.getX(), dir.getY());
        }
    }

    /** Round 1 — Kreuzgericht: two diagonal streams crossing at 45°, speed 160. */
    private void spawnRichterKreuzgericht() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double diag = 160.0 / Math.sqrt(2);

        // Stream 1: top-left half + left edge → down-right
        _hud.addDodgeBullet(ix + Math.random() * (iw / 2), iy - 8,  diag,  diag);
        _hud.addDodgeBullet(ix - 8, iy + Math.random() * (ih / 2),  diag,  diag);
        // Stream 2: top-right half + right edge → down-left
        _hud.addDodgeBullet(ix + iw / 2 + Math.random() * (iw / 2), iy - 8, -diag, diag);
        _hud.addDodgeBullet(ix + iw + 8, iy + Math.random() * (ih / 2), -diag, diag);
    }

    /** Round 2 — Heiliger Regen: dense rain from top, speed 200. */
    private void spawnRichterHeiligerRegen() {
        double x = OvertaleHud.BATTLE_INNER_X + Math.random() * OvertaleHud.BATTLE_INNER_W;
        _hud.addDodgeBullet(x, OvertaleHud.BATTLE_INNER_Y - 8, 0, 200);
    }

    /** Round 3 — Mauern des Himmels: walls from left+right with a shifting 80px corridor. */
    private void spawnRichterMauern() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double corridorTop = iy + _richterCorridorOffset;
        double corridorBot = corridorTop + 80;
        int steps = 8;
        for (int i = 0; i < steps; i++) {
            double y = iy + (i + 0.5) * (ih / steps);
            if (y >= corridorTop && y <= corridorBot) continue;
            _hud.addDodgeBullet(ix - 8,      y, 140, 0);
            _hud.addDodgeBullet(ix + iw + 8, y, -140, 0);
        }
    }

    /** Round 4 — Göttliches Gericht: dense crossfire from all sides at speed 180. */
    private void spawnRichterGoettlichesGericht() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double[][] origins = {
            {ix + Math.random() * iw, iy - 8},
            {ix + Math.random() * iw, iy - 8},
            {ix + Math.random() * iw, iy + ih + 1},
            {ix + Math.random() * iw, iy + ih + 1},
            {ix - 8,      iy + Math.random() * ih},
            {ix - 8,      iy + Math.random() * ih},
            {ix + iw + 1, iy + Math.random() * ih},
            {ix + iw + 1, iy + Math.random() * ih}
        };
        for (double[] o : origins) {
            Point2D dir = aimAtHeart(o[0], o[1], 180);
            _hud.addDodgeBullet(o[0], o[1], dir.getX(), dir.getY());
        }
    }

    /** Richter bolt: 20×20 gold projectile from top center, speed 90, deals 5 damage. */
    private void spawnRichterBolt() {
        if (!_inDodgePhase) return;
        double centerX = OvertaleHud.BATTLE_INNER_X + OvertaleHud.BATTLE_INNER_W / 2.0 - 10;
        _hud.addRichterBolt(centerX, OvertaleHud.BATTLE_INNER_Y - 20, 0, 90);
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
        Node view = chest.getViewComponent().getChildren().get(0);
        if (view instanceof javafx.scene.layout.StackPane sp) {
            Rectangle rect = (Rectangle) sp.getChildren().get(0);
            rect.setFill(Color.web("#555555"));
            rect.setStroke(Color.GRAY);
            Text label = (Text) sp.getChildren().get(1);
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

    // ── Death / game over ─────────────────────────────────────────────────────

    private void checkPlayerDead() {
        if (_currentHP > 0) return;
        if (_isGameOver) return;
        _isGameOver = true;
        stopDodgePhase();
        _hud.hideAll();

        Rectangle overlay = new Rectangle(800, 600, Color.color(0, 0, 0, 0.88));

        Text gameOver = new Text("GAME OVER");
        gameOver.setFont(Font.font(64));
        gameOver.setFill(Color.RED);
        gameOver.setX(400 - gameOver.getLayoutBounds().getWidth() / 2);
        gameOver.setY(270);

        Text restart = new Text("R — Neustart");
        restart.setFont(Font.font(20));
        restart.setFill(Color.WHITE);
        restart.setX(400 - restart.getLayoutBounds().getWidth() / 2);
        restart.setY(320);

        getGameScene().addUINode(overlay);
        getGameScene().addUINode(gameOver);
        getGameScene().addUINode(restart);
        _gameOverNodes = List.of(overlay, gameOver, restart);
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
            if (!boss.isActive()) continue;
            BossComponent bc = boss.getComponent(BossComponent.class);
            if (!bc.isDefeated() && _player.distanceBBox(boss) < 60) {
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
