package gg.mew.slabby.shop;

import gg.mew.slabby.SlabbyHelper;
import gg.mew.slabby.audit.Auditable;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

public interface ShopLog extends Auditable {

    Action action();
    String data();

    @Getter
    @Accessors(fluent = true, chain = false)
    enum Action {
        PROPERTY_CHANGED(ValueChanged.class),

        OWNER_ADDED(Void.class),
        OWNER_REMOVED(Void.class),

        BUY(Sale.class),
        SELL(Sale.class),

        DEPOSIT(Sale.class),
        WITHDRAW(Sale.class),

        SHOP_CREATED(Void.class),
        SHOP_DESTROYED(Void.class);

        private final Class<?> dataClass;

        Action(final Class<?> dataClass) {
            this.dataClass = dataClass;
        }
    }

    interface Builder {

        Builder action(final Action action);
        Builder data(final String data);
        ShopLog build();

        default Builder serialized(final Object any) {
            return this.data(SlabbyHelper.api().gson().toJson(any));
        }

    }

    //TODO: We can't use generics with serialization
    record ValueChanged<T>(String name, T from, T to) {}

    //TODO: Rename person -> player
    record Sale(UUID person, double price, int quantity) {}
}
