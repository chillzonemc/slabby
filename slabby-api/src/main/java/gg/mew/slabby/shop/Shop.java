package gg.mew.slabby.shop;

import gg.mew.slabby.audit.Auditable;

import java.util.Collection;
import java.util.UUID;

public interface Shop extends Auditable {

    class Names {
        public static final String ITEM = "item";
        public static final String X = "x";
        public static final String Y = "y";
        public static final String Z = "z";
        public static final String WORLD = "world";
        public static final String BUY_PRICE = "buyPrice";
        public static final String SELL_PRICE = "sellPrice";
        public static final String QUANTITY = "quantity";
        public static final String STOCK = "stock";
        public static final String NOTE = "note";
        public static final String NAME = "name";
        public static final String INVENTORY_X = "inventoryX";
        public static final String INVENTORY_Y = "inventoryY";
        public static final String INVENTORY_Z = "inventoryZ";
        public static final String INVENTORY_WORLD = "inventoryWorld";
    }

    String item();

    int x();

    int y();

    int z();

    String world();

    Double buyPrice();

    void buyPrice(final Double buyPrice);

    Double sellPrice();

    void sellPrice(final Double sellPrice);

    int quantity();

    void quantity(final int quantity);

    //TODO: allow null stock, null stock is infinite stock
    int stock();

    void stock(final int stock);

    String note();

    void note(final String note);

    String name();

    void name(final String name);

    Integer inventoryX();

    Integer inventoryY();

    Integer inventoryZ();

    String inventoryWorld();

    void inventory(final Integer x, final Integer y, final Integer z, final String world);

    default boolean hasInventory() {
        return inventoryX() != null && inventoryY() != null && inventoryZ() != null && inventoryWorld() != null;
    }

    Collection<ShopOwner> owners();

    Collection<ShopLog> logs();

    boolean isOwner(final UUID uniqueId);

    interface Builder {

         Builder item(final String item);
         Builder x(final int x);
         Builder y(final int y);
         Builder z(final int z);
         Builder world(final String dimension);
         Builder buyPrice(final Double buyPrice);
         Builder sellPrice(final Double sellPrice);
         Builder quantity(final int quantity);
         Builder stock(final int stock);
         Builder note(final String note);
         Builder name(final String name);
         Shop build();

     }

}