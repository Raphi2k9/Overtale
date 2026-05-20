package at.htl.overtale.component.items;

public class SpringerAmulett extends Item {
    public SpringerAmulett() {
        super("Springer-Amulett", "Amulett des Springers. Heilt 8 HP.", 0, 8);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Das Springer-Amulett strahlt Wärme aus und heilt 8 HP.";
    }
}
