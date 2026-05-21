package at.htl.overtale.component.items;

public class Leereklinge extends Item {

    public Leereklinge() {
        super("Leereklinge", "Eine Klinge aus dem Nichts — 12 Bonusschaden beim nächsten Angriff", 12, 0);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du schwingt die Leereklinge — dein nächster Angriff trifft um 12 härter!";
    }
}
