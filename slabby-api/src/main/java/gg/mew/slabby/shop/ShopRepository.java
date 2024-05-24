package gg.mew.slabby.shop;

public interface ShopRepository {

    <T> T builder(final Class<?> builderType);

    void create(final Shop shop);
    void create(final ShopOwner shopOwner);

}
