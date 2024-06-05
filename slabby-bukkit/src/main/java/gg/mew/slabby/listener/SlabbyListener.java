package gg.mew.slabby.listener;

import gg.mew.slabby.Slabby;
import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.invui.ReloadableItem;
import gg.mew.slabby.shop.Shop;
import gg.mew.slabby.shop.ShopOwner;
import gg.mew.slabby.shop.ShopWizard;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor
public final class SlabbyListener implements Listener {

    private final SlabbyAPI api;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(final PlayerInteractEvent event) {
        final var block = event.getClickedBlock();

        //noinspection DataFlowIssue
        if (!event.hasBlock() || block.getType() == Material.AIR || event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        var tryOpen = true;

        final var shopOpt = api.repository().shopAt(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

        if (event.getItem() != null) {
            final var configurationItem = Bukkit.getItemFactory().createItemStack(api.configuration().item());

            if (event.getItem().isSimilar(configurationItem)) {
                //TODO: don't allow if shop already exists, plus other filters.

                tryOpen = false;

                if (shopOpt.isPresent()) {
                    destroyShopUI(event.getPlayer(), shopOpt.get());
                } else {
                    newShopUI(event.getPlayer(), block);
                }
            }
        }

        if (tryOpen) {
            shopOpt.ifPresent(shop -> {
                ownerShopUI(event.getPlayer(), shop);
            });
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

                modifyShopUI(event.getWhoClicked(), wizard);

                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getReason() == InventoryCloseEvent.Reason.PLAYER) {
            //TODO: or shop ui again?
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

            //TODO: catch exceptions, limit precision to 2 decimals
            switch (wizard.state()) {
                case AWAITING_NOTE -> wizard.note(text);
                case AWAITING_BUY_PRICE -> wizard.buyPrice(Double.parseDouble(text));
                case AWAITING_SELL_PRICE -> wizard.sellPrice(Double.parseDouble(text));
                case AWAITING_QUANTITY -> wizard.quantity(Integer.parseInt(text));
            }

            wizard.state(ShopWizard.WizardState.AWAITING_CONFIRMATION);

            modifyShopUI(event.getPlayer(), wizard);

            event.setCancelled(true);
        }
    }

    //TODO: clientShopUI

    private ItemStack itemStack(final Material material, final Consumer<ItemStack> action) {
        final var itemStack = new ItemStack(material);

        action.accept(itemStack);

        return itemStack;
    }

    private void clientShopUI(final Player client, final Shop shop) {
        final var item = Bukkit.getItemFactory().createItemStack(shop.item());

        //TODO: remove buy/sell options when not available

        final var gui = Gui.normal()
                .setStructure("12..3.456")
                .addIngredient('1', new ReloadableItem(s -> itemStack(Material.GOLD_INGOT, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(
                            Component.text("Buy '", NamedTextColor.GOLD)
                                    .append(item.displayName())
                                    .append(Component.text(String.format("' * %d", shop.quantity()), NamedTextColor.GOLD))
                    );
                    meta.lore(new ArrayList<>() {{
                        if (shop.buyPrice() != null) {
                            add(Component.text(String.format("Buy for: %s", api.decimalFormat().format(shop.buyPrice())), NamedTextColor.DARK_PURPLE));
                        }
                        add(Component.text(String.format("In stock: %d", shop.stock()), NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("(%s stacks)", shop.stock() / item.getMaxStackSize()), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    final var result = api.operations().buy(client.getUniqueId(), shop);

                    if (!result.success())
                        client.sendMessage(Component.text(result.cause().name()));
                }))
                .addIngredient('2', new ReloadableItem(s -> itemStack(Material.IRON_INGOT, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(
                            Component.text("Sell '", NamedTextColor.GOLD)
                                    .append(item.displayName())
                                    .append(Component.text(String.format("' * %d", shop.quantity()), NamedTextColor.GOLD))
                    );
                    meta.lore(new ArrayList<>() {{
                        if (shop.sellPrice() != null) {
                            add(Component.text(String.format("Sell for: %s", api.decimalFormat().format(shop.sellPrice())), NamedTextColor.DARK_PURPLE));
                        }
                        add(Component.text(String.format("In stock: %d", shop.stock()), NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("(%s stacks)", shop.stock() / item.getMaxStackSize()), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    final var result = api.operations().sell(client.getUniqueId(), shop);

                    if (!result.success())
                        client.sendMessage(Component.text(result.cause().name()));
                }))
                .addIngredient('.', new SimpleItem(new ItemBuilder(Material.AIR)))
                .addIngredient('3', new SimpleItem(item))
                .addIngredient('4', new SimpleItem(itemStack(Material.NAME_TAG, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Sellers note", NamedTextColor.GREEN));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(shop.note(), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                })))
                .addIngredient('5', new SimpleItem(itemStack(Material.PAPER, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Current funds", NamedTextColor.GOLD));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("Funds:", NamedTextColor.DARK_PURPLE)
                                .appendSpace()
                                .color(NamedTextColor.GREEN)
                                .append(Component.text(String.format("$%s", api.decimalFormat().format(api.economy().balance(client.getUniqueId()))))));
                    }});
                    it.setItemMeta(meta);
                })))
                .addIngredient('6', new SimpleItem(itemStack(Material.COMMAND_BLOCK, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Slabby Shop", NamedTextColor.GOLD));

                    final var owners = shop.owners().stream().map(o -> Bukkit.getOfflinePlayer(o.uniqueId()).getName()).toArray(String[]::new);

                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("Owned by %s", String.join(", ", owners)), NamedTextColor.GREEN));
                        add(Component.text("Selling: ", NamedTextColor.DARK_PURPLE).append(item.displayName()));

                        //TODO: ensure quantity > 0

                        if (shop.buyPrice() != null) {
                            final var buyPrice = api.decimalFormat().format(shop.buyPrice());
                            final var buyPriceEach = shop.buyPrice() == 0 ? "0" : api.decimalFormat().format(shop.buyPrice() / shop.quantity());

                            add(Component.text(String.format("Buy %d for $%s ($%s each)", shop.quantity(), buyPrice, buyPriceEach), NamedTextColor.DARK_PURPLE));
                        }

                        if (shop.sellPrice() != null) {
                            final var sellPrice = api.decimalFormat().format(shop.sellPrice());
                            final var sellPriceEach = shop.sellPrice() == 0 ? "0" : api.decimalFormat().format(shop.sellPrice() / shop.quantity());

                            add(Component.text(String.format("Sell %d for $%s ($%s each)", shop.quantity(), sellPrice, sellPriceEach), NamedTextColor.DARK_PURPLE));
                        }
                    }});

                    it.setItemMeta(meta);
                })))
                .build();

        final var window = Window.single()
                .setViewer(client)
                .setTitle("[Slabby] Client")
                .setGui(gui)
                .build();

        window.open();
    }

    private void destroyShopUI(final Player shopOwner, final Shop shop) {
        final var gui = Gui.normal()
                .setStructure("...123...")
                .addIngredient('.', new SimpleItem(new ItemBuilder(Material.AIR)))
                .addIngredient('1', new SimpleItem(itemStack(Material.GREEN_STAINED_GLASS_PANE, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Destroy Shop", NamedTextColor.GREEN));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("This will destroy your items.", NamedTextColor.RED));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    api.repository().destroy(shop);
                    shopOwner.closeInventory();
                }))
                .addIngredient('2', new SimpleItem(itemStack(Material.COMMAND_BLOCK, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Slabby Shop", NamedTextColor.GOLD));

                    final var owners = shop.owners().stream().map(o -> Bukkit.getOfflinePlayer(o.uniqueId()).getName()).toArray(String[]::new);

                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("Owned by %s", String.join(", ", owners)), NamedTextColor.GREEN));

                        final var item = Bukkit.getItemFactory().createItemStack(shop.item());

                        add(Component.text("Selling: ", NamedTextColor.DARK_PURPLE).append(item.displayName()));

                        //TODO: ensure quantity > 0

                        if (shop.buyPrice() != null) {
                            final var buyPrice = api.decimalFormat().format(shop.buyPrice());
                            final var buyPriceEach = shop.buyPrice() == 0 ? "0" : api.decimalFormat().format(shop.buyPrice() / shop.quantity());

                            add(Component.text(String.format("Buy %d for $%s ($%s each)", shop.quantity(), buyPrice, buyPriceEach), NamedTextColor.DARK_PURPLE));
                        }

                        if (shop.sellPrice() != null) {
                            final var sellPrice = api.decimalFormat().format(shop.sellPrice());
                            final var sellPriceEach = shop.sellPrice() == 0 ? "0" : api.decimalFormat().format(shop.sellPrice() / shop.quantity());

                            add(Component.text(String.format("Sell %d for $%s ($%s each)", shop.quantity(), sellPrice, sellPriceEach), NamedTextColor.DARK_PURPLE));
                        }
                    }});

                    it.setItemMeta(meta);
                })))
                .addIngredient('3', new SimpleItem(itemStack(Material.BARRIER, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Cancel", NamedTextColor.RED));
                    it.setItemMeta(meta);
                }), c -> {
                    shopOwner.closeInventory();
                }))
                .build();

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle("[Slabby] Destroy Shop")
                .setGui(gui)
                .build();

        window.open();
    }

    private void newShopUI(final Player shopOwner, final Block block) {
        final var gui = Gui.normal()
                .setStructure(".........")
                .addIngredient('.', new SimpleItem(new ItemBuilder(Material.RED_STAINED_GLASS_PANE)))
                .build();

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle("[Slabby] New Shop")//TODO: translate
                .setGui(gui)
                .build();

        window.open();

        final var wizard = api.operations().wizardFor(shopOwner.getUniqueId());

        wizard.state(ShopWizard.WizardState.AWAITING_ITEM)
                .x(block.getX())
                .y(block.getY())
                .z(block.getZ())
                .world(block.getWorld().getName());
    }

    private void ownerShopUI(final Player shopOwner, final Shop shop) {
        final var item = Bukkit.getItemFactory().createItemStack(shop.item());
        final var gui = Gui.normal()
                .setStructure("shm.icprt")
                .addIngredient('s', new ReloadableItem(s -> itemStack(Material.CHEST_MINECART, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Deposit '", NamedTextColor.GOLD)
                            .append(item.displayName()) //TODO: reset?
                            .append(Component.text("'"))
                    );
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("+Shift for bulk deposit", NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("In stock: %d", shop.stock()), NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("(%d stacks)", shop.stock() / item.getMaxStackSize()), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    //TODO: ensure two people using a shop at the same time does not cause issues. e.g shop object not updating its stock
                    final var result = api.operations().deposit(shopOwner.getUniqueId(), shop, shop.quantity());

                    if (!result.success())
                        shopOwner.sendMessage(Component.text(result.cause().name()));
                }))
                .addIngredient('h', new ReloadableItem(s -> itemStack(Material.HOPPER_MINECART, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Withdraw '", NamedTextColor.GOLD)
                            .append(item.displayName()) //TODO: reset?
                            .append(Component.text("'"))
                    );
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("+Shift for bulk withdrawal", NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("In stock: %d", shop.stock()), NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("(%d stacks)", shop.stock() / item.getMaxStackSize()), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    final var result = api.operations().withdraw(shopOwner.getUniqueId(), shop, shop.quantity());

                    if (!result.success())
                        shopOwner.sendMessage(Component.text(result.cause().name()));
                }))
                .addIngredient('m', new ReloadableItem(s -> itemStack(Material.MINECART, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Change rate", NamedTextColor.GOLD));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("Amount per click: %d", shop.quantity()), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    shopOwner.sendMessage(Component.text("This feature is not available", NamedTextColor.RED));
                }))
                .addIngredient('.', new SimpleItem(new ItemBuilder(Material.AIR)))
                .addIngredient('i', new SimpleItem(new ItemBuilder(Bukkit.getItemFactory().createItemStack(shop.item()))))
                .addIngredient('c', new SimpleItem(itemStack(Material.CHEST, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Link chest", NamedTextColor.GOLD));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("Link a chest for refilling!", NamedTextColor.GREEN));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    shopOwner.sendMessage(Component.text("This feature is not available", NamedTextColor.RED));
                }))
                .addIngredient('p', new SimpleItem(itemStack(Material.COMMAND_BLOCK, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Slabby Shop", NamedTextColor.GOLD));

                    final var owners = shop.owners().stream().map(o -> Bukkit.getOfflinePlayer(o.uniqueId()).getName()).toArray(String[]::new);

                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("Owned by %s", String.join(", ", owners)), NamedTextColor.GREEN));
                        add(Component.text("Selling: ", NamedTextColor.DARK_PURPLE).append(item.displayName()));

                        //TODO: ensure quantity > 0

                        if (shop.buyPrice() != null) {
                            final var buyPrice = api.decimalFormat().format(shop.buyPrice());
                            final var buyPriceEach = shop.buyPrice() == 0 ? "0" : api.decimalFormat().format(shop.buyPrice() / shop.quantity());

                            add(Component.text(String.format("Buy %d for $%s ($%s each)", shop.quantity(), buyPrice, buyPriceEach), NamedTextColor.DARK_PURPLE));
                        }

                        if (shop.sellPrice() != null) {
                            final var sellPrice = api.decimalFormat().format(shop.sellPrice());
                            final var sellPriceEach = shop.sellPrice() == 0 ? "0" : api.decimalFormat().format(shop.sellPrice() / shop.quantity());

                            add(Component.text(String.format("Sell %d for $%s ($%s each)", shop.quantity(), sellPrice, sellPriceEach), NamedTextColor.DARK_PURPLE));
                        }
                    }});

                    it.setItemMeta(meta);
                })))
                .addIngredient('r', new SimpleItem(itemStack(Material.COMPARATOR, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Modify Shop", NamedTextColor.GOLD));
                    it.setItemMeta(meta);
                }), c -> {
                    modifyShopUI(shopOwner, api.operations().wizardFor(shopOwner.getUniqueId()).useExisting(shop));
                }))
                .addIngredient('t', new SimpleItem(itemStack(Material.OAK_SIGN, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("View as customer", NamedTextColor.GOLD));
                    it.setItemMeta(meta);
                }), c -> {
                    clientShopUI(shopOwner, shop);
                }))
                .build();

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle("[Slabby] Owner") //TODO: translate
                .setGui(gui)
                .build();

        window.open();
    }

    private void modifyShopUI(final HumanEntity shopOwner, final ShopWizard wizard) {
        final var gui = Gui.normal()
                .setStructure("it.gry.nb")
                .addIngredient('.', new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('i', new SimpleItem(Bukkit.getItemFactory().createItemStack(wizard.item())))
                .addIngredient('t', new SimpleItem(itemStack(Material.NAME_TAG, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Sellers note", NamedTextColor.GREEN));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(wizard.note(), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    wizard.state(ShopWizard.WizardState.AWAITING_NOTE);
                    shopOwner.closeInventory();
                    shopOwner.sendMessage(Component.text("Please enter your note."));
                }))
                .addIngredient('g', new SimpleItem(itemStack(Material.GREEN_STAINED_GLASS_PANE, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Buy price", NamedTextColor.GREEN));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("$%.2f", wizard.buyPrice()), NamedTextColor.DARK_PURPLE));
                        add(Component.text("Click to set", NamedTextColor.DARK_PURPLE));
                        add(Component.text("(-1 means not for sale)", NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    wizard.state(ShopWizard.WizardState.AWAITING_BUY_PRICE);
                    shopOwner.closeInventory();
                    shopOwner.sendMessage(Component.text("Please enter your buy price."));
                }))
                .addIngredient('r', new SimpleItem(itemStack(Material.RED_STAINED_GLASS_PANE, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Sell price", NamedTextColor.RED));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("$%.2f", wizard.sellPrice()), NamedTextColor.DARK_PURPLE));
                        add(Component.text("Click to set", NamedTextColor.DARK_PURPLE));
                        add(Component.text("(-1 means not buying)", NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    wizard.state(ShopWizard.WizardState.AWAITING_SELL_PRICE);
                    shopOwner.closeInventory();
                    shopOwner.sendMessage(Component.text("Please enter your sell price."));
                }))
                .addIngredient('y', new SimpleItem(itemStack(Material.YELLOW_STAINED_GLASS_PANE, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Quantity", NamedTextColor.YELLOW));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("Amount per transaction: %d", wizard.quantity()), NamedTextColor.DARK_PURPLE));
                        add(Component.text("Click to set", NamedTextColor.DARK_PURPLE));
                        add(Component.text("(Amount of items per buy/sell)", NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    wizard.state(ShopWizard.WizardState.AWAITING_QUANTITY);
                    shopOwner.closeInventory();
                    shopOwner.sendMessage(Component.text("Please enter your quantity."));
                }))
                .addIngredient('n', new SimpleItem(itemStack(Material.NETHER_STAR, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Confirm", NamedTextColor.GREEN));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("New Shop", NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("%s,%d,%d,%d", wizard.world(), wizard.x(), wizard.y(), wizard.z()), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {
                    //TODO: should be create or update.

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
                            .build();

                    wizard.destroy();

                    api.repository().create(shop);

                    shop.owners().add(api.repository().<ShopOwner.Builder>builder(ShopOwner.Builder.class)
                            .uniqueId(shopOwner.getUniqueId())
                            .share(100)
                            .build());

                    shopOwner.closeInventory();

                    shopOwner.sendMessage(Component.text("Shop created!"));
                }))
                .addIngredient('b', new SimpleItem(itemStack(Material.BARRIER, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Cancel", NamedTextColor.RED));
                    it.setItemMeta(meta);
                }), c -> {
                    wizard.destroy();
                    shopOwner.closeInventory();
                }))
                .build();

        final var window = Window.single()
                .setViewer((Player) shopOwner)
                .setTitle("[Slabby] Editing Shop") //TODO: translate
                .setGui(gui)
                .build();

        Bukkit.getScheduler().runTask((Slabby)api, window::open);
    }

}
