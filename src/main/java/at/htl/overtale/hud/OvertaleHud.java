package at.htl.overtale.hud;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class OvertaleHud {

    // --- Spieler Stats ---
    private String _playerName = "FRISK";
    private int _level = 1;
    private int _currentHP = 20;
    private int _maxHP = 20;

    // --- UI Nodes ---
    private Text _hpText;
    private Text _enemyHpText;
    private Text _dialogText;
    private Rectangle _hpBar;
    private Rectangle _enemyHpBar;
    private Rectangle _heart;

    // Panes
    private Pane _hudPane;
    private Pane _battleMenuPane;  // Buttons + HP-Panel (nur im Menü sichtbar)
    private Pane _dialogPane;

    // Typewriter
    private String _fullDialog = "";
    private int _charIndex = 0;
    private Timeline _typewriterTimeline;

    // Buttons
    private static final String[] _BUTTON_LABELS = {"FIGHT", "ACT", "ITEM", "MERCY"};
    private Rectangle[] _buttonBoxes = new Rectangle[4];
    private Text[] _buttonTexts = new Text[4];
    private int _selectedBtn = 0;

    // Layout
    private static final double SCREEN_W    = 800;
    private static final double SCREEN_H    = 600;
    private static final double HUD_Y       = 430;
    private static final double BATTLE_SIZE = 150;
    private static final double DIALOG_Y    = 480;

    // Kampfkasten-Innenfläche (public für GameApp)
    public static final double BATTLE_INNER_X = (SCREEN_W - BATTLE_SIZE) / 2;  // 325
    public static final double BATTLE_INNER_Y = 63;
    public static final double BATTLE_INNER_W = BATTLE_SIZE;
    public static final double BATTLE_INNER_H = BATTLE_SIZE;
    public static final int    HEART_SIZE      = 10;

    // Dodge-Kugeln (UI-Ebene, damit sie über dem schwarzen Hintergrund sichtbar sind)
    private final List<Rectangle> _dodgeBullets    = new ArrayList<>();
    private final List<double[]>  _dodgeBulletVels = new ArrayList<>();

    public void build() {
        _hudPane        = new Pane();
        _battleMenuPane = new Pane();
        _dialogPane     = new Pane();

        buildBattleBox();   // → _hudPane
        buildHUDPanel();    // → _battleMenuPane
        buildButtons();     // → _battleMenuPane
        buildHeart();       // → _hudPane (letztes Kind = oberste Ebene)
        buildDialogBox();   // → _dialogPane

        _hudPane.getChildren().add(1, _battleMenuPane); // hinter dem Kampfkasten-Rand

        FXGL.addUINode(_hudPane);
        FXGL.addUINode(_dialogPane);

        _hudPane.setVisible(false);
        _dialogPane.setVisible(false);
    }

    private void buildBattleBox() {
        Rectangle border = new Rectangle(BATTLE_SIZE + 6, BATTLE_SIZE + 6);
        border.setFill(Color.BLACK);
        border.setStroke(Color.WHITE);
        border.setStrokeWidth(3);
        border.setTranslateX(BATTLE_INNER_X - 3);
        border.setTranslateY(60);
        _hudPane.getChildren().add(border);
    }

    private void buildHUDPanel() {
        Rectangle hudBg = new Rectangle(SCREEN_W, 80, Color.BLACK);
        hudBg.setTranslateY(HUD_Y);
        _battleMenuPane.getChildren().add(hudBg);

        Rectangle topLine = new Rectangle(SCREEN_W, 3, Color.WHITE);
        topLine.setTranslateY(HUD_Y);
        _battleMenuPane.getChildren().add(topLine);

        Text nameText = makeText(_playerName, 18);
        nameText.setTranslateX(30);
        nameText.setTranslateY(HUD_Y + 28);
        _battleMenuPane.getChildren().add(nameText);

        Text lvLabel = makeText("LV  " + _level, 18);
        lvLabel.setTranslateX(180);
        lvLabel.setTranslateY(HUD_Y + 28);
        _battleMenuPane.getChildren().add(lvLabel);

        Text hpLabel = makeText("HP", 16);
        hpLabel.setTranslateX(30);
        hpLabel.setTranslateY(HUD_Y + 58);
        _battleMenuPane.getChildren().add(hpLabel);

        Rectangle hpBarBg = new Rectangle(120, 14, Color.web("#600000"));
        hpBarBg.setTranslateX(60);
        hpBarBg.setTranslateY(HUD_Y + 44);
        _battleMenuPane.getChildren().add(hpBarBg);

        _hpBar = new Rectangle(120, 14, Color.web("#FFFF00"));
        _hpBar.setTranslateX(60);
        _hpBar.setTranslateY(HUD_Y + 44);
        _battleMenuPane.getChildren().add(_hpBar);

        _hpText = makeText(_currentHP + " / " + _maxHP, 16);
        _hpText.setTranslateX(192);
        _hpText.setTranslateY(HUD_Y + 58);
        _battleMenuPane.getChildren().add(_hpText);

        // Gegner-HP (rechte Seite)
        Text enemyLabel = makeText("ENEMY", 18);
        enemyLabel.setTranslateX(470);
        enemyLabel.setTranslateY(HUD_Y + 28);
        _battleMenuPane.getChildren().add(enemyLabel);

        Text enemyHpLabel = makeText("HP", 16);
        enemyHpLabel.setTranslateX(470);
        enemyHpLabel.setTranslateY(HUD_Y + 58);
        _battleMenuPane.getChildren().add(enemyHpLabel);

        Rectangle enemyHpBarBg = new Rectangle(120, 14, Color.web("#600000"));
        enemyHpBarBg.setTranslateX(500);
        enemyHpBarBg.setTranslateY(HUD_Y + 44);
        _battleMenuPane.getChildren().add(enemyHpBarBg);

        _enemyHpBar = new Rectangle(120, 14, Color.web("#FF4400"));
        _enemyHpBar.setTranslateX(500);
        _enemyHpBar.setTranslateY(HUD_Y + 44);
        _battleMenuPane.getChildren().add(_enemyHpBar);

        _enemyHpText = makeText("-- / --", 16);
        _enemyHpText.setTranslateX(632);
        _enemyHpText.setTranslateY(HUD_Y + 58);
        _battleMenuPane.getChildren().add(_enemyHpText);
    }

    private void buildButtons() {
        double btnW   = 140;
        double btnH   = 38;
        double startX = 30;
        double btnY   = HUD_Y + 90;
        double gap    = 10;

        for (int i = 0; i < 4; i++) {
            double x = startX + i * (btnW + gap);

            Rectangle box = new Rectangle(btnW, btnH, Color.BLACK);
            box.setStroke(Color.WHITE);
            box.setStrokeWidth(2);
            box.setTranslateX(x);
            box.setTranslateY(btnY);
            _buttonBoxes[i] = box;
            _battleMenuPane.getChildren().add(box);

            Text label = makeText(_BUTTON_LABELS[i], 18);
            label.setTranslateX(x + btnW / 2 - label.getLayoutBounds().getWidth() / 2 + 4);
            label.setTranslateY(btnY + 26);
            _buttonTexts[i] = label;
            _battleMenuPane.getChildren().add(label);
        }

        highlightButton(0);
    }

    private void buildDialogBox() {
        Rectangle dialogBg = new Rectangle(SCREEN_W - 50, 90, Color.BLACK);
        dialogBg.setStroke(Color.WHITE);
        dialogBg.setStrokeWidth(3);
        dialogBg.setTranslateX(25);
        dialogBg.setTranslateY(DIALOG_Y);
        _dialogPane.getChildren().add(dialogBg);

        _dialogText = makeText("", 16);
        _dialogText.setTranslateX(45);
        _dialogText.setTranslateY(DIALOG_Y + 35);
        _dialogPane.getChildren().add(_dialogText);
    }

    private void buildHeart() {
        _heart = new Rectangle(HEART_SIZE, HEART_SIZE, Color.RED);
        _heart.setVisible(false);
        _hudPane.getChildren().add(_heart); // letztes Kind → immer oben
    }

    // --- Sichtbarkeit ---

    public void showHUD() {
        _hudPane.setVisible(true);
        _battleMenuPane.setVisible(true);
        _dialogPane.setVisible(false);
    }

    /** Zeigt nur den Kampfkasten (ohne Buttons/HP-Panel) für die Ausweichphase. */
    public void showBattleBoxOnly() {
        _hudPane.setVisible(true);
        _battleMenuPane.setVisible(false);
        _dialogPane.setVisible(false);
    }

    public void showDialogOnly() {
        _hudPane.setVisible(false);
        _dialogPane.setVisible(true);
    }

    public void hideAll() {
        _hudPane.setVisible(false);
        _dialogPane.setVisible(false);
    }

    public boolean isHUDVisible() {
        return _hudPane != null && _hudPane.isVisible();
    }

    /** True nur wenn das Kampfmenü (Buttons) sichtbar ist – nicht während Ausweichphase. */
    public boolean isBattleMenuVisible() {
        return isHUDVisible() && _battleMenuPane != null && _battleMenuPane.isVisible();
    }

    // --- Dialog API ---

    public void showDialog(String text) {
        showDialog(text, null);
    }

    public void showDialog(String text, Runnable onDone) {
        _fullDialog = "* " + text;
        _charIndex = 0;
        _dialogText.setText("");

        if (_typewriterTimeline != null) _typewriterTimeline.stop();

        _typewriterTimeline = new Timeline(
                new KeyFrame(Duration.millis(40), e -> {
                    if (_charIndex < _fullDialog.length()) {
                        _dialogText.setText(_fullDialog.substring(0, ++_charIndex));
                    }
                })
        );
        _typewriterTimeline.setCycleCount(_fullDialog.length());
        if (onDone != null) {
            _typewriterTimeline.setOnFinished(e -> onDone.run());
        }
        _typewriterTimeline.play();
    }

    public boolean isTypewriterDone() {
        return _charIndex >= _fullDialog.length();
    }

    public void skipTypewriter() {
        if (_typewriterTimeline != null) _typewriterTimeline.stop();
        _dialogText.setText(_fullDialog);
        _charIndex = _fullDialog.length();
    }

    public void hideDialog() {
        if (_typewriterTimeline != null) _typewriterTimeline.stop();
        _dialogText.setText("");
        _fullDialog = "";
        _charIndex = 0;
        _dialogPane.setVisible(false);
    }

    // --- HUD API ---

    public void updateHP(int current, int max) {
        _currentHP = current;
        _maxHP = max;
        _hpText.setText(current + " / " + max);
        double ratio = (double) current / max;
        _hpBar.setWidth(120 * ratio);
        _hpBar.setFill(ratio > 0.5 ? Color.web("#FFFF00") : Color.web("#FF6600"));
    }

    public void updateEnemyHP(int current, int max) {
        _enemyHpText.setText(current + " / " + max);
        double ratio = (double) current / max;
        _enemyHpBar.setWidth(120 * ratio);
    }

    public void highlightButton(int index) {
        for (int i = 0; i < 4; i++) {
            _buttonBoxes[i].setFill(Color.BLACK);
            _buttonTexts[i].setFill(Color.WHITE);
        }
        _buttonBoxes[index].setFill(Color.web("#333300"));
        _buttonTexts[index].setFill(Color.YELLOW);
        _selectedBtn = index;
    }

    public int getSelectedButton() { return _selectedBtn; }

    // --- Herz-API (Ausweichphase) ---

    public void showHeart() {
        _heart.setTranslateX(BATTLE_INNER_X + BATTLE_INNER_W / 2.0 - HEART_SIZE / 2.0);
        _heart.setTranslateY(BATTLE_INNER_Y + BATTLE_INNER_H / 2.0 - HEART_SIZE / 2.0);
        _heart.setVisible(true);
    }

    public void hideHeart() {
        _heart.setVisible(false);
    }

    public void moveHeart(double dx, double dy) {
        double nx = Math.max(BATTLE_INNER_X,
                Math.min(_heart.getTranslateX() + dx, BATTLE_INNER_X + BATTLE_INNER_W - HEART_SIZE));
        double ny = Math.max(BATTLE_INNER_Y,
                Math.min(_heart.getTranslateY() + dy, BATTLE_INNER_Y + BATTLE_INNER_H - HEART_SIZE));
        _heart.setTranslateX(nx);
        _heart.setTranslateY(ny);
    }

    public double getHeartX() { return _heart.getTranslateX(); }
    public double getHeartY() { return _heart.getTranslateY(); }

    // --- Dodge-Kugel-API ---

    public void addDodgeBullet(double x, double y, double vx, double vy) {
        Rectangle bullet = new Rectangle(8, 8, Color.WHITE);
        bullet.setTranslateX(x);
        bullet.setTranslateY(y);
        // Vor dem Herz einfügen, damit das Herz immer oben liegt
        int heartIdx = _hudPane.getChildren().indexOf(_heart);
        _hudPane.getChildren().add(heartIdx, bullet);
        _dodgeBullets.add(bullet);
        _dodgeBulletVels.add(new double[]{vx, vy});
    }

    public void updateDodgeBullets(double tpf) {
        List<Rectangle> toRemove = new ArrayList<>();
        for (int i = 0; i < _dodgeBullets.size(); i++) {
            Rectangle b = _dodgeBullets.get(i);
            double[] v = _dodgeBulletVels.get(i);
            b.setTranslateX(b.getTranslateX() + v[0] * tpf);
            b.setTranslateY(b.getTranslateY() + v[1] * tpf);
            double bx = b.getTranslateX(), by = b.getTranslateY();
            if (bx < BATTLE_INNER_X - 15 || bx > BATTLE_INNER_X + BATTLE_INNER_W + 15 ||
                by < BATTLE_INNER_Y - 15 || by > BATTLE_INNER_Y + BATTLE_INNER_H + 15) {
                toRemove.add(b);
            }
        }
        for (Rectangle r : toRemove) removeDodgeBullet(r);
    }

    /** Prüft Kollision Herz ↔ Kugeln, entfernt getroffene Kugel. @return true wenn getroffen. */
    public boolean checkAndRemoveCollidingBullet() {
        double hx = _heart.getTranslateX(), hy = _heart.getTranslateY();
        for (Rectangle b : _dodgeBullets) {
            double bx = b.getTranslateX(), by = b.getTranslateY();
            if (bx < hx + HEART_SIZE && bx + 8 > hx && by < hy + HEART_SIZE && by + 8 > hy) {
                removeDodgeBullet(b);
                return true;
            }
        }
        return false;
    }

    public void removeDodgeBullet(Rectangle bullet) {
        int idx = _dodgeBullets.indexOf(bullet);
        if (idx >= 0) {
            _hudPane.getChildren().remove(bullet);
            _dodgeBullets.remove(idx);
            _dodgeBulletVels.remove(idx);
        }
    }

    public void clearDodgeBullets() {
        _hudPane.getChildren().removeAll(_dodgeBullets);
        _dodgeBullets.clear();
        _dodgeBulletVels.clear();
    }

    private Text makeText(String content, double size) {
        Text t = new Text(content);
        t.setFont(Font.font("Monospaced", size));
        t.setFill(Color.WHITE);
        return t;
    }
}
