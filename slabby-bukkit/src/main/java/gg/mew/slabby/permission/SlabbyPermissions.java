package gg.mew.slabby.permission;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class SlabbyPermissions {

    private final String BASE = "slabby";

    public final String SHOP_INTERACT = BASE + ".shop.interact";
    public final String SHOP_MODIFY = BASE + ".shop.modify";
    public final String SHOP_MODIFY_OTHERS = SHOP_MODIFY + ".others";
    public final String SHOP_IMPORT = BASE + ".shop.import";
    public final String SHOP_LINK = BASE + ".shop.link";
    public final String SHOP_NOTIFY = BASE + ".shop.notify";

}
