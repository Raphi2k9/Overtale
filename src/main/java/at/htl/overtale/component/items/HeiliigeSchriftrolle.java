package at.htl.overtale.component.items;

public class HeiliigeSchriftrolle extends Item{
    public HeiliigeSchriftrolle() {
        super("Heilige Schriftrolle", "Heilige Schriftrolle aus den alten Ruinen der Götter, diese heilt dich um 10 und verstärkt deinen Angriff um 7", 7, 10);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Du liest dir die heilige Schriftrolle durch und du heilst dich um 10 und ebenfalls wird dein Angriff um 7 verstärkt.";
    }
}
