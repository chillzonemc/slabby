package gg.mew.slabby.importer;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.shop.Shop;
import gg.mew.slabby.shop.ShopOwner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public final class SlabboImporter implements Importer {

    @Override
    public void onImport(final SlabbyAPI api) {
        final var slabbo = Bukkit.getPluginManager().getPlugin("Slabbo");

        //TODO: Allow some kind of result
        if (slabbo == null)
            return;

        final var config = YamlConfiguration.loadConfiguration(new File(slabbo.getDataFolder(), "shops.yml"));
        final var section = Objects.requireNonNull(config.getConfigurationSection("shops"));

        final var shops = section.getValues(false);

        for (final var oldShop : shops.values()) {
            final var shopClass = oldShop.getClass();

            try {
                final var buyPrice = shopClass.getDeclaredField("buyPrice").getDouble(oldShop);
                final var sellPrice = shopClass.getDeclaredField("sellPrice").getDouble(oldShop);
                final var location = (Location) shopClass.getDeclaredField("location").get(oldShop);
                final var item = (ItemStack) shopClass.getDeclaredField("item").get(oldShop);
                final var stock = shopClass.getDeclaredField("stock").getInt(oldShop);
                final var quantity = shopClass.getDeclaredField("quantity").getInt(oldShop);
                final var note = (String) shopClass.getDeclaredField("note").get(oldShop);
                final var name = (String) shopClass.getDeclaredField("shopName").get(oldShop);

                final var ownerId = (UUID) shopClass.getDeclaredField("ownerId").get(oldShop);

                final var shop = api.repository()
                        .<Shop.Builder>builder(Shop.Builder.class)
                        .item(item.getType().getKey().asString() + item.getItemMeta().getAsString())
                        .x(location.getBlockX())
                        .y(location.getBlockY())
                        .z(location.getBlockZ())
                        .world(location.getWorld().getName())
                        .buyPrice(buyPrice)
                        .sellPrice(sellPrice)
                        .quantity(quantity)
                        .stock(stock)
                        .note(note)
                        .name(name)
                        .build();

                api.repository().create(shop);

                final var owner = api.repository()
                        .<ShopOwner.Builder>builder(ShopOwner.Builder.class)
                        .uniqueId(ownerId)
                        .share(100)
                        .build();

                shop.owners().add(owner);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                api.exceptionService().log(e);
                throw new RuntimeException(e);
            }
        }
    }

}