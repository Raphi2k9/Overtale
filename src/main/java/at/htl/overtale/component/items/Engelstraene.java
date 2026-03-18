package at.htl.overtale.component.items;

public class Engelstraene extends Item {
    public Engelstraene() {
        super("Engelsträne", "Träne eines Engels, heilt dich um 10 HP", 0, 10);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du benutzt die Engelsträne und heilst dich um 10 HP";
    }
}
