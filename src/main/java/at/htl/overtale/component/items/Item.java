package at.htl.overtale.component.items;

public abstract class Item {

    private final String name;
    private final String description;

    public Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }

    /**
     * Benutzt das Item. Gibt eine Nachricht zurück, die im Dialog angezeigt wird.
     * @param inventory  das Inventar, aus dem das Item entfernt werden soll
     * @param slot       der Slot-Index des Items
     * @return Nachricht, die nach der Benutzung angezeigt wird
     */
    public abstract String use(Inventory inventory, int slot);

    /**
     * Wie viele HP dieses Item heilt (0 = kein Heileffekt).
     */
    public int getHealAmount() { return 0; }
}
