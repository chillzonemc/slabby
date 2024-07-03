package gg.mew.slabby.shop;

import gg.mew.slabby.SlabbyHelper;
import gg.mew.slabby.audit.Auditable;
import gg.mew.slabby.shop.log.*;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

public interface ShopLog extends Auditable {

    UUID uniqueId();
    Action action();
    String data();

    @Getter
    @Accessors(fluent = true, chain = false)
    enum Action {
        //TODO: how does sqlite store enums? if it is by their ordinal, we're in trouble
        LOCATION_CHANGED(LocationChanged.class),
        BUY_PRICE_CHANGED(ValueChanged.Double.class),
        SELL_PRICE_CHANGED(ValueChanged.Double.class),
        QUANTITY_CHANGED(ValueChanged.Int.class),
        NOTE_CHANGED(ValueChanged.String.class),
        NAME_CHANGED(ValueChanged.String.class),

        LINKED_INVENTORY_CHANGED(LocationChanged.class),

        OWNER_ADDED(Void.class),
        OWNER_REMOVED(Void.class),

        BUY(Transaction.class),
        SELL(Transaction.class),

        DEPOSIT(Transaction.class),
        WITHDRAW(Transaction.class),

        SHOP_CREATED(Void.class),
        SHOP_DESTROYED(Void.class);

        private final Class<?> dataClass;

        Action(final Class<?> dataClass) {
            this.dataClass = dataClass;
        }
    }

    interface Builder {

        Builder action(final Action action);
        Builder uniqueId(final UUID uniqueId);
        Builder data(final String data);
        ShopLog build();

        default Builder serialized(final Object any) {
            return this.data(SlabbyHelper.api().gson().toJson(any));
        }

    }

}
