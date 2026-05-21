package at.htl.overtale.component.items;

public class Traumessenz extends Item {

    public Traumessenz() {
        super("Traumessenz", "Destillierter Traum — heilt 10 HP", 0, 10);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Die Traumessenz fließt durch dich — du heilst 10 HP.";
    }
}
