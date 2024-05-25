package gg.mew.slabby.shop;

import gg.mew.slabby.SlabbyAPI;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class BukkitShopOperations implements ShopOperations {

    private final SlabbyAPI api;

    @Override
    public ShopOperationResult buy(final UUID uniqueId, final Shop shop) {
        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));

        if (shop.stock() < shop.quantity())
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_SELLER);

        //TODO: check for space

        final var result = api.economy().withdraw(uniqueId, shop.buyPrice());

        if (!result.success())
            return new ShopOperationResult(false, Cause.INSUFFICIENT_BALANCE_BUYER);

        shop.stock(shop.stock() - shop.quantity());

        api.repository().update(shop);

        addItemToInventory(shop, player);

        return new ShopOperationResult(true, Cause.NONE);
    }

    private static void addItemToInventory(final Shop shop, final Player player) {
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        itemStack.setAmount(shop.quantity());

        player.getInventory().addItem(itemStack);
    }

    @Override
    public ShopOperationResult sell(final UUID uniqueId, final Shop shop) {
        return null;
    }

    @Override
    public ShopOperationResult withdraw(final UUID uniqueId, final Shop shop, final int amount) {
        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));

        if (shop.stock() < amount)
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_SELLER);

        //TODO: check for space

        shop.stock(shop.stock() - amount);

        api.repository().update(shop);

        addItemToInventory(shop, player);

        return new ShopOperationResult(true, Cause.NONE);
    }

    @Override
    public ShopOperationResult deposit(final UUID uniqueId, final Shop shop, final int amount) {
        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        if (!player.getInventory().containsAtLeast(itemStack, amount))
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_BUYER);

        itemStack.setAmount(amount);

        player.getInventory().remove(itemStack);

        shop.stock(shop.stock() + amount);

        api.repository().update(shop);

        return new ShopOperationResult(true, Cause.NONE);
    }

}
