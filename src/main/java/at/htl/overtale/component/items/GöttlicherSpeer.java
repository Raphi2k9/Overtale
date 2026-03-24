package at.htl.overtale.component.items;

public class GöttlicherSpeer extends Item{
    public GöttlicherSpeer() {
        super("GöttlicherSpeer", "Waffe der Götter, gibt dir 10 Angriffsbonus", 10, 0);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du rüstest den göttlichen Speer aus und er verleiht dir um 3 mehr Angriffschaden.";
    }
}
