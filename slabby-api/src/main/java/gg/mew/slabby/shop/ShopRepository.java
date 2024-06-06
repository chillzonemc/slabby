package gg.mew.slabby.shop;

import java.util.Optional;
import java.util.concurrent.Callable;

public interface ShopRepository {

    <T> T builder(final Class<?> builderType);

    void create(final Shop shop);
    void create(final ShopOwner shopOwner);

    void delete(final Shop shop);
    void delete(final ShopOwner shopOwner);

    void update(final Shop shop);
    void update(final ShopOwner shopOwner);

    void refresh(final Shop shop);
    void refresh(final ShopOwner shopOwner);

    Optional<Shop> shopAt(final int x, final int y, final int z, final String world);

    <T> T transaction(final Callable<T> transaction);

}
