package gg.mew.slabby.shop;

import gg.mew.slabby.audit.Auditable;

public interface ShopLog extends Auditable {

    String oldValue();
    String newValue();
    // shop
    // type
    // oldValue?
    // newValue?
    // audit

    enum ShopAction {
        LOCATION_CHANGED,
        BUY_PRICE_CHANGED,
        //...
        // OWNER_ADDED/REMOVED, etc.
    }

}
