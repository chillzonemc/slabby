package gg.mew.slabby.shop;

import gg.mew.slabby.SlabbyAPI;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class BukkitShopOperations implements ShopOperations {

    private final Map<UUID, BukkitShopWizard> wizards = new HashMap<>();

    private final SlabbyAPI api;

    @Override
    public BukkitShopWizard wizardFor(final UUID uniqueId) {
        return wizards.computeIfAbsent(uniqueId, key -> new BukkitShopWizard(uniqueId, this.api));
    }

    @Override
    public boolean wizardExists(final UUID uniqueId) {
        return wizards.containsKey(uniqueId);
    }

    @Override
    public void destroyWizard(final UUID uniqueId) {
        wizards.remove(uniqueId);
    }

    @Override
    public Map<UUID, Double> splitCost(final double amount, final Shop shop) {
        final var result = new HashMap<UUID, Double>();

        for (final var shopOwner : shop.owners()) {
            result.put(shopOwner.uniqueId(), amount * (shopOwner.share() * 0.01));
        }

        return result;
    }

    //TODO: re-use withdraw/deposit for buy/sell.

    @Override
    public ShopOperationResult buy(final UUID uniqueId, final Shop shop) {
        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));

        if (shop.stock() < shop.quantity())
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_WITHDRAW);

        //TODO: check for space

        final var result = api.economy().withdraw(uniqueId, shop.buyPrice());

        if (!result.success())
            return new ShopOperationResult(false, Cause.INSUFFICIENT_BALANCE_WITHDRAW);

        //TODO: bug with cost, fixed with splitCost fix I think

        final var cost = splitCost(result.amount(), shop);

        for (final var entry : cost.entrySet()) {
            //TODO: verify success
            api.economy().deposit(entry.getKey(), entry.getValue());
        }

        shop.stock(shop.stock() - shop.quantity());

        api.repository().update(shop);

        addItemToInventory(shop, player);

        return new ShopOperationResult(true, Cause.NONE);
    }

    @Override
    public ShopOperationResult sell(final UUID uniqueId, final Shop shop) {
        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        if (!player.getInventory().containsAtLeast(itemStack, shop.quantity()))
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_DEPOSIT);

        final var cost = splitCost(shop.sellPrice(), shop);

        for (final var entry : cost.entrySet()) {
            if (!api.economy().hasAmount(entry.getKey(), entry.getValue()))
                return new ShopOperationResult(false, Cause.INSUFFICIENT_BALANCE_DEPOSIT);
        }

        for (final var entry : cost.entrySet()) {
            api.economy().withdraw(entry.getKey(), entry.getValue());
        }

        api.economy().deposit(uniqueId, shop.sellPrice());

        itemStack.setAmount(shop.quantity());

        player.getInventory().removeItem(itemStack);

        shop.stock(shop.stock() + shop.quantity());

        api.repository().update(shop);

        return new ShopOperationResult(true, Cause.NONE);
    }

    @Override
    public ShopOperationResult withdraw(final UUID uniqueId, final Shop shop, final int amount) {
        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));

        if (shop.stock() < amount)
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_WITHDRAW);

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
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_DEPOSIT);

        itemStack.setAmount(amount);

        player.getInventory().removeItem(itemStack);

        shop.stock(shop.stock() + amount);

        api.repository().update(shop);

        return new ShopOperationResult(true, Cause.NONE);
    }

    private static void addItemToInventory(final Shop shop, final Player player) {
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        itemStack.setAmount(shop.quantity());

        player.getInventory().addItem(itemStack);
    }

}
