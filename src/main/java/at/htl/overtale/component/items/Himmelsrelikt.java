package at.htl.overtale.component.items;

public class Himmelsrelikt extends Item{
    public Himmelsrelikt() {
        super("Himmelsrelikt", "Uraltes Relikt aus längst vergangenen Zeiten, das dich um 5 heilt und das dir 4 mehr Angriff verleiht", 4, 5);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du benutzt das Himmelsrelikt und heilst dich um 5 und greifst um 4 stärker an.";
    }
}
