package at.htl.overtale;

import at.htl.overtale.component.BossComponent;
import at.htl.overtale.component.BossPattern;
import at.htl.overtale.component.LootChestComponent;
import at.htl.overtale.component.items.Item;
import at.htl.overtale.component.items.ItemFactory;
import at.htl.overtale.component.items.Inventory;
import at.htl.overtale.data.ChestConfig;
import at.htl.overtale.data.DialogConfig;
import at.htl.overtale.data.GameDataLoader;
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

    // ── Data (loaded from JSON) ───────────────────────────────────────────────

    private BossData[]   _bosses;
    private DialogConfig _dialogs;

    // ── State ─────────────────────────────────────────────────────────────────

    private final GameMusicPlayer _music = new GameMusicPlayer();

    private OvertaleHud      _hud;
    private DialogManager    _dialogManager;
    private InventoryHud     _inventoryHud;
    private Inventory        _inventory;
    private Entity           _player;
    private Entity           _toriel;
    private Entity           _undyne;
    private Entity           _alphys;
    private Entity           _sans;
    private Entity           _papyrus;
    private List<Entity>     _bossEntities = new ArrayList<>();
    private List<Entity>     _chests       = new ArrayList<>();

    private int _currentHP        = 20;
    private int _maxHP            = 40;
    private int _enemyHP          = 0;
    private int _enemyMaxHP       = 0;
    private int _currentBossIndex = -1;
    private int _bossesDefeated   = 0;
    private int _bonusDamage      = 0;

    private double      _spiralAngle          = 0.0;
    private boolean     _inDodgePhase         = false;
    private boolean     _isGameOver           = false;
    private int         _dodgeRound           = 0;
    private double      _richterCorridorOffset = 110;
    private double      _damageCooldown       = 0.0;
    private TimerAction _dodgeTimerAction;
    private TimerAction _richterBoltTimerAction;
    private List<Node>  _gameOverNodes        = List.of();
    private List<Node>  _winNodes             = List.of();
    private boolean     _isVictory            = false;

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
        onKeyDown(KeyCode.ESCAPE, () ->
            javafx.application.Platform.runLater(() -> {
                if (getSceneService().getCurrentScene() != getGameScene()) {
                    playMenuMusic();
                } else {
                    if (_currentBossIndex >= 0) playFightMusic();
                    else _music.stop();
                }
            })
        );

        onKey(KeyCode.W, () -> {
            if (_inDodgePhase) {
                if (!_hud.isGravityEnabled()) _hud.moveHeart(0, -3);
            } else if (!_dialogManager.isActive() && !_inventoryHud.isVisible() && !_hud.isHUDVisible()) {
                _player.translateY(-3);
            }
        });
        onKeyDown(KeyCode.SPACE, () -> {
            if (_inDodgePhase && _hud.isGravityEnabled()) _hud.jumpHeart();
        });
        onKey(KeyCode.S, () -> {
            if (_inDodgePhase) {
                if (!_hud.isGravityEnabled()) _hud.moveHeart(0, 3);
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
                _hud.highlightButton((_hud.getSelectedButton() - 1 + 4) % 4);
            }
        });
        onKeyDown(KeyCode.RIGHT, () -> {
            if (_inventoryHud.isVisible()) {
                _inventoryHud.navigate(+4);
            } else if (!_inDodgePhase && _hud.isBattleMenuVisible()) {
                _hud.highlightButton((_hud.getSelectedButton() + 1) % 4);
            }
        });

        onKeyDown(KeyCode.X, () -> {
            if (_inventoryHud.isVisible()) { _inventoryHud.hide(); _hud.showHUD(); }
            else if (!_inDodgePhase && _hud.isHUDVisible()) _hud.hideAll();
        });

        onKeyDown(KeyCode.Q, () -> {
            if (_inventoryHud.isVisible()) {
                String msg = _inventoryHud.dropSelected();
                if (msg != null) { _inventoryHud.hide(); _dialogManager.startDialog(List.of(msg), () -> _hud.showHUD()); }
            }
        });

        onKeyDown(KeyCode.Z, () -> {
            if (_inventoryHud.isVisible())                           handleItemUse();
            else if (!_inDodgePhase && _hud.isBattleMenuVisible())  handleBattleMenuConfirm();
            else if (!_inDodgePhase)                                 _dialogManager.advance();
        });

        onKeyDown(KeyCode.E, () -> {
            if (_inventoryHud.isVisible()) {
                handleItemUse();
            } else if (!_inDodgePhase && _hud.isBattleMenuVisible()) {
                handleBattleMenuConfirm();
            } else if (!_dialogManager.isActive() && !_inDodgePhase) {
                Entity nearChest = getNearbyChest();
                Entity nearBoss  = getNearbyBoss();
                if      (nearChest != null)                              handleOpenChest(nearChest);
                else if (nearBoss  != null)                              startBossFight(nearBoss);
                else if (_toriel  != null && _player.distanceBBox(_toriel)  < 60) _dialogManager.startDialog(_dialogs.toriel);
                else if (_undyne  != null && _player.distanceBBox(_undyne)  < 60) _dialogManager.startDialog(_dialogs.undyne);
                else if (_alphys  != null && _player.distanceBBox(_alphys)  < 60) _dialogManager.startDialog(_dialogs.alphys);
                else if (_sans    != null && _player.distanceBBox(_sans)    < 60) _dialogManager.startDialog(_dialogs.sans);
                else if (_papyrus != null && _player.distanceBBox(_papyrus) < 60) _dialogManager.startDialog(_dialogs.papyrus);
            }
        });

        onKey(KeyCode.R, () -> {
            if (!_isGameOver && !_isVictory) return;
            _gameOverNodes.forEach(n -> getGameScene().removeUINode(n));
            _winNodes.forEach(n -> getGameScene().removeUINode(n));
            _isGameOver = false;
            _isVictory  = false;
            _currentHP  = 20;
            getGameController().startNewGame();
        });
    }

    // ── Music ─────────────────────────────────────────────────────────────────

    private void playMenuMusic() {
        _music.loop("Overtale_Sample_Song_1.mp3");
    }

    private void playFightMusic() {
        _music.loop("Overtale_song_2.mp3");
    }

    // ── Game init ─────────────────────────────────────────────────────────────

    @Override
    protected void initGame() {
        _bosses  = GameDataLoader.loadBosses();
        _dialogs = GameDataLoader.loadDialogs();

        _music.stop();

        _bossEntities.clear();
        _chests.clear();
        _currentBossIndex      = -1;
        _bossesDefeated        = 0;
        _enemyHP               = 0;
        _enemyMaxHP            = 0;
        _bonusDamage           = 0;
        _inDodgePhase          = false;
        _dodgeRound            = 0;
        _damageCooldown        = 0.0;
        _spiralAngle           = 0.0;
        _isVictory             = false;
        _winNodes              = List.of();
        _dodgeTimerAction      = null;
        _richterBoltTimerAction = null;

        getGameWorld().addEntityFactory(new GameEntityFactory());
        setLevelFromMap("TestMap1.tmx");

        _player  = spawn("player", 752, 2192);
        _toriel  = spawn("npc", new SpawnData(656, 1520).put("color", Color.web("#FF80AB")));
        _undyne  = spawn("npc", new SpawnData(976, 2128).put("color", Color.web("#42A5F5")));
        _alphys  = spawn("npc", new SpawnData(752,  944).put("color", Color.web("#A5D6A7")));
        _sans    = spawn("npc", 624, 1648);
        _papyrus = spawn("npc", 848, 1648);

        for (int i = 0; i < _bosses.length; i++) {
            BossData b = _bosses[i];
            _bossEntities.add(spawn("boss", new SpawnData(b.spawnX(), b.spawnY())
                    .put("color",     Color.web(b.color()))
                    .put("stroke",    Color.web(b.stroke()))
                    .put("bossIndex", i)));
        }

        for (ChestConfig cc : GameDataLoader.loadChests()) {
            _chests.add(spawn("lootChest", new SpawnData(cc.x, cc.y)
                    .put("items", new Item[]{ItemFactory.create(cc.item)})));
        }

        _inventory = new Inventory();

        getGameScene().getViewport().bindToEntity(_player, getAppWidth() / 2, getAppHeight() / 2);
        getGameScene().getViewport().setLazy(true);
        getGameScene().getViewport().setBounds(0, 0, 60 * 32, 70 * 32);
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(EntityType.PLAYER, EntityType.BULLET) {
                    @Override
                    protected void onCollisionBegin(Entity p, Entity bullet) {
                        if (_inDodgePhase) return;
                        bullet.removeFromWorld();
                        takeDamage(2);
                    }
                }
        );
    }

    @Override
    protected void initUI() {
        _hud = new OvertaleHud();
        _hud.build();
        _dialogManager = new DialogManager(_hud);
        _inventoryHud  = new InventoryHud(_inventory);
        _inventoryHud.build();

        _hud.updateHP(_currentHP, _maxHP);
        _hud.updateEnemyHP(0, 1);
        _dialogManager.startDialog(_dialogs.intro);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (_damageCooldown > 0) _damageCooldown -= tpf;
        if (_inDodgePhase) {
            _hud.updateDodgeBullets(tpf);
            _hud.updateGravity(tpf);
            _hud.updateObstacles(tpf);
            _hud.updateLasers(tpf);
            if (_damageCooldown <= 0) {
                if      (_hud.checkAndRemoveCollidingBullet()) { takeDamage(2); }
                else if (_hud.checkAndRemoveCollidingBolt())   { takeDamage(5); }
                else if (_hud.checkLaserCollision())           { takeDamage(2); _damageCooldown = 0.7; }
                else if (_hud.checkObstacleCollision())        { takeDamage(2); _damageCooldown = 0.5; }
            }
        }
    }

    private void takeDamage(int dmg) {
        _currentHP -= dmg;
        if (_currentHP < 0) _currentHP = 0;
        _hud.updateHP(_currentHP, _maxHP);
        checkPlayerDead();
    }

    // ── Boss fight ────────────────────────────────────────────────────────────

    private void startBossFight(Entity bossEntity) {
        _currentBossIndex = bossEntity.getComponent(BossComponent.class).getBossIndex();
        _dodgeRound = 0;
        BossData boss = _bosses[_currentBossIndex];
        _enemyMaxHP = boss.maxHp();
        _enemyHP    = boss.maxHp();
        _hud.setEnemyName(boss.name());
        _hud.updateEnemyHP(_enemyHP, _enemyMaxHP);
        playFightMusic();
        _dialogManager.startDialog(boss.preDialog(), () -> _hud.showHUD());
    }

    private void startDodgePhase() {
        BossData boss = _bosses[_currentBossIndex];
        if (boss.pattern() == BossPattern.GRAVITY_JUMP) {
            beginGravityDodge();
        } else if (boss.hasPhases()) {
            int phase = phaseIndex();
            _hud.showAnnouncement("Runde " + (_dodgeRound + 1) + " — " + boss.phaseNames()[phase] + "...");
            getGameTimer().runOnceAfter(
                    boss.pattern() == BossPattern.LASER ? this::beginAugeDodge : this::beginRichterDodge,
                    Duration.seconds(1.5));
        } else {
            beginStandardDodge();
        }
    }

    private void beginStandardDodge() {
        _inDodgePhase = true;
        _hud.showBattleBoxOnly();
        _hud.showHeart();
        BossData boss = _bosses[_currentBossIndex];
        _dodgeTimerAction = getGameTimer().runAtInterval(
                this::spawnBulletsForPattern, Duration.seconds(boss.bulletInterval()));
        getGameTimer().runOnceAfter(this::endDodgePhase, Duration.seconds(boss.fightDuration()));
    }

    private void beginRichterDodge() {
        if (_isGameOver) return;
        _inDodgePhase = true;
        _hud.showBattleBoxOnly();
        _hud.showHeart();

        BossData boss  = _bosses[_currentBossIndex];
        int      phase = phaseIndex();
        _dodgeTimerAction = getGameTimer().runAtInterval(
                this::spawnRichterPhase, Duration.seconds(boss.phaseIntervals()[phase]));
        getGameTimer().runOnceAfter(this::endDodgePhase, Duration.seconds(boss.phaseDurations()[phase]));

        if (phase == 3) {
            _richterCorridorOffset = (OvertaleHud.BATTLE_INNER_H - 80) / 2.0;
            getGameTimer().runOnceAfter(() -> _richterCorridorOffset = 30.0,                            Duration.seconds(2.0));
            getGameTimer().runOnceAfter(() -> _richterCorridorOffset = OvertaleHud.BATTLE_INNER_H - 110, Duration.seconds(4.0));
        }
        if (phase == 4) {
            _richterBoltTimerAction = getGameTimer().runAtInterval(this::spawnRichterBolt, Duration.seconds(1.5));
        }
    }

    private void stopDodgePhase() {
        if (!_inDodgePhase) return;
        _inDodgePhase = false;
        if (_dodgeTimerAction      != null) { _dodgeTimerAction.expire();      _dodgeTimerAction      = null; }
        if (_richterBoltTimerAction != null) { _richterBoltTimerAction.expire(); _richterBoltTimerAction = null; }
        _hud.hideHeart();
        _hud.clearDodgeBullets();
    }

    private void endDodgePhase() {
        stopDodgePhase();
        checkPlayerDead();
        if (_isGameOver) return;

        if (_bosses[_currentBossIndex].hasPhases()) _dodgeRound++;

        _enemyHP = Math.max(0, _enemyHP - 5 - _bonusDamage);
        _bonusDamage = 0;
        _hud.updateEnemyHP(_enemyHP, _enemyMaxHP);

        if (_enemyHP <= 0) handleBossDefeated();
        else               _hud.showHUD();
    }

    private void handleBossDefeated() {
        BossData boss     = _bosses[_currentBossIndex];
        Entity bossEntity = _bossEntities.get(_currentBossIndex);
        bossEntity.getComponent(BossComponent.class).setDefeated();
        bossEntity.removeFromWorld();
        _bossesDefeated++;

        _chests.add(spawn("lootChest", new SpawnData(bossEntity.getX() + 24, bossEntity.getY() + 24)
                .put("items", new Item[]{boss.reward()})));

        _currentBossIndex = -1;
        _music.stop();
        _hud.hideAll();
        _dialogManager.startDialog(boss.postDialog(), () -> {
            if (_bossesDefeated >= _bosses.length) showVictory();
        });
    }

    private void showVictory() {
        _dialogManager.startDialog(_dialogs.victory, this::showWinScreen);
    }

    private void showWinScreen() {
        _isVictory = true;
        _hud.hideAll();

        Rectangle overlay = new Rectangle(800, 600, Color.color(0, 0, 0, 0.92));

        Rectangle goldBar = new Rectangle(0, 195, 800, 210);
        goldBar.setFill(Color.color(0.12, 0.09, 0.0, 0.9));

        Text stars = new Text("★  ★  ★  ★  ★");
        stars.setFont(Font.font("Monospaced", 22));
        stars.setFill(Color.web("#FFD700"));
        stars.setX(400 - stars.getLayoutBounds().getWidth() / 2);
        stars.setY(230);

        Text title = new Text("LEVEL 1 ABGESCHLOSSEN!");
        title.setFont(Font.font("Monospaced", 34));
        title.setFill(Color.web("#FFD700"));
        title.setX(400 - title.getLayoutBounds().getWidth() / 2);
        title.setY(285);

        Text subtitle = new Text("Alle Archons wurden bezwungen.");
        subtitle.setFont(Font.font("Monospaced", 17));
        subtitle.setFill(Color.web("#FFFDE7"));
        subtitle.setX(400 - subtitle.getLayoutBounds().getWidth() / 2);
        subtitle.setY(325);

        Text hpLine = new Text("HP übrig:  " + _currentHP + " / " + _maxHP);
        hpLine.setFont(Font.font("Monospaced", 15));
        hpLine.setFill(Color.web("#A5D6A7"));
        hpLine.setX(400 - hpLine.getLayoutBounds().getWidth() / 2);
        hpLine.setY(362);

        Text restart = new Text("R  —  Neustart");
        restart.setFont(Font.font("Monospaced", 15));
        restart.setFill(Color.web("#9E9E9E"));
        restart.setX(400 - restart.getLayoutBounds().getWidth() / 2);
        restart.setY(395);

        getGameScene().addUINode(overlay);
        getGameScene().addUINode(goldBar);
        getGameScene().addUINode(stars);
        getGameScene().addUINode(title);
        getGameScene().addUINode(subtitle);
        getGameScene().addUINode(hpLine);
        getGameScene().addUINode(restart);

        _winNodes = List.of(overlay, goldBar, stars, title, subtitle, hpLine, restart);
    }

    // ── Phase index helpers ───────────────────────────────────────────────────

    /** Cycles through all phases; first round always maps to phase 0 for Richter-style bosses. */
    private int phaseIndex() {
        BossData boss = _bosses[_currentBossIndex];
        int n = boss.phaseNames().length;
        if (boss.pattern() == BossPattern.LASER) return _dodgeRound % n;
        return (_dodgeRound == 0) ? 0 : ((_dodgeRound - 1) % (n - 1)) + 1;
    }

    // ── Bullet patterns ───────────────────────────────────────────────────────

    private void spawnBulletsForPattern() {
        if (_currentBossIndex < 0) return;
        switch (_bosses[_currentBossIndex].pattern()) {
            case RANDOM_TARGET -> spawnRandomTargetBullet();
            case RAIN          -> spawnRainBullets();
            case CROSSFIRE     -> spawnCrossfireBullets();
            case SPIRAL        -> spawnSpiralBullets();
        }
    }

    private void spawnRandomTargetBullet() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        int side = (int)(Math.random() * 4);
        double x, y;
        switch (side) {
            case 0  -> { x = ix + Math.random() * iw; y = iy - 8; }
            case 1  -> { x = ix + Math.random() * iw; y = iy + ih + 1; }
            case 2  -> { x = ix - 8;                  y = iy + Math.random() * ih; }
            default -> { x = ix + iw + 1;             y = iy + Math.random() * ih; }
        }
        Point2D dir = aimAtHeart(x, y, 130);
        _hud.addDodgeBullet(x, y, dir.getX(), dir.getY());
    }

    private void spawnRainBullets() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W;
        int count = 2 + (int)(Math.random() * 2);
        for (int i = 0; i < count; i++)
            _hud.addDodgeBullet(ix + Math.random() * iw, iy - 8, 0, 165);
    }

    private void spawnCrossfireBullets() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double[][] origins = {
            {ix + Math.random() * iw, iy - 8},
            {ix + Math.random() * iw, iy + ih + 1},
            {ix - 8,      iy + Math.random() * ih},
            {ix + iw + 1, iy + Math.random() * ih}
        };
        for (double[] o : origins) {
            Point2D dir = aimAtHeart(o[0], o[1], 195);
            _hud.addDodgeBullet(o[0], o[1], dir.getX(), dir.getY());
        }
    }

    private void spawnSpiralBullets() {
        double cx = OvertaleHud.BATTLE_INNER_X + OvertaleHud.BATTLE_INNER_W / 2.0 - 5;
        double cy = OvertaleHud.BATTLE_INNER_Y + OvertaleHud.BATTLE_INNER_H / 2.0 - 5;
        int count = 8;
        double speed = 145.0;
        for (int i = 0; i < count; i++) {
            double angle = _spiralAngle + i * (2 * Math.PI / count);
            _hud.addDodgeBullet(cx, cy, Math.cos(angle) * speed, Math.sin(angle) * speed);
        }
        _spiralAngle += Math.PI / 8.0;
    }

    private Point2D aimAtHeart(double fromX, double fromY, double speed) {
        double hcx = _hud.getHeartX() + OvertaleHud.HEART_SIZE / 2.0;
        double hcy = _hud.getHeartY() + OvertaleHud.HEART_SIZE / 2.0;
        return new Point2D(hcx - fromX, hcy - fromY).normalize().multiply(speed);
    }

    // ── Der Richter – multi-phase crossfire ───────────────────────────────────

    private void spawnRichterPhase() {
        if (!_inDodgePhase) return;
        switch (phaseIndex()) {
            case 0 -> spawnRichterUrteil();
            case 1 -> spawnRichterKreuzgericht();
            case 2 -> spawnRichterHeiligerRegen();
            case 3 -> spawnRichterMauern();
            case 4 -> spawnRichterGoettlichesGericht();
        }
    }

    private void spawnRichterUrteil() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double[][] o = {
            {ix + Math.random() * iw, iy - 8},
            {ix + Math.random() * iw, iy + ih + 1},
            {ix - 8,      iy + Math.random() * ih},
            {ix + iw + 1, iy + Math.random() * ih}
        };
        for (double[] p : o) { Point2D d = aimAtHeart(p[0], p[1], 120); _hud.addDodgeBullet(p[0], p[1], d.getX(), d.getY()); }
    }

    private void spawnRichterKreuzgericht() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double diag = 160.0 / Math.sqrt(2);
        _hud.addDodgeBullet(ix + Math.random() * (iw / 2),        iy - 8,  diag,  diag);
        _hud.addDodgeBullet(ix - 8, iy + Math.random() * (ih / 2),          diag,  diag);
        _hud.addDodgeBullet(ix + iw / 2 + Math.random() * (iw / 2), iy - 8, -diag, diag);
        _hud.addDodgeBullet(ix + iw + 8, iy + Math.random() * (ih / 2),     -diag, diag);
    }

    private void spawnRichterHeiligerRegen() {
        _hud.addDodgeBullet(OvertaleHud.BATTLE_INNER_X + Math.random() * OvertaleHud.BATTLE_INNER_W,
                OvertaleHud.BATTLE_INNER_Y - 8, 0, 200);
    }

    private void spawnRichterMauern() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double corridorTop = iy + _richterCorridorOffset;
        double corridorBot = corridorTop + 80;
        for (int i = 0; i < 8; i++) {
            double y = iy + (i + 0.5) * (ih / 8);
            if (y >= corridorTop && y <= corridorBot) continue;
            _hud.addDodgeBullet(ix - 8,      y,  140, 0);
            _hud.addDodgeBullet(ix + iw + 8, y, -140, 0);
        }
    }

    private void spawnRichterGoettlichesGericht() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double[][] o = {
            {ix + Math.random() * iw, iy - 8},      {ix + Math.random() * iw, iy - 8},
            {ix + Math.random() * iw, iy + ih + 1}, {ix + Math.random() * iw, iy + ih + 1},
            {ix - 8,      iy + Math.random() * ih}, {ix - 8,      iy + Math.random() * ih},
            {ix + iw + 1, iy + Math.random() * ih}, {ix + iw + 1, iy + Math.random() * ih}
        };
        for (double[] p : o) { Point2D d = aimAtHeart(p[0], p[1], 180); _hud.addDodgeBullet(p[0], p[1], d.getX(), d.getY()); }
    }

    private void spawnRichterBolt() {
        if (!_inDodgePhase) return;
        _hud.addRichterBolt(OvertaleHud.BATTLE_INNER_X + OvertaleHud.BATTLE_INNER_W / 2.0 - 10,
                OvertaleHud.BATTLE_INNER_Y - 20, 0, 90);
    }

    // ── Der Springer – gravity jump-and-run ──────────────────────────────────

    private void beginGravityDodge() {
        if (_isGameOver) return;
        _inDodgePhase = true;
        _hud.showBattleBoxOnly();
        _hud.enableGravity();
        BossData boss = _bosses[_currentBossIndex];
        _dodgeTimerAction = getGameTimer().runAtInterval(
                this::spawnGravityObstacle, Duration.seconds(boss.bulletInterval()));
        getGameTimer().runOnceAfter(this::endDodgePhase, Duration.seconds(boss.fightDuration()));
    }

    private void spawnGravityObstacle() {
        if (!_inDodgePhase) return;
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double startX = ix + iw + 5;
        double speed  = -190.0;
        // Apex at iy+96 (JUMP_VELOCITY=420, GRAVITY=460). Gap anchored ±20 px around it.
        switch ((int)(Math.random() * 3)) {
            case 0 -> {
                double gapY = iy + 36 + Math.random() * 40;
                _hud.addObstacle(startX, iy,          22, gapY - iy,             speed);
                _hud.addObstacle(startX, gapY + 80,   22, iy + ih - (gapY + 80), speed);
            }
            case 1 -> {
                double h = 50 + Math.random() * 60;
                _hud.addObstacle(startX, iy + ih - h, 22, h, speed);
            }
            default -> {
                double h1 = 60 + Math.random() * 40, h2 = 60 + Math.random() * 40;
                _hud.addObstacle(startX,      iy + ih - h1, 20, h1, speed);
                _hud.addObstacle(startX + 36, iy + ih - h2, 20, h2, speed);
            }
        }
    }

    // ── Das Auge – laser patterns ─────────────────────────────────────────────

    private void beginAugeDodge() {
        if (_isGameOver) return;
        _inDodgePhase = true;
        _hud.showBattleBoxOnly();
        _hud.showHeart();

        int phase = phaseIndex();
        if (phase == 0) {
            spawnSweepingLaser();
        } else {
            _dodgeTimerAction = getGameTimer().runAtInterval(
                    this::spawnAugePhase, Duration.seconds(_bosses[_currentBossIndex].phaseIntervals()[phase]));
        }
        getGameTimer().runOnceAfter(this::endDodgePhase,
                Duration.seconds(_bosses[_currentBossIndex].phaseDurations()[phase]));
    }

    private void spawnAugePhase() {
        if (!_inDodgePhase) return;
        switch (phaseIndex()) {
            case 1 -> spawnVerticalFlashLasers();
            case 2 -> spawnHorizontalFlashLasers();
            case 3 -> spawnCrossFlashLaser();
        }
    }

    private void spawnSweepingLaser() {
        _hud.addLaser(OvertaleHud.BATTLE_INNER_X, OvertaleHud.BATTLE_INNER_Y + 5,
                OvertaleHud.BATTLE_INNER_W, 14, 0, 40, true);
    }

    private void spawnVerticalFlashLasers() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        for (int i = 0; i < 3; i++) {
            double lx = ix + 14 + Math.random() * (iw - 28);
            Rectangle laser = _hud.addLaser(lx, iy, 14, ih, 0, 0, false);
            getGameTimer().runOnceAfter(() -> _hud.activateLaser(laser),   Duration.seconds(0.7));
            getGameTimer().runOnceAfter(() -> _hud.removeLaserNode(laser), Duration.seconds(1.4));
        }
    }

    private void spawnHorizontalFlashLasers() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        int count = 2 + (int)(Math.random() * 2);
        for (int i = 0; i < count; i++) {
            double ly = iy + 14 + Math.random() * (ih - 28);
            Rectangle laser = _hud.addLaser(ix, ly, iw, 14, 0, 0, false);
            getGameTimer().runOnceAfter(() -> _hud.activateLaser(laser),   Duration.seconds(0.7));
            getGameTimer().runOnceAfter(() -> _hud.removeLaserNode(laser), Duration.seconds(1.4));
        }
    }

    private void spawnCrossFlashLaser() {
        double ix = OvertaleHud.BATTLE_INNER_X, iy = OvertaleHud.BATTLE_INNER_Y;
        double iw = OvertaleHud.BATTLE_INNER_W, ih = OvertaleHud.BATTLE_INNER_H;
        double cx = ix + 30 + Math.random() * (iw - 60);
        double cy = iy + 30 + Math.random() * (ih - 60);
        Rectangle hL = _hud.addLaser(ix,     cy - 7, iw, 14, 0, 0, false);
        Rectangle vL = _hud.addLaser(cx - 7, iy,     14, ih, 0, 0, false);
        getGameTimer().runOnceAfter(() -> { _hud.activateLaser(hL); _hud.activateLaser(vL); },   Duration.seconds(0.6));
        getGameTimer().runOnceAfter(() -> { _hud.removeLaserNode(hL); _hud.removeLaserNode(vL); }, Duration.seconds(1.2));
    }

    // ── Battle menu ───────────────────────────────────────────────────────────

    private void handleBattleMenuConfirm() {
        switch (_hud.getSelectedButton()) {
            case 0 -> startDodgePhase();
            case 1 -> handleActButton();
            case 2 -> { _inventoryHud.show(); _hud.hideAll(); }
            case 3 -> _hud.hideAll();
        }
    }

    private void handleActButton() {
        if (_currentBossIndex < 0) return;
        _hud.hideAll();
        _dialogManager.startDialog(_bosses[_currentBossIndex].actDialog(), () -> _hud.showHUD());
    }

    // ── Item use ──────────────────────────────────────────────────────────────

    private void handleItemUse() {
        int  slot = _inventoryHud.getSelectedSlot();
        Item item = _inventory.getItem(slot);
        if (item == null) return;
        if (item.getHealAmount() > 0) { _currentHP = Math.min(_currentHP + item.getHealAmount(), _maxHP); _hud.updateHP(_currentHP, _maxHP); }
        if (item.getDamageAmount() > 0) _bonusDamage += item.getDamageAmount();
        String msg = _inventoryHud.useSelected();
        _inventoryHud.hide();
        _dialogManager.startDialog(List.of(msg), () -> _hud.showHUD());
    }

    // ── Chest ─────────────────────────────────────────────────────────────────

    private void handleOpenChest(Entity chest) {
        LootChestComponent lcc  = chest.getComponent(LootChestComponent.class);
        List<Item>         loot = lcc.open();

        javafx.scene.layout.StackPane sp = (javafx.scene.layout.StackPane)
                chest.getViewComponent().getChildren().get(0);
        ((javafx.scene.shape.Rectangle) sp.getChildren().get(0)).setFill(Color.web("#555555"));
        ((javafx.scene.shape.Rectangle) sp.getChildren().get(0)).setStroke(Color.GRAY);
        ((javafx.scene.text.Text) sp.getChildren().get(1)).setText("");

        if (loot.isEmpty()) { _dialogManager.startDialog(List.of("Die Truhe ist leer.")); return; }

        List<String> messages = new ArrayList<>();
        for (Item item : loot) {
            messages.add(_inventory.addItem(item)
                    ? "Du hast erhalten: " + item.getName()
                    : "Inventar voll! " + item.getName() + " passt nicht rein.");
        }
        _inventoryHud.refresh();
        _dialogManager.startDialog(messages);
    }

    // ── Death / game over ─────────────────────────────────────────────────────

    private void checkPlayerDead() {
        if (_currentHP > 0 || _isGameOver) return;
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
            if (!lcc.isOpened() && _player.distanceBBox(chest) < 60) return chest;
        }
        return null;
    }

    private Entity getNearbyBoss() {
        for (Entity boss : _bossEntities) {
            if (!boss.isActive()) continue;
            BossComponent bc = boss.getComponent(BossComponent.class);
            if (!bc.isDefeated() && _player.distanceBBox(boss) < 60) return boss;
        }
        return null;
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) { launch(args); }
}
