package at.htl.overtale.component;

import at.htl.overtale.component.items.Item;
import com.almasb.fxgl.entity.component.Component;

import java.util.ArrayList;
import java.util.List;

public class LootChestComponent extends Component {

    private final List<Item> _items = new ArrayList<>();
    private boolean _opened = false;

    public LootChestComponent(Item... items) {
        for (Item item : items) {
            _items.add(item);
        }
    }

    public boolean isOpened() { return _opened; }

    /** Gibt alle Items zurück und markiert die Truhe als geöffnet. */
    public List<Item> open() {
        _opened = true;
        List<Item> loot = new ArrayList<>(_items);
        _items.clear();
        return loot;
    }
}
