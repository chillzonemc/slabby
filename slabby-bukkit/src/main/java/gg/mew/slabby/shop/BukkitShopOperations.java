package gg.mew.slabby.shop;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.exception.*;
import gg.mew.slabby.exception.UnsupportedOperationException;
import gg.mew.slabby.permission.SlabbyPermissions;
import gg.mew.slabby.shop.log.LocationChanged;
import gg.mew.slabby.shop.log.Transaction;
import gg.mew.slabby.shop.log.ValueChanged;
import gg.mew.slabby.wrapper.sound.Sounds;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;
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
    public void buy(final UUID uniqueId, final Shop shop) throws SlabbyException {
        if (!api.permission().hasPermission(uniqueId, SlabbyPermissions.SHOP_INTERACT))
            throw new NoPermissionException();

        try {
            api.repository().refresh(shop);
        } catch (final Exception e) {
            throw new UnrecoverableException(e);
        }

        if (shop.buyPrice() == null)
            throw new UnsupportedOperationException("Unable to buy from shop: shop is not selling");

        final var client = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));

        if (!shop.hasStock(shop.quantity()))
            throw new ShopOutOfStockException();

        //TODO: check for space

        final var result = api.economy().withdraw(uniqueId, shop.buyPrice());

        if (!result.success())
            throw new InsufficientBalanceToBuyException();

        if (shop.stock() != null)
            shop.stock(shop.stock() - shop.quantity());

        final var cost = splitCost(result.amount(), shop);

        for (final var entry : cost.entrySet()) {
            //TODO: verify
            final var success = api.economy().deposit(entry.getKey(), entry.getValue());
        }

        api.repository().update(shop);

        final var log = api.repository()
                .<ShopLog.Builder>builder(ShopLog.Builder.class)
                .action(ShopLog.Action.BUY)
                .uniqueId(uniqueId)
                .serialized(new Transaction(shop.buyPrice(), shop.quantity()))
                .build();

        shop.logs().add(log);

        final var itemStack = addItemToInventory(shop, client);

        notifyBuy(uniqueId, shop, client, itemStack);
    }

    private void notifyBuy(final UUID uniqueId, final Shop shop, final Player client, final ItemStack itemStack) {
        api.sound().play(uniqueId, shop, Sounds.BUY_SELL_SUCCESS);

        client.sendMessage(api.messages().client().buy().message(itemStack.displayName(), shop.quantity(), shop.buyPrice()));

        for (final var shopOwner : shop.owners()) {
            final var playerOwner = Bukkit.getPlayer(shopOwner.uniqueId());

            if (playerOwner != null) {
                playerOwner.sendMessage(api.messages().client().buy().messageOwner(client.displayName(), shop.quantity(), itemStack.displayName(), shop.buyPrice()));
            }
        }
    }

    @Override
    public void sell(final UUID uniqueId, final Shop shop) throws SlabbyException {
        if (!api.permission().hasPermission(uniqueId, SlabbyPermissions.SHOP_INTERACT))
            throw new NoPermissionException();

        api.repository().refresh(shop);

        if (shop.sellPrice() == null)
            throw new UnsupportedOperationException("Unable to sell to shop: shop is not buying");

        final var client = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        if (!client.getInventory().containsAtLeast(itemStack, shop.quantity()))
            throw new PlayerOutOfStockException();

        final var cost = splitCost(shop.sellPrice(), shop);

        if (cost.entrySet().stream().anyMatch(it -> !api.economy().hasAmount(it.getKey(), it.getValue())))
            throw new InsufficientBalanceToSellException();

        if (shop.stock() != null)
            shop.stock(shop.stock() - shop.quantity());

        api.repository().update(shop);

        final var log = api.repository()
                .<ShopLog.Builder>builder(ShopLog.Builder.class)
                .action(ShopLog.Action.SELL)
                .uniqueId(uniqueId)
                .serialized(new Transaction(shop.sellPrice(), shop.quantity()))
                .build();

        shop.logs().add(log);

        for (final var entry : cost.entrySet()) {
            api.economy().withdraw(entry.getKey(), entry.getValue());
        }

        api.economy().deposit(uniqueId, shop.sellPrice());

        itemStack.setAmount(shop.quantity());

        client.getInventory().removeItem(itemStack);

        notifySell(shop, client, itemStack);
    }

    private void notifySell(final Shop shop, final Player client, final ItemStack itemStack) {
        api.sound().play(client.getUniqueId(), shop, Sounds.BUY_SELL_SUCCESS);

        client.sendMessage(api.messages().client().sell().message(itemStack.displayName(), shop.quantity(), shop.sellPrice()));

        for (final var shopOwner : shop.owners()) {
            final var playerOwner = Bukkit.getPlayer(shopOwner.uniqueId());

            if (playerOwner != null) {
                playerOwner.sendMessage(api.messages().client().sell().messageOwner(client.displayName(), shop.quantity(), itemStack.displayName(), shop.buyPrice()));
            }
        }
    }

    @Override
    public void withdraw(final UUID uniqueId, final Shop shop, final int amount) throws SlabbyException {
        if (amount < 1)
            throw new IllegalArgumentException("Amount has to be higher than zero");

        if (shop.stock() == null)
            throw new UnsupportedOperationException("Cannot withdraw from admin shop");

        try {
            api.repository().refresh(shop);
        } catch (final Exception e) {
            throw new UnrecoverableException(e);
        }

        if (!shop.hasStock(amount))
            throw new ShopOutOfStockException();

        //TODO: check for space

        final var stock = shop.stock();

        shop.stock(stock - amount);

        api.repository().update(shop);

        final var log = api.repository()
                .<ShopLog.Builder>builder(ShopLog.Builder.class)
                .action(ShopLog.Action.WITHDRAW)
                .uniqueId(uniqueId)
                .serialized(new ValueChanged.Int(stock, shop.stock()))
                .build();

        shop.logs().add(log);

        addItemToInventory(shop, Objects.requireNonNull(Bukkit.getPlayer(uniqueId)));

        api.sound().play(uniqueId, shop, Sounds.BUY_SELL_SUCCESS);
    }

    @Override
    public void deposit(final UUID uniqueId, final Shop shop, final int amount) throws SlabbyException {
        if (amount < 1)
            throw new IllegalArgumentException("Amount has to be higher than zero");

        if (shop.stock() == null)
            throw new UnsupportedOperationException("Cannot deposit to admin shop");

        api.repository().refresh(shop);

        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        if (!player.getInventory().containsAtLeast(itemStack, amount))
            throw new PlayerOutOfStockException();

        final var stock = shop.stock();

        shop.stock(stock + amount);

        api.repository().update(shop);

        final var log = api.repository()
                .<ShopLog.Builder>builder(ShopLog.Builder.class)
                .action(ShopLog.Action.DEPOSIT)
                .uniqueId(uniqueId)
                .serialized(new ValueChanged.Int(stock, shop.stock()))
                .build();

        shop.logs().add(log);

        itemStack.setAmount(amount);

        //TODO: what does the hashmap do?
        player.getInventory().removeItem(itemStack);


        api.sound().play(uniqueId, shop, Sounds.BUY_SELL_SUCCESS);
    }

    @Override
    public void createOrUpdateShop(final UUID uniqueId, final ShopWizard wizard) throws SlabbyException {
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
    public void removeShop(final UUID uniqueId, final Shop shop) throws SlabbyException {
        if (shop.displayEntityId() != null && Bukkit.getEntity(shop.displayEntityId()) instanceof Display e)
            e.remove();

        api.repository().markAsDeleted(shop);

        api.sound().play(uniqueId, shop, Sounds.DESTROY);
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

    @Override
    public void unlinkShop(final UUID uniqueId, final Shop shop) throws SlabbyException {
        shop.inventory(null, null, null, null);

        api.repository().update(shop);

        final var log = api.repository().<ShopLog.Builder>builder(ShopLog.Builder.class)
                .action(ShopLog.Action.INVENTORY_LINK_CHANGED)
                .uniqueId(uniqueId)
                .serialized(new LocationChanged(null, null, null, null))
                .build();

        shop.logs().add(log);

        api.sound().play(uniqueId, shop, Sounds.MODIFY_SUCCESS);

        Objects.requireNonNull(Bukkit.getPlayer(uniqueId))
                .sendMessage(api.messages().owner().inventoryLink().cancel().message());
    }

    private static ItemStack addItemToInventory(final Shop shop, final Player player) {
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        final var itemStacks = new ArrayList<ItemStack>();

        var quantity = shop.quantity();

        while (quantity > 0) {
            final var cloneStack = itemStack.clone();
            final var maxStackSize = Math.min(quantity, cloneStack.getMaxStackSize());

            cloneStack.setAmount(maxStackSize);

            itemStacks.add(cloneStack);

            quantity -= maxStackSize;
        }

        player.getInventory().addItem(itemStacks.toArray(ItemStack[]::new));

        return itemStack;
    }

}
