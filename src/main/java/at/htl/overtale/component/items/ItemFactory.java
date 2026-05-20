package at.htl.overtale.component.items;

public class ItemFactory {
    public static Item create(String name) {
        return switch (name) {
            case "Engelssegen"        -> new Engelssegen();
            case "HeiligesSchwert"    -> new HeiligesSchwert();
            case "HeiliigeSchriftrolle" -> new HeiliigeSchriftrolle();
            case "SpringerAmulett"    -> new SpringerAmulett();
            case "KosmischesAuge"     -> new KosmischesAuge();
            case "Engelstraene"       -> new Engelstraene();
            case "GoldenerNektar"     -> new GoldenerNektar();
            case "GöttlicherSpeer"    -> new GöttlicherSpeer();
            case "Himmelsrelikt"      -> new Himmelsrelikt();
            case "Sonnenessenz"       -> new Sonnenessenz();
            default -> throw new IllegalArgumentException("Unknown item: " + name);
        };
    }
}
