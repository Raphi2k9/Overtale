package at.htl.overtale.component.items;

public class KosmischesAuge extends Item {
    public KosmischesAuge() {
        super("Kosmisches Auge", "Auge des besiegten Sehers. Verstärkt deinen nächsten Angriff um 6.", 6, 0);
    }

    @Override
    public String use(Inventory inventory, int slot) {
        inventory.removeItem(slot);
        return "Das Kosmische Auge leuchtet und verleiht dir 6 Bonus-Angriffschaden.";
    }
}
