package at.htl.overtale.component.items;

public class Engelssegen extends Item {

    public Engelssegen() {
        super("Engelssegen", "Segen von einem Enger, deine Angriffe werden um 10 verstärkt", 10, 0);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du benutzt die Engelssegen und dein Angriff wurde um 10 gesteigert";
    }
}
