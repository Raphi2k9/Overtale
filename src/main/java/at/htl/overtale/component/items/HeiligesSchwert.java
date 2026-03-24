package at.htl.overtale.component.items;

public class HeiligesSchwert extends Item{
    public HeiligesSchwert() {
        super("Heiliges Schwert", "Schwert eines Götterschmieds, erhöht deinen Angriff um 3.", 3, 0);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du rüstest das Heilige Schwert aus und es verleiht dir um 3 mehr Angriffschaden.";
    }
}
