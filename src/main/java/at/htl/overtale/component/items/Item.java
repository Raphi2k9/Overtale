package at.htl.overtale.component.items;

public abstract class Item {

    private final String _name;
    private final String _description;

    public Item(String name, String description) {
        _name = name;
        _description = description;
    }

    public String getName() { return _name; }
    public String getDescription() { return _description; }

    public abstract String use(Inventory inventory, int slot);

    /**
     * Wie viele HP dieses Item heilt (0 = kein Heileffekt).
     */
    public int getHealAmount() { return 0; }
}
