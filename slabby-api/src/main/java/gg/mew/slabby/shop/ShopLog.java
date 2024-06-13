package gg.mew.slabby.shop;

public interface ShopLog {

    Action action();
    String oldValue();
    String newValue();

    enum Action {
        LOCATION_CHANGED,
        BUY_PRICE_CHANGED,
        SELL_PRICE_CHANGED,
        QUANTITY_CHANGED,
        NOTE_CHANGED,
        LINKED_INVENTORY_CHANGED,

        OWNER_ADDED,
        OWNER_REMOVED,

        ITEM_SOLD,
        ITEM_BOUGHT,

        DEPOSIT,
        WITHDRAW,

        SHOP_CREATED,
        SHOP_DESTROYED,
    }

    interface Builder {

        Builder action(final Action action);
        Builder oldValue(final String oldValue);
        Builder newValue(final String newValue);
        ShopLog build();

    }

}
