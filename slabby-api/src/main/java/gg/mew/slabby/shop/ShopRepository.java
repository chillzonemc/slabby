package gg.mew.slabby.shop;

import java.util.Optional;

public interface ShopRepository {

    <T> T builder(final Class<?> builderType);

    void create(final Shop shop);
    void create(final ShopOwner shopOwner);

    void update(final Shop shop);
    void update(final ShopOwner shopOwner);

    Optional<Shop> shopAt(final int x, final int y, final int z, final String world);

}
