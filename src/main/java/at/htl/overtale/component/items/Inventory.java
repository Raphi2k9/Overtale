package at.htl.overtale.component.items;

public class Inventory {

    public static final int SIZE = 8;
    private final Item[] _slots = new Item[SIZE];

    /**
     * Fügt ein Item in den ersten freien Slot ein.
     * return true wenn erfolgreich, false wenn Inventar voll
     */
    public boolean addItem(Item item) {
        for (int i = 0; i < SIZE; i++) {
            if (_slots[i] == null) {
                _slots[i] = item;
                return true;
            }
        }
        return false;
    }

    public Item removeItem(int index) {
        Item item = _slots[index];
        _slots[index] = null;
        return item;
    }

    public Item getItem(int index) { return _slots[index]; }
    public int getSize() { return SIZE; }

    public boolean isFull() {
        for (Item slot : _slots) {
            if (slot == null) return false;
        }
        return true;
    }
}
