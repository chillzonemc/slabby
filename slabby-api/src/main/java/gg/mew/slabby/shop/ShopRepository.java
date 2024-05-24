package gg.mew.slabby.shop;

import java.util.Collection;

//TODO: make it make sense
public interface ShopRepository {

    Shop.Builder shopBuilder();
    ShopOwner.Builder shopOwnerBuilder();

    void create(final Shop shop);

    void create(final Collection<Shop> shops);

    void refresh(final Shop shop);

}
