package gg.mew.slabby.shop;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.permission.SlabbyPermissions;
import gg.mew.slabby.shop.log.IntValueChanged;
import gg.mew.slabby.shop.log.Transaction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Accessors(fluent = true, chain = false)
public final class BukkitShopOperations implements ShopOperations {

    @Getter
    private final Map<UUID, ShopWizard> wizards = new HashMap<>();

    private final SlabbyAPI api;

    @Override
    public ShopWizard wizard(final UUID uniqueId) {
        return this.wizards.computeIfAbsent(uniqueId, u -> new BukkitShopWizard(api));
    }

    @Override
    public ShopWizard wizardFrom(final UUID uniqueId, final Shop shop) {
        return this.wizards.computeIfAbsent(uniqueId, u -> new BukkitShopWizard(api, shop));
    }

    @Override
    public void ifWizard(final UUID uniqueId, final Consumer<ShopWizard> action) {
        final var wizard = this.wizards.get(uniqueId);

        if (wizard != null)
            action.accept(wizard);
    }

    @Override
    public Map<UUID, Double> splitCost(final double amount, final Shop shop) {
        final var result = new HashMap<UUID, Double>();

        for (final var shopOwner : shop.owners()) {
            result.put(shopOwner.uniqueId(), amount * (shopOwner.share() * 0.01));
        }

        return result;
    }

    @Override
    public ShopOperationResult buy(final UUID uniqueId, final Shop shop) {
        if (!api.permission().hasPermission(uniqueId, SlabbyPermissions.SHOP_INTERACT))
            return new ShopOperationResult(false, Cause.OPERATION_NO_PERMISSION);

        try {
            api.repository().refresh(shop);
        } catch (final Exception e) {
            return new ShopOperationResult(false, Cause.OPERATION_FAILED);
        }

        if (shop.buyPrice() == null)
            return new ShopOperationResult(false, Cause.OPERATION_NOT_ALLOWED);

        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));

        if (shop.stock() < shop.quantity())
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_TO_WITHDRAW);

        //TODO: check for space

        final var result = api.economy().withdraw(uniqueId, shop.buyPrice());

        if (!result.success())
            return new ShopOperationResult(false, Cause.INSUFFICIENT_BALANCE_TO_WITHDRAW);

        final var stock = shop.stock();

        shop.stock(stock - shop.quantity());

        try {
            api.repository().update(shop);

            final var log = api.repository()
                    .<ShopLog.Builder>builder(ShopLog.Builder.class)
                    .action(ShopLog.Action.BUY)
                    .uniqueId(uniqueId)
                    .serialized(new Transaction(shop.buyPrice(), shop.quantity()))
                    .build();

            shop.logs().add(log);
        } catch (final Exception e) {
            return new ShopOperationResult(false, Cause.OPERATION_FAILED);
        }

        final var cost = splitCost(result.amount(), shop);

        for (final var entry : cost.entrySet()) {
            //TODO: verify success?
            api.economy().deposit(entry.getKey(), entry.getValue());
        }

        addItemToInventory(shop, player);

        return new ShopOperationResult(true, Cause.NONE);
    }

    @Override
    public ShopOperationResult sell(final UUID uniqueId, final Shop shop) {
        if (!api.permission().hasPermission(uniqueId, SlabbyPermissions.SHOP_INTERACT))
            return new ShopOperationResult(false, Cause.OPERATION_NO_PERMISSION);

        try {
            api.repository().refresh(shop);
        } catch (final Exception e) {
            return new ShopOperationResult(false, Cause.OPERATION_FAILED);
        }

        if (shop.sellPrice() == null)
            return new ShopOperationResult(false, Cause.OPERATION_NOT_ALLOWED);

        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        if (!player.getInventory().containsAtLeast(itemStack, shop.quantity()))
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_TO_DEPOSIT);

        final var cost = splitCost(shop.sellPrice(), shop);

        for (final var entry : cost.entrySet()) {
            if (!api.economy().hasAmount(entry.getKey(), entry.getValue()))
                return new ShopOperationResult(false, Cause.INSUFFICIENT_BALANCE_TO_DEPOSIT);
        }

        final var stock = shop.stock();

        shop.stock(stock + shop.quantity());

        try {
            api.repository().update(shop);

            final var log = api.repository()
                    .<ShopLog.Builder>builder(ShopLog.Builder.class)
                    .action(ShopLog.Action.SELL)
                    .uniqueId(uniqueId)
                    .serialized(new Transaction(shop.sellPrice(), shop.quantity()))
                    .build();

            shop.logs().add(log);
        } catch (Exception e) {
            return new ShopOperationResult(false, Cause.OPERATION_FAILED);
        }

        for (final var entry : cost.entrySet()) {
            api.economy().withdraw(entry.getKey(), entry.getValue());
        }

        api.economy().deposit(uniqueId, shop.sellPrice());

        itemStack.setAmount(shop.quantity());

        player.getInventory().removeItem(itemStack);

        return new ShopOperationResult(true, Cause.NONE);
    }

    @Override
    public ShopOperationResult withdraw(final UUID uniqueId, final Shop shop, final int amount) {
        if (amount < 1)
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_TO_WITHDRAW);

        try {
            api.repository().refresh(shop);
        } catch (final Exception e) {
            return new ShopOperationResult(false, Cause.OPERATION_FAILED);
        }

        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));

        //TODO: stock == 0?
        if (shop.stock() < amount)
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_TO_WITHDRAW);

        //TODO: check for space

        final var stock = shop.stock();

        shop.stock(stock - amount);

        try {
            api.repository().update(shop);

            final var log = api.repository()
                    .<ShopLog.Builder>builder(ShopLog.Builder.class)
                    .action(ShopLog.Action.WITHDRAW)
                    .uniqueId(uniqueId)
                    .serialized(new IntValueChanged(stock, shop.stock()))
                    .build();

            shop.logs().add(log);
        } catch (final Exception e) {
            return new ShopOperationResult(false, Cause.OPERATION_FAILED);
        }

        addItemToInventory(shop, player);

        return new ShopOperationResult(true, Cause.NONE);
    }

    @Override
    public ShopOperationResult deposit(final UUID uniqueId, final Shop shop, final int amount) {
        if (amount < 1)
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_TO_DEPOSIT);

        try {
            api.repository().refresh(shop);
        } catch (final Exception e) {
            return new ShopOperationResult(false, Cause.OPERATION_FAILED);
        }

        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        //TODO: doesn't contain at least 1?
        if (!player.getInventory().containsAtLeast(itemStack, amount))
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_TO_DEPOSIT);

        final var stock = shop.stock();

        shop.stock(stock + amount);

        try {
            api.repository().update(shop);

            final var log = api.repository()
                    .<ShopLog.Builder>builder(ShopLog.Builder.class)
                    .action(ShopLog.Action.DEPOSIT)
                    .uniqueId(uniqueId)
                    .serialized(new IntValueChanged(stock, shop.stock()))
                    .build();

            shop.logs().add(log);
        } catch (final Exception e) {
            return new ShopOperationResult(false, Cause.OPERATION_FAILED);
        }

        itemStack.setAmount(amount);

        //TODO: what does the hashmap do?
        player.getInventory().removeItem(itemStack);

        return new ShopOperationResult(true, Cause.NONE);
    }

    private static void addItemToInventory(final Shop shop, final Player player) {
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        itemStack.setAmount(shop.quantity());

        player.getInventory().addItem(itemStack);
    }

}
