package gg.mew.slabby.shop;

import java.util.Optional;
import java.util.concurrent.Callable;

public interface ShopRepository {

    <T> T builder(final Class<?> builderType);

    void create(final Shop shop) throws Exception;
    void create(final ShopOwner shopOwner) throws Exception;

    void delete(final Shop shop) throws Exception;
    void delete(final ShopOwner shopOwner) throws Exception;

    void update(final Shop shop) throws Exception;
    void update(final ShopOwner shopOwner) throws Exception;

    void refresh(final Shop shop) throws Exception;
    void refresh(final ShopOwner shopOwner) throws Exception;

    Optional<Shop> shopAt(final int x, final int y, final int z, final String world) throws Exception;

    @SuppressWarnings("UnusedReturnValue")
    <T> T transaction(final Callable<T> transaction) throws Exception;

}
