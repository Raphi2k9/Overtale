package at.htl.overtale.hud;

import at.htl.overtale.component.items.Inventory;
import at.htl.overtale.component.items.Item;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class InventoryHud {

    // ── Layout constants ──────────────────────────────────────────────────────

    private static final double PANEL_X = 20;
    private static final double PANEL_Y = 280;
    private static final double PANEL_W = 760;
    private static final double PANEL_H = 320;   // 38 + 4×44 + 64 + 42

    // Section heights
    private static final double TITLE_H = 38;
    private static final double ROW_H   = 44;
    private static final double DESC_H  = 64;
    // Remaining hint section: PANEL_H - TITLE_H - 4*ROW_H - DESC_H = 42 px

    // Section Y positions (absolute screen coords)
    private static final double SLOTS_Y = PANEL_Y + TITLE_H;           // 318
    private static final double DESC_Y  = SLOTS_Y + 4 * ROW_H;         // 494
    private static final double HINT_Y  = DESC_Y + DESC_H;             // 558

    // Two columns: 368 px each, 8 px gap, 8 px outer padding each side
    // 8 + 368 + 8 + 368 + 8 = 760 ✓
    private static final double COL0_X = PANEL_X + 8;                  // 28
    private static final double COL1_X = COL0_X + 368 + 8;             // 404
    private static final double COL_W  = 368;

    // Slot image/text insets inside a column
    private static final double ICON_INSET = 8;   // from column edge to image left
    private static final double ICON_SIZE  = 32;
    private static final double TEXT_INSET = ICON_INSET + ICON_SIZE + 8; // 48 px

    // Colours
    private static final Color GOLD_SEP = Color.color(0.8, 0.60, 0.00);
    private static final Color SEL_FILL = Color.color(0.30, 0.25, 0.00);
    private static final Color SEL_BDR  = Color.color(0.80, 0.65, 0.00, 0.90);
    private static final Color EMPTY_FG = Color.web("#555555");

    // ── State ─────────────────────────────────────────────────────────────────

    private final Inventory _inventory;

    private Pane        _inventoryPane;
    private Rectangle[] _slotBoxes      = new Rectangle[Inventory.SIZE];
    private Rectangle[] _slotPlaceholders = new Rectangle[Inventory.SIZE];
    private ImageView[] _slotIcons      = new ImageView[Inventory.SIZE];
    private Text[]      _slotTexts      = new Text[Inventory.SIZE];
    private Text        _descText;

    private int selectedSlot = 0;

    public InventoryHud(Inventory inventory) {
        _inventory = inventory;
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    public void build() {
        _inventoryPane = new Pane();

        // Panel background
        Rectangle bg = new Rectangle(PANEL_W, PANEL_H, Color.color(0.05, 0.05, 0.05, 0.97));
        bg.setTranslateX(PANEL_X);
        bg.setTranslateY(PANEL_Y);
        _inventoryPane.getChildren().add(bg);

        // Title "ITEM"
        Text title = new Text("ITEM");
        title.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        title.setFill(Color.YELLOW);
        title.setTranslateX(COL0_X + 4);
        title.setTranslateY(PANEL_Y + 26);
        _inventoryPane.getChildren().add(title);

        // Separator: title → slots
        addSeparator(SLOTS_Y);

        // 8 slots in 4×2 grid (row = i%4, col = i/4)
        for (int i = 0; i < Inventory.SIZE; i++) {
            int    row  = i % 4;
            int    col  = i / 4;
            double colX = col == 0 ? COL0_X : COL1_X;
            double rowY = SLOTS_Y + row * ROW_H;

            // Slot background (transparent by default, gold when selected)
            Rectangle box = new Rectangle(COL_W, ROW_H - 4, Color.TRANSPARENT);
            box.setStrokeWidth(0);
            box.setTranslateX(colX);
            box.setTranslateY(rowY + 2);
            _slotBoxes[i] = box;
            _inventoryPane.getChildren().add(box);

            // Placeholder shown when no icon is available
            Rectangle placeholder = new Rectangle(ICON_SIZE, ICON_SIZE, Color.color(0.15, 0.12, 0.05));
            placeholder.setStroke(Color.color(0.6, 0.5, 0.1));
            placeholder.setStrokeWidth(1);
            placeholder.setArcWidth(4);
            placeholder.setArcHeight(4);
            placeholder.setTranslateX(colX + ICON_INSET);
            placeholder.setTranslateY(rowY + 6);
            _slotPlaceholders[i] = placeholder;
            _inventoryPane.getChildren().add(placeholder);

            // Icon ImageView (hidden until an item with an icon is in this slot)
            ImageView iconView = new ImageView();
            iconView.setFitWidth(ICON_SIZE);
            iconView.setFitHeight(ICON_SIZE);
            iconView.setSmooth(false);
            iconView.setTranslateX(colX + ICON_INSET);
            iconView.setTranslateY(rowY + 6);
            iconView.setVisible(false);
            _slotIcons[i] = iconView;
            _inventoryPane.getChildren().add(iconView);

            // Item name (truncated to 18 chars)
            Text name = makeText("---", 14);
            name.setFill(EMPTY_FG);
            name.setTranslateX(colX + TEXT_INSET);
            name.setTranslateY(rowY + 6 + 22);   // baseline roughly at icon vertical centre
            _slotTexts[i] = name;
            _inventoryPane.getChildren().add(name);
        }

        // Separator: slots → description
        addSeparator(DESC_Y);

        // Description: word-wrapping Text inside a clipped Pane (fixed DESC_H height)
        _descText = new Text("");
        _descText.setFont(Font.font("Monospaced", 13));
        _descText.setFill(Color.web("#CCCCCC"));
        _descText.setWrappingWidth(PANEL_W - 40);   // auto word-wrap, never overflows horizontally
        _descText.setTranslateX(0);
        _descText.setTranslateY(14);                // baseline 14 px from pane top

        Pane descPane = new Pane(_descText);
        descPane.setTranslateX(COL0_X + 4);
        descPane.setTranslateY(DESC_Y + 4);
        // Clip to fixed height so description never pushes other elements
        descPane.setClip(new Rectangle(PANEL_W - 40, DESC_H - 8));
        _inventoryPane.getChildren().add(descPane);

        // Separator: description → hints
        addSeparator(HINT_Y);

        // Hint bar
        Text hint = makeText("Z: Benutzen    Q: Wegwerfen    X: Zurück", 12);
        hint.setFill(Color.GRAY);
        hint.setTranslateX(COL0_X + 4);
        hint.setTranslateY(HINT_Y + 22);
        _inventoryPane.getChildren().add(hint);

        FXGL.addUINode(_inventoryPane);
        _inventoryPane.setVisible(false);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void show() {
        refresh();
        highlightSlot(0);
        _inventoryPane.setVisible(true);
    }

    public void hide() {
        _inventoryPane.setVisible(false);
    }

    public boolean isVisible() {
        return _inventoryPane != null && _inventoryPane.isVisible();
    }

    /**
     * Navigates the selection cursor.
     * delta: -1 = up row, +1 = down row, -4 = left column, +4 = right column
     */
    public void navigate(int delta) {
        int next = selectedSlot + delta;
        if (next >= 0 && next < Inventory.SIZE) {
            highlightSlot(next);
        }
    }

    public String useSelected() {
        Item item = _inventory.getItem(selectedSlot);
        if (item == null) return null;
        String msg = item.use(_inventory, selectedSlot);
        refresh();
        highlightSlot(Math.min(selectedSlot, Inventory.SIZE - 1));
        return msg;
    }

    public String dropSelected() {
        Item item = _inventory.getItem(selectedSlot);
        if (item == null) return null;
        _inventory.removeItem(selectedSlot);
        refresh();
        highlightSlot(Math.min(selectedSlot, Inventory.SIZE - 1));
        return item.getName() + " weggeworfen.";
    }

    public int getSelectedSlot() { return selectedSlot; }

    public void refresh() {
        for (int i = 0; i < Inventory.SIZE; i++) {
            Item item = _inventory.getItem(i);
            if (item != null) {
                _slotTexts[i].setText(truncate(item.getName()));
                _slotTexts[i].setFill(i == selectedSlot ? Color.YELLOW : Color.WHITE);
                String iconName = item.getIconName();
                if (iconName != null) {
                    _slotIcons[i].setImage(FXGL.image("items/" + iconName));
                    _slotIcons[i].setVisible(true);
                    _slotPlaceholders[i].setVisible(false);
                } else {
                    _slotIcons[i].setVisible(false);
                    _slotPlaceholders[i].setVisible(true);
                }
            } else {
                _slotTexts[i].setText("---");
                _slotTexts[i].setFill(EMPTY_FG);
                _slotIcons[i].setVisible(false);
                _slotPlaceholders[i].setVisible(true);
            }
        }
        updateDesc();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void highlightSlot(int index) {
        for (int i = 0; i < Inventory.SIZE; i++) {
            _slotBoxes[i].setFill(Color.TRANSPARENT);
            _slotBoxes[i].setStroke(Color.TRANSPARENT);
            _slotBoxes[i].setStrokeWidth(0);
            _slotTexts[i].setFill(_inventory.getItem(i) != null ? Color.WHITE : EMPTY_FG);
        }
        _slotBoxes[index].setFill(SEL_FILL);
        _slotBoxes[index].setStroke(SEL_BDR);
        _slotBoxes[index].setStrokeWidth(1.5);
        _slotTexts[index].setFill(Color.YELLOW);
        selectedSlot = index;
        updateDesc();
    }

    private void updateDesc() {
        Item item = _inventory.getItem(selectedSlot);
        _descText.setText(item != null ? item.getDescription() : "");
    }

    private String truncate(String name) {
        return name.length() > 18 ? name.substring(0, 17) + "…" : name;
    }

    private void addSeparator(double y) {
        Rectangle sep = new Rectangle(PANEL_W - 16, 1, GOLD_SEP);
        sep.setTranslateX(PANEL_X + 8);
        sep.setTranslateY(y);
        _inventoryPane.getChildren().add(sep);
    }

    private Text makeText(String content, double size) {
        Text t = new Text(content);
        t.setFont(Font.font("Monospaced", size));
        t.setFill(Color.WHITE);
        return t;
    }
}
