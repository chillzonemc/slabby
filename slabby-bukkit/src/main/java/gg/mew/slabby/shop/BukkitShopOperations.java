package gg.mew.slabby.shop;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.permission.SlabbyPermissions;
import gg.mew.slabby.shop.log.Transaction;
import gg.mew.slabby.shop.log.ValueChanged;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;

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
    public void ifWizardOrElse(final UUID uniqueId, final Consumer<ShopWizard> action, final Runnable orElse) {
        final var wizard = this.wizards.get(uniqueId);

        if (wizard != null)
            action.accept(wizard);
        else
            orElse.run();
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

        if (!shop.hasStock(shop.quantity()))
            return new ShopOperationResult(false, Cause.INSUFFICIENT_STOCK_TO_WITHDRAW);

        //TODO: check for space

        final var result = api.economy().withdraw(uniqueId, shop.buyPrice());

        if (!result.success())
            return new ShopOperationResult(false, Cause.INSUFFICIENT_BALANCE_TO_BUY);

        if (shop.stock() != null)
            shop.stock(shop.stock() - shop.quantity());

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
                return new ShopOperationResult(false, Cause.INSUFFICIENT_BALANCE_TO_SELL);
        }

        if (shop.stock() != null)
            shop.stock(shop.stock() - shop.quantity());

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

        //TODO: do not allow if stock is null

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
                    .serialized(new ValueChanged.Int(stock, shop.stock()))
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

        //TODO: do not allow if stock is null

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
                    .serialized(new ValueChanged.Int(stock, shop.stock()))
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

    @Override
    public void createOrUpdateShop(final UUID uniqueId, final ShopWizard wizard) throws Exception {
        final var success = api.repository().transaction(() -> {
            final var shopOpt = api.repository().shopById(wizard.id());

            if (shopOpt.isPresent()) {
                final var shop = shopOpt.get();

                shop.buyPrice(wizard.buyPrice());
                shop.sellPrice(wizard.sellPrice());
                shop.quantity(wizard.quantity());
                shop.note(wizard.note());
                shop.state(wizard.state());

                shop.location(wizard.x(), wizard.y(), wizard.z(), wizard.world());

                for (final var entry : wizard.valueChanges().entrySet()) {
                    final var log = api.repository().<ShopLog.Builder>builder(ShopLog.Builder.class)
                            .action(entry.getKey())
                            .uniqueId(uniqueId)
                            .serialized(entry.getValue())
                            .build();

                    shop.logs().add(log);
                }

                api.repository().update(shop);
            } else {
                final var shop = api.repository().<Shop.Builder>builder(Shop.Builder.class)
                        .x(wizard.x())
                        .y(wizard.y())
                        .z(wizard.z())
                        .world(wizard.world())
                        .item(wizard.item())
                        .buyPrice(wizard.buyPrice())
                        .sellPrice(wizard.sellPrice())
                        .quantity(wizard.quantity())
                        .note(wizard.note())
                        .stock(api.isAdminMode(uniqueId) ? null : 0)
                        .build();

                api.repository().createOrUpdate(shop);

                shop.owners().add(api.repository().<ShopOwner.Builder>builder(ShopOwner.Builder.class)
                        .uniqueId(uniqueId)
                        .share(100)
                        .build());
            }

            return true;
        });

        if (success) {
            final var shopOpt = api.repository().shopAt(wizard.x(), wizard.y(), wizard.z(), wizard.world());

            if (shopOpt.isPresent()) {
                final var shop = shopOpt.get();

                removeAndSpawnDisplayItem(shop);

                api.repository().update(shop);
            }
        }
    }

    @Override
    public void removeShop(final Shop shop) throws Exception {
        if (shop.displayEntityId() != null && Bukkit.getEntity(shop.displayEntityId()) instanceof Display e)
            e.remove();

        api.repository().markAsDeleted(shop);
    }

    @Override
    public void removeAndSpawnDisplayItem(final Shop shop) {
        final var item = Bukkit.getItemFactory().createItemStack(shop.item());
        final var world = Bukkit.getWorld(shop.world());

        if (shop.displayEntityId() != null && Bukkit.getEntity(shop.displayEntityId()) instanceof Display e)
            e.remove();

        final var block = world.getBlockAt(shop.x(), shop.y(), shop.z());

        final var itemDisplay = (ItemDisplay) world.spawnEntity(new Location(world, block.getBoundingBox().getCenterX(), block.getBoundingBox().getMaxY(), block.getBoundingBox().getCenterZ()), EntityType.ITEM_DISPLAY);

        itemDisplay.setBillboard(Display.Billboard.VERTICAL);

        itemDisplay.setItemStack(item);
        itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);

        shop.displayEntityId(itemDisplay.getUniqueId());
    }

    private static void addItemToInventory(final Shop shop, final Player player) {
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        itemStack.setAmount(shop.quantity());

        player.getInventory().addItem(itemStack);
    }

}
