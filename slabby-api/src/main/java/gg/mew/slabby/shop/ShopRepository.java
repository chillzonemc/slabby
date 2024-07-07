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

    void markAsDeleted(final Shop shop) throws Exception;

    <T> Optional<Shop> shopById(final T id) throws Exception;

    Optional<Shop> shopAt(final int x, final int y, final int z, final String world) throws Exception;

    Optional<Shop> shopWithInventoryAt(final int x, final int y, final int z, final String world) throws Exception;

    Collection<Shop> shopsOf(final UUID uniqueId, final Shop.State state) throws Exception;

    @SuppressWarnings("UnusedReturnValue")
    <T> T transaction(final Callable<T> transaction) throws Exception;

}
