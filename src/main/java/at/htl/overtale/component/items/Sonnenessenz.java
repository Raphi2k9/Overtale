package at.htl.overtale.component.items;

public class Sonnenessenz extends Item {

    public Sonnenessenz() {
        super("Sonnenessenz", "Frisch von der Sonne Herabgefallen, heilt 5 HP", 0, 5);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du benutzt die Sonnenessenz und heilst dich um 5 HP";
    }
}
