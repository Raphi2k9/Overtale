package at.htl.overtale.component.items;

public class Engelssegen extends Item {

    private final int healAmount;

    public Engelssegen() {
        super("Engelssegen", "Heilt 10 HP. Ein sanftes göttliches Leuchten.");
        this.healAmount = 10;
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du benutzt den Engelssegen und heilest " + healAmount + " HP!";
    }

    public int getHealAmount() { return healAmount; }
}
