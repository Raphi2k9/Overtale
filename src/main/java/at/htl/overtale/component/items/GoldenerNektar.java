package at.htl.overtale.component.items;

public class GoldenerNektar extends Item {
    public GoldenerNektar() {
        super("Goldener Nektar", "Getränk der Götter heilt dich um 20", 0, 20);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du benutzt den goldenen Nektar und heilst dich um 20 HP";
    }
}
