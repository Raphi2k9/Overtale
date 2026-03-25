package at.htl.overtale.hud;

import at.htl.overtale.component.items.Inventory;
import at.htl.overtale.component.items.Item;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Zeigt das Inventar mit 8 Slots in einem 4×2 Raster an.
 *
 * Layout:
 *   [Slot 0]  [Slot 4]
 *   [Slot 1]  [Slot 5]
 *   [Slot 2]  [Slot 6]
 *   [Slot 3]  [Slot 7]
 *
 * Navigation:
 *   UP/DOWN  → Zeile (±1)
 *   LEFT/RIGHT → Spalte (±4)
 *   Z → Item benutzen
 *   X → Inventar schließen
 */
public class InventoryHud {

    private static final double SCREEN_W  = 800;
    private static final double INV_Y     = 435;   // Y-Start der Inventarbox
    private static final double BOX_H     = 155;   // Höhe der Inventarbox
    private static final double SLOT_W    = 330;
    private static final double SLOT_H    = 28;
    private static final double COL_GAP   = 370;   // Abstand zwischen den Spalten
    private static final double START_X   = 45;
    private static final double START_Y   = INV_Y + 8;
    private static final double ROW_GAP   = 6;

    private final Inventory _inventory;

    private Pane _inventoryPane;
    private Rectangle[] _slotBoxes = new Rectangle[Inventory.SIZE];
    private Text[] _slotTexts = new Text[Inventory.SIZE];
    private Text _descText;
    private Text _hintText;
    private Text _titleText;

    private int selectedSlot = 0;

    public InventoryHud(Inventory inventory) {
        _inventory = inventory;
    }

    public void build() {
        _inventoryPane = new Pane();

        // Hintergrund
        Rectangle bg = new Rectangle(SCREEN_W - 50, BOX_H, Color.BLACK);
        bg.setStroke(Color.WHITE);
        bg.setStrokeWidth(3);
        bg.setTranslateX(25);
        bg.setTranslateY(INV_Y);
        _inventoryPane.getChildren().add(bg);

        // Titel
        _titleText = makeText("ITEM", 16);
        _titleText.setTranslateX(35);
        _titleText.setTranslateY(INV_Y + 22);
        _titleText.setFill(Color.YELLOW);
        _inventoryPane.getChildren().add(_titleText);

        // 8 Slots: 4 Zeilen × 2 Spalten
        for (int i = 0; i < Inventory.SIZE; i++) {
            int row = i % 4;
            int col = i / 4;
            double x = START_X + col * COL_GAP;
            double y = START_Y + row * (SLOT_H + ROW_GAP);

            Rectangle box = new Rectangle(SLOT_W, SLOT_H, Color.BLACK);
            box.setTranslateX(x);
            box.setTranslateY(y);
            _slotBoxes[i] = box;
            _inventoryPane.getChildren().add(box);

            Text text = makeText("---", 14);
            text.setTranslateX(x + 6);
            text.setTranslateY(y + SLOT_H - 7);
            _slotTexts[i] = text;
            _inventoryPane.getChildren().add(text);
        }

        // Beschreibungstext (unten in der Box)
        _descText = makeText("", 13);
        _descText.setTranslateX(35);
        _descText.setTranslateY(INV_Y + BOX_H - 28);
        _inventoryPane.getChildren().add(_descText);

        // Steuerungs-Hinweis
        _hintText = makeText("Z: Benutzen   Q: Wegwerfen   X: Zurück", 11);
        _hintText.setFill(Color.GRAY);
        _hintText.setTranslateX(35);
        _hintText.setTranslateY(INV_Y + BOX_H - 10);
        _inventoryPane.getChildren().add(_hintText);

        FXGL.addUINode(_inventoryPane);
        _inventoryPane.setVisible(false);
    }

    /** Öffnet das Inventar und aktualisiert die Anzeige. */
    public void show() {
        refresh();
        highlightSlot(0);
        _inventoryPane.setVisible(true);
    }

    /** Schließt das Inventar. */
    public void hide() {
        _inventoryPane.setVisible(false);
    }

    public boolean isVisible() {
        return _inventoryPane != null && _inventoryPane.isVisible();
    }

    /**
     * Navigiert im Inventar.
     * @param delta  -1 = hoch, +1 = runter, -4 = links, +4 = rechts
     */
    public void navigate(int delta) {
        int next = selectedSlot + delta;
        if (next >= 0 && next < Inventory.SIZE) {
            highlightSlot(next);
        }
    }

    /**
     * Benutzt das aktuell markierte Item.
     * @return die Nachricht des Items, oder null wenn der Slot leer ist
     */
    public String useSelected() {
        Item item = _inventory.getItem(selectedSlot);
        if (item == null) return null;
        String msg = item.use(_inventory, selectedSlot);
        refresh();
        highlightSlot(Math.min(selectedSlot, Inventory.SIZE - 1));
        return msg;
    }

    /**
     * Wirft das aktuell markierte Item weg (ohne es zu benutzen).
     * @return Meldung, oder null wenn der Slot leer ist
     */
    public String dropSelected() {
        Item item = _inventory.getItem(selectedSlot);
        if (item == null) return null;
        _inventory.removeItem(selectedSlot);
        refresh();
        highlightSlot(Math.min(selectedSlot, Inventory.SIZE - 1));
        return item.getName() + " weggeworfen.";
    }

    public int getSelectedSlot() { return selectedSlot; }

    // --- private helpers ---

    private void highlightSlot(int index) {
        for (int i = 0; i < Inventory.SIZE; i++) {
            _slotBoxes[i].setFill(Color.BLACK);
            _slotTexts[i].setFill(Color.WHITE);
        }
        _slotBoxes[index].setFill(Color.web("#333300"));
        _slotTexts[index].setFill(Color.YELLOW);
        selectedSlot = index;
        updateDesc();
    }

    private void refresh() {
        for (int i = 0; i < Inventory.SIZE; i++) {
            Item item = _inventory.getItem(i);
            _slotTexts[i].setText(item != null ? item.getName() : "---");
        }
        updateDesc();
    }

    private void updateDesc() {
        Item item = _inventory.getItem(selectedSlot);
        _descText.setText(item != null ? item.getDescription() : "");
    }

    private Text makeText(String content, double size) {
        Text t = new Text(content);
        t.setFont(Font.font("Monospaced", size));
        t.setFill(Color.WHITE);
        return t;
    }
}
