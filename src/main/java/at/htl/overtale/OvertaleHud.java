package at.htl.overtale;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class OvertaleHud {

    // --- Spieler Stats ---
    private String playerName = "FRISK";
    private int    level      = 1;
    private int    currentHP  = 20;
    private int    maxHP      = 20;

    // --- UI Nodes ---
    private Text    hpText;
    private Text    dialogText;
    private Rectangle hpBar;

    // Typewriter
    private String  fullDialog = "";
    private int     charIndex  = 0;
    private Timeline typewriterTimeline;

    // Buttons
    private static final String[] BUTTON_LABELS = {"FIGHT", "ACT", "ITEM", "MERCY"};
    private Rectangle[]           buttonBoxes   = new Rectangle[4];
    private Text[]                buttonTexts   = new Text[4];
    private int                   selectedBtn   = 0;

    // Layout
    private static final double SCREEN_W    = 800;
    private static final double SCREEN_H    = 600;
    private static final double HUD_Y       = 430;   // where the HUD panel starts
    private static final double BATTLE_SIZE = 150;   // battle box size


    public void build() {
        buildBattleBox();
        buildHUDPanel();
        buildButtons();
        buildDialogBox();
    }
    //  1. Battle Box (center, where the heart moves)
    private void buildBattleBox() {
        Rectangle border = new Rectangle(BATTLE_SIZE + 6, BATTLE_SIZE + 6);
        border.setFill(Color.BLACK);
        border.setStroke(Color.WHITE);
        border.setStrokeWidth(3);
        border.setTranslateX((SCREEN_W - BATTLE_SIZE) / 2 - 3);
        border.setTranslateY(60);
        FXGL.addUINode(border);
    }
    //  2. HUD Panel: Name / LV / HP
    private void buildHUDPanel() {
        // Background strip
        Rectangle hudBg = new Rectangle(SCREEN_W, 80, Color.BLACK);
        hudBg.setTranslateX(0);
        hudBg.setTranslateY(HUD_Y);
        FXGL.addUINode(hudBg);

        // Top border line
        Rectangle topLine = new Rectangle(SCREEN_W, 3, Color.WHITE);
        topLine.setTranslateX(0);
        topLine.setTranslateY(HUD_Y);
        FXGL.addUINode(topLine);

        // Player name
        Text nameText = makeText(playerName, 18);
        nameText.setTranslateX(30);
        nameText.setTranslateY(HUD_Y + 28);
        FXGL.addUINode(nameText);

        // LV
        Text lvLabel = makeText("LV  " + level, 18);
        lvLabel.setTranslateX(180);
        lvLabel.setTranslateY(HUD_Y + 28);
        FXGL.addUINode(lvLabel);

        // HP label
        Text hpLabel = makeText("HP", 16);
        hpLabel.setTranslateX(30);
        hpLabel.setTranslateY(HUD_Y + 58);
        FXGL.addUINode(hpLabel);

        // HP bar background (dark red)
        Rectangle hpBarBg = new Rectangle(120, 14, Color.web("#600000"));
        hpBarBg.setTranslateX(60);
        hpBarBg.setTranslateY(HUD_Y + 44);
        FXGL.addUINode(hpBarBg);

        // HP bar (yellow-green)
        hpBar = new Rectangle(120, 14, Color.web("#FFFF00"));
        hpBar.setTranslateX(60);
        hpBar.setTranslateY(HUD_Y + 44);
        FXGL.addUINode(hpBar);

        // HP numbers
        hpText = makeText(currentHP + " / " + maxHP, 16);
        hpText.setTranslateX(192);
        hpText.setTranslateY(HUD_Y + 58);
        FXGL.addUINode(hpText);
    }
    //  3. Buttons: FIGHT / ACT / ITEM / MERCY
    private void buildButtons() {
        double btnW    = 140;
        double btnH    = 38;
        double startX  = 30;
        double btnY    = HUD_Y + 90;
        double gap     = 10;

        for (int i = 0; i < 4; i++) {
            double x = startX + i * (btnW + gap);

            Rectangle box = new Rectangle(btnW, btnH, Color.BLACK);
            box.setStroke(Color.WHITE);
            box.setStrokeWidth(2);
            box.setTranslateX(x);
            box.setTranslateY(btnY);
            buttonBoxes[i] = box;
            FXGL.addUINode(box);

            Text label = makeText(BUTTON_LABELS[i], 18);
            label.setTranslateX(x + btnW / 2 - label.getLayoutBounds().getWidth() / 2 + 4);
            label.setTranslateY(btnY + 26);
            buttonTexts[i] = label;
            FXGL.addUINode(label);
        }

        highlightButton(0); // FIGHT selected by default
    }
    //  Dialog Box
    private void buildDialogBox() {
        Rectangle dialogBg = new Rectangle(SCREEN_W - 50, 70, Color.BLACK);
        dialogBg.setStroke(Color.WHITE);
        dialogBg.setStrokeWidth(3);
        dialogBg.setTranslateX(25);
        dialogBg.setTranslateY(HUD_Y + 140);
        FXGL.addUINode(dialogBg);

        dialogText = makeText("", 16);
        dialogText.setTranslateX(45);
        dialogText.setTranslateY(HUD_Y + 172);
        FXGL.addUINode(dialogText);
    }
    //  Public API
    // Dialog Box
    public void showDialog(String text) {
        fullDialog = "* " + text;
        charIndex  = 0;
        dialogText.setText("");

        if (typewriterTimeline != null) typewriterTimeline.stop();

        typewriterTimeline = new Timeline(
                new KeyFrame(Duration.millis(40), e -> {
                    if (charIndex < fullDialog.length()) {
                        dialogText.setText(fullDialog.substring(0, ++charIndex));
                    }
                })
        );
        typewriterTimeline.setCycleCount(fullDialog.length());
        typewriterTimeline.play();
    }
    // Update HP bar. Call when player takes damage/heals.
    public void updateHP(int current, int max) {
        currentHP = current;
        maxHP     = max;
        hpText.setText(current + " / " + max);
        double ratio = (double) current / max;
        hpBar.setWidth(120 * ratio);
        hpBar.setFill(ratio > 0.5 ? Color.web("#FFFF00") : Color.web("#FF6600"));
    }

    // Highlight selected battle button (0=FIGHT,1=ACT,2=ITEM,3=MERCY).
    public void highlightButton(int index) {
        for (int i = 0; i < 4; i++) {
            buttonBoxes[i].setFill(Color.BLACK);
            buttonTexts[i].setFill(Color.WHITE);
        }
        buttonBoxes[index].setFill(Color.web("#333300"));
        buttonTexts[index].setFill(Color.YELLOW);
        selectedBtn = index;
    }
    public int getSelectedButton() { return selectedBtn; }
    //  Helper
    private Text makeText(String content, double size) {
        Text t = new Text(content);
        t.setFont(Font.font("Monospaced", size));
        t.setFill(Color.WHITE);
        return t;
    }
}