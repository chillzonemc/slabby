package gg.mew.slabby.shop;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

public interface ShopRepository {

    <T> T builder(final Class<?> builderType);

    void createOrUpdate(final Shop shop) throws Exception;
    void createOrUpdate(final ShopOwner shopOwner) throws Exception;

    void delete(final Shop shop) throws Exception;
    void delete(final ShopOwner shopOwner) throws Exception;

    void update(final Shop shop) throws Exception;
    void update(final ShopOwner shopOwner) throws Exception;

    void refresh(final Shop shop) throws Exception;
    void refresh(final ShopOwner shopOwner) throws Exception;

    Optional<Shop> shopAt(final int x, final int y, final int z, final String world) throws Exception;

    Optional<Shop> shopWithInventoryAt(final int x, final int y, final int z, final String world) throws Exception;

    @SuppressWarnings("UnusedReturnValue")
    <T> T transaction(final Callable<T> transaction) throws Exception;

}
