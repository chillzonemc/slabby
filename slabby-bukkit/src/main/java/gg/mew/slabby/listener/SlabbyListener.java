package gg.mew.slabby.listener;

import gg.mew.slabby.Slabby;
import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.gui.*;
import gg.mew.slabby.permission.SlabbyPermissions;
import gg.mew.slabby.shop.Shop;
import gg.mew.slabby.shop.ShopOperations;
import gg.mew.slabby.shop.ShopOwner;
import gg.mew.slabby.shop.ShopWizard;
import gg.mew.slabby.wrapper.sound.Sounds;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AutoUpdateItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.item.impl.SuppliedItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class SlabbyListener implements Listener {

    private final SlabbyAPI api;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(final PlayerInteractEvent event) {
        api.permission().ifPermission(event.getPlayer().getUniqueId(), SlabbyPermissions.SHOP_INTERACT, () -> handlePlayerInteract(event));
    }

    private void handlePlayerInteract(PlayerInteractEvent event) {
        final var block = event.getClickedBlock();
        final var player = event.getPlayer();

        if (block == null || block.getType() == Material.AIR || event.getHand() != EquipmentSlot.HAND)
            return;

        final Optional<Shop> shopOpt;

        try {
            shopOpt = api.repository().shopAt(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
        } catch (final Exception e) {
            final var location = player.getLocation();
            api.sound().play(player.getUniqueId(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName(), Sounds.BLOCKED);
            //TODO: notify player
            return;
        }

        final var configurationItem = Bukkit.getItemFactory().createItemStack(api.configuration().item());
        final var hasConfigurationItem = event.getItem() != null && event.getItem().isSimilar(configurationItem);

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK -> {
                shopOpt.ifPresentOrElse(shop -> {
                    //TODO: need a way for SHOP_MODIFY_OTHERS to express intent. command? /slabby admin
                    if (shop.isOwner(player.getUniqueId())) {
                        if (hasConfigurationItem) {
                            DestroyShopUI.open(api, player, shop);
                        } else {
                            OwnerShopUI.open(api, player, shop);
                        }
                    } else {
                        ClientShopUI.open(api, player, shop);
                    }
                }, () -> {
                    if (hasConfigurationItem) {
                        api.permission().ifPermission(player.getUniqueId(), SlabbyPermissions.SHOP_MODIFY, () -> CreateShopUI.open(api, player, block));
                    }
                });
            }
            case LEFT_CLICK_BLOCK -> {
                shopOpt.ifPresent(shop -> {
                    if (!shop.isOwner(player.getUniqueId()))
                        return;

                    if (api.configuration().restock().punch().enabled()) {
                        if (api.configuration().restock().punch().shulker() && event.getItem() != null && event.getItem().getType() == Material.SHULKER_BOX) {
                            //TODO: shulker, deposit operation only supports player inventory atm
                        } else if (api.configuration().restock().punch().bulk())  {
                            final var item = Bukkit.getItemFactory().createItemStack(shop.item());

                            final var toDeposit = player.isSneaking()
                                    ? Arrays.stream(player.getInventory().getContents())
                                    .filter(Objects::nonNull)
                                    .filter(item::isSimilar)
                                    .mapToInt(ItemStack::getAmount)
                                    .sum()
                                    : shop.quantity();

                            final var result = api.operations().deposit(player.getUniqueId(), shop, toDeposit);

                            if (result.success()) {
                                api.sound().play(player.getUniqueId(), shop, Sounds.BUY_SELL_SUCCESS);
                            }
                        }
                    }
                });

                if (api.operations().wizardExists(player.getUniqueId())) {
                    final var wizard = api.operations().wizardFor(player.getUniqueId());

                    if (wizard.state() == ShopWizard.WizardState.AWAITING_INVENTORY_LINK) {
                        if (player.isSneaking() && event.getClickedBlock().getType() == Material.CHEST) {
                            try {
                                final var linkShopOpt = api.repository().shopAt(wizard.x(), wizard.y(), wizard.z(), wizard.world());

                                linkShopOpt.ifPresent(shop -> {
                                    shop.inventoryX(block.getX());
                                    shop.inventoryY(block.getY());
                                    shop.inventoryZ(block.getZ());
                                    shop.inventoryWorld(block.getWorld().getName());
                                });

                                if (linkShopOpt.isPresent()) {
                                    api.repository().update(linkShopOpt.get());
                                    api.sound().play(player.getUniqueId(), linkShopOpt.get(), Sounds.SUCCESS);
                                }
                            } catch (final Exception e) {
                                //TODO: notify player
                            }
                            wizard.destroy();
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryClick(final InventoryClickEvent event) {
        if (api.operations().wizardExists(event.getWhoClicked().getUniqueId())) {
            final var wizard = api.operations().wizardFor(event.getWhoClicked().getUniqueId());

            if (wizard.state() == ShopWizard.WizardState.AWAITING_ITEM) {
                final var item = Objects.requireNonNull(event.getCurrentItem());

                wizard.item(item.getType().getKey().asString() + item.getItemMeta().getAsString());
                wizard.state(ShopWizard.WizardState.AWAITING_CONFIRMATION);

                ModifyShopUI.open(api, (Player) event.getWhoClicked(), wizard);

                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getReason() == InventoryCloseEvent.Reason.PLAYER) {
            api.operations().destroyWizard(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onChatMessage(final AsyncChatEvent event) {
        if (api.operations().wizardExists(event.getPlayer().getUniqueId())) {
            final var wizard = api.operations().wizardFor(event.getPlayer().getUniqueId());

            if (!wizard.state().awaitingTextInput())
                return;

            final var serializer = PlainTextComponentSerializer.plainText();
            final var text = serializer.serialize(event.message());

            //TODO: limit precision to 2 decimals

            try {
                switch (wizard.state()) {
                    case AWAITING_NOTE -> wizard.note(text);
                    case AWAITING_BUY_PRICE -> wizard.buyPrice(Double.parseDouble(text));
                    case AWAITING_SELL_PRICE -> wizard.sellPrice(Double.parseDouble(text));
                    case AWAITING_QUANTITY -> wizard.quantity(Integer.parseInt(text));
                }

                api.sound().play(event.getPlayer().getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.MODIFY_SUCCESS);
            } catch (final NumberFormatException e) {
                event.getPlayer().sendMessage(Component.text("That's not a valid number!", NamedTextColor.RED));
            }

            wizard.state(ShopWizard.WizardState.AWAITING_CONFIRMATION);

            ModifyShopUI.open(api, event.getPlayer(), wizard);

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryMoveItem(final InventoryMoveItemEvent event) {
        final var restock = api.configuration().restock();

        if (!restock.chests().enabled() || !restock.chests().hoppers().enabled())
            return;

        final var location = event.getDestination().getLocation();

        if (location == null)
            return;

        //TODO: do this per stack in order to reduce database calls

        Optional<Shop> shopOpt = Optional.empty();

        //TODO: caching would be very useful for this

        try {
            shopOpt = api.repository().shopWithInventoryAt(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
        } catch (final Exception ignored) {}

        shopOpt.ifPresent(shop -> {
            final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

            if (!itemStack.isSimilar(event.getItem()))
                return;

            shop.stock(shop.stock() + event.getItem().getAmount());

            try {
                api.repository().update(shop);

                event.setItem(ItemStack.empty());
            } catch (final Exception ignored) {}
        });
    }

}
