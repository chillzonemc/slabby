package gg.mew.slabby.shop;

import gg.mew.slabby.audit.Auditable;

import java.util.UUID;

public interface ShopOwner extends Auditable {

    UUID uniqueId();

    int share();

    interface Builder {

        Builder uniqueId(final UUID uniqueId);
        Builder share(final int share);
        ShopOwner build();

    }

}
