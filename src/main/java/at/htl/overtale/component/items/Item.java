package at.htl.overtale.component.items;

public abstract class Item {

    private final String _name;
    private final String _description;
    private int _healAmount = 0;
    private int _damageAmount = 0;

    public Item(String name, String description, int damageAmount, int healAmount) {
        _name = name;
        _description = description;
        _damageAmount = damageAmount;
        _healAmount = healAmount;
    }

    public String getName() { return _name; }
    public String getDescription() { return _description; }

    public abstract String use(Inventory inventory, int slot);

    public int getHealAmount(){return _healAmount;};
    public int getDamageAmount(){return _damageAmount;};
}
