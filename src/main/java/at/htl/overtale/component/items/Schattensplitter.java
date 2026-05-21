package at.htl.overtale.component.items;

public class Schattensplitter extends Item {

    public Schattensplitter() {
        super("Schattensplitter", "Ein Splitter der Dunkelheit — gibt dir 8 Angriffsbonus", 8, 0);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du setzt den Schattensplitter ein — dein Angriff steigt um 8.";
    }
}
