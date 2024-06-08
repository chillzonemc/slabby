package gg.mew.slabby.listener;

import gg.mew.slabby.Slabby;
import gg.mew.slabby.SlabbyAPI;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
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
        final var block = event.getClickedBlock();
        final var player = event.getPlayer();

        //noinspection DataFlowIssue
        if (!event.hasBlock() || block.getType() == Material.AIR || event.getHand() != EquipmentSlot.HAND)
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
                    if (shop.isOwner(player.getUniqueId())) {
                        if (hasConfigurationItem) {
                            destroyShopUI(player, shop);
                        } else {
                            ownerShopUI(player, shop);
                        }
                    } else {
                        clientShopUI(player, shop);
                    }
                }, () -> {
                    if (hasConfigurationItem)
                        newShopUI(player, block);
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

                modifyShopUI((Player) event.getWhoClicked(), wizard);

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
            } catch (NumberFormatException e) {
                event.getPlayer().sendMessage(Component.text("That's not a valid number!", NamedTextColor.RED));
            }

            wizard.state(ShopWizard.WizardState.AWAITING_CONFIRMATION);

            modifyShopUI(event.getPlayer(), wizard);

            event.setCancelled(true);
        }
    }

    private Supplier<? extends ItemProvider> itemStack(final Material material, final BiConsumer<ItemStack, ItemMeta> action) {
        return () -> s -> {
            final var itemStack = new ItemStack(material);
            final var meta = itemStack.getItemMeta();

            action.accept(itemStack, meta);

            itemStack.setItemMeta(meta);

            return itemStack;
        };
    }

    private void clientShopUI(final Player client, final Shop shop) {
        final var item = Bukkit.getItemFactory().createItemStack(shop.item());

        final var gui = Gui.normal()
                .setStructure("12..3.456")
                .addIngredient('1', new SuppliedItem(itemStack(Material.GOLD_INGOT, (it, meta) -> {
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
                }), c -> {
                    final var result = api.operations().buy(client.getUniqueId(), shop);

                    if (!result.success()) {
                        client.sendMessage(localize(result));
                        api.sound().play(client.getUniqueId(), shop, Sounds.BLOCKED);
                    } else {
                        api.sound().play(client.getUniqueId(), shop, Sounds.BUY_SELL_SUCCESS);

                        client.sendMessage(
                                Component.text("Bought", NamedTextColor.GREEN)
                                        .appendSpace()
                                        .append(Component.text(shop.quantity()))
                                        .appendSpace()
                                        .append(item.displayName())
                                        .appendSpace()
                                        .append(Component.text("for a total of $"))
                                        .append(Component.text(api.decimalFormat().format(shop.buyPrice())))
                        );
                        //TODO: notify sellers
                    }
                    return true;
                }))
                .addIngredient('2', new SuppliedItem(itemStack(Material.IRON_INGOT, (it, meta) -> {
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
                }), c -> {
                    final var result = api.operations().sell(client.getUniqueId(), shop);

                    if (!result.success()) {
                        client.sendMessage(localize(result));
                        api.sound().play(client.getUniqueId(), shop, Sounds.BLOCKED);
                    } else {
                        client.sendMessage(
                                Component.text("Sold", NamedTextColor.GREEN)
                                        .appendSpace()
                                        .append(Component.text(shop.quantity()))
                                        .appendSpace()
                                        .append(item.displayName())
                                        .appendSpace()
                                        .append(Component.text("for a total of $"))
                                        .append(Component.text(api.decimalFormat().format(shop.sellPrice())))
                        );
                        //TODO: notify sellers
                    }
                    return true;
                }))
                .addIngredient('.', new SimpleItem(new ItemBuilder(Material.AIR)))
                .addIngredient('3', new SimpleItem(item))
                .addIngredient('4', new SimpleItem(itemStack(Material.NAME_TAG, (it, meta) -> {
                    meta.displayName(Component.text("Sellers note", NamedTextColor.GREEN));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(shop.note(), NamedTextColor.DARK_PURPLE));
                    }});
                }).get()))
                .addIngredient('5', new AutoUpdateItem(20, itemStack(Material.PAPER, (it, meta) -> {
                    meta.displayName(Component.text("Current funds", NamedTextColor.GOLD));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("Funds:", NamedTextColor.DARK_PURPLE)
                                .appendSpace()
                                .color(NamedTextColor.GREEN)
                                .append(Component.text(String.format("$%s", api.decimalFormat().format(api.economy().balance(client.getUniqueId()))))));
                    }});
                })))
                .addIngredient('6', new SimpleItem(itemStack(Material.COMMAND_BLOCK, (it, meta) -> {
                    meta.displayName(Component.text("Slabby Shop", NamedTextColor.GOLD));

                    final var owners = shop.owners().stream().map(o -> Bukkit.getOfflinePlayer(o.uniqueId()).getName()).toArray(String[]::new);

                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("Owned by %s", String.join(", ", owners)), NamedTextColor.GREEN));
                        add(Component.text("Selling: ", NamedTextColor.DARK_PURPLE).append(item.displayName()));

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
                }).get()))
                .build();

        final var window = Window.single()
                .setViewer(client)
                .setTitle("[Slabby] Client")
                .setGui(gui)
                .build();

        window.open();
    }

    private static Component localize(ShopOperations.ShopOperationResult result) {
        return switch (result.cause()) {
            case INSUFFICIENT_BALANCE_TO_WITHDRAW -> Component.text("You don't have enough funds!", NamedTextColor.RED);
            case INSUFFICIENT_BALANCE_TO_DEPOSIT ->
                    Component.text("The shop doesn't have enough funds!", NamedTextColor.RED);
            case INSUFFICIENT_STOCK_TO_WITHDRAW -> Component.text("This shop is out of stock!", NamedTextColor.RED);
            case INSUFFICIENT_STOCK_TO_DEPOSIT -> Component.text("You don't have enough items", NamedTextColor.RED);
            case OPERATION_NOT_ALLOWED, OPERATION_FAILED, NONE ->
                    Component.text("Something went wrong!", NamedTextColor.RED);
        };
    }

    private void destroyShopUI(final Player shopOwner, final Shop shop) {
        final var gui = Gui.normal()
                .setStructure("...123...")
                .addIngredient('.', new SimpleItem(new ItemBuilder(Material.AIR)))
                .addIngredient('1', new SimpleItem(itemStack(Material.GREEN_STAINED_GLASS_PANE, (it, meta) -> {
                    meta.displayName(Component.text("Destroy Shop", NamedTextColor.GREEN));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("This will destroy your items.", NamedTextColor.RED));
                    }});
                }).get(), c -> {
                    try {
                        api.repository().delete(shop);

                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.DESTROY);

                        shopOwner.closeInventory();
                    } catch (final Exception e) {
                        //TODO: explain to player what happened

                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BLOCKED);
                    }
                }))
                .addIngredient('2', new SimpleItem(itemStack(Material.COMMAND_BLOCK, (it, meta) -> {
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
                }).get()))
                .addIngredient('3', new SimpleItem(itemStack(Material.BARRIER, (it, meta) -> {
                    meta.displayName(Component.text("Cancel", NamedTextColor.RED));
                }).get(), c -> {
                    shopOwner.closeInventory();
                    api.sound().play(shopOwner.getUniqueId(), shop, Sounds.CANCEL);
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
                .addIngredient('s', new SuppliedItem(itemStack(Material.CHEST_MINECART, (it, meta) -> {
                    meta.displayName(Component.text("Deposit '", NamedTextColor.GOLD)
                            .append(item.displayName()) //TODO: reset?
                            .append(Component.text("'"))
                    );
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("+Shift for bulk deposit", NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("In stock: %d", shop.stock()), NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("(%d stacks)", shop.stock() / item.getMaxStackSize()), NamedTextColor.DARK_PURPLE));
                    }});
                }), c -> {
                    final var result = api.operations().deposit(shopOwner.getUniqueId(), shop, shop.quantity());

                    if (!result.success()) {
                        shopOwner.sendMessage(localize(result));
                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BLOCKED);
                    } else {
                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BUY_SELL_SUCCESS);
                    }

                    return true;
                }))
                .addIngredient('h', new SuppliedItem(itemStack(Material.HOPPER_MINECART, (it, meta) -> {
                    meta.displayName(Component.text("Withdraw '", NamedTextColor.GOLD)
                            .append(item.displayName()) //TODO: reset?
                            .append(Component.text("'"))
                    );
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("+Shift for bulk withdrawal", NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("In stock: %d", shop.stock()), NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("(%d stacks)", shop.stock() / item.getMaxStackSize()), NamedTextColor.DARK_PURPLE));
                    }});
                }), c -> {
                    final var result = api.operations().withdraw(shopOwner.getUniqueId(), shop, shop.quantity());

                    if (!result.success()) {
                        shopOwner.sendMessage(localize(result));
                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BLOCKED);
                    } else {
                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BUY_SELL_SUCCESS);
                    }

                    return true;
                }))
                .addIngredient('m', new SuppliedItem(itemStack(Material.MINECART, (it, meta) -> {
                    meta.displayName(Component.text("Change rate", NamedTextColor.GOLD));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("Amount per click: %d", shop.quantity()), NamedTextColor.DARK_PURPLE));
                    }});
                }), c -> {
                    shopOwner.sendMessage(Component.text("This feature is not available", NamedTextColor.RED));

                    return false;
                }))
                .addIngredient('.', new SimpleItem(new ItemBuilder(Material.AIR)))
                .addIngredient('i', new SimpleItem(new ItemBuilder(Bukkit.getItemFactory().createItemStack(shop.item()))))
                .addIngredient('c', new SimpleItem(itemStack(Material.CHEST, (it, meta) -> {
                    meta.displayName(Component.text("Link chest", NamedTextColor.GOLD));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("Link a chest for refilling!", NamedTextColor.GREEN));
                    }});
                }).get(), c -> {
                    shopOwner.sendMessage(Component.text("This feature is not available", NamedTextColor.RED));
                    api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BLOCKED);
                }))
                .addIngredient('p', new SimpleItem(itemStack(Material.COMMAND_BLOCK, (it, meta) -> {
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
                }).get()))
                .addIngredient('r', new SimpleItem(itemStack(Material.COMPARATOR, (it, meta) -> {
                    meta.displayName(Component.text("Modify Shop", NamedTextColor.GOLD));
                }).get(), c -> {
                    modifyShopUI(shopOwner, api.operations().wizardFor(shopOwner.getUniqueId()).useExisting(shop));
                    api.sound().play(shopOwner.getUniqueId(), shop, Sounds.NAVIGATION);
                }))
                .addIngredient('t', new SimpleItem(itemStack(Material.OAK_SIGN, (it, meta) -> {
                    meta.displayName(Component.text("View as customer", NamedTextColor.GOLD));
                }).get(), c -> {
                    clientShopUI(shopOwner, shop);
                    api.sound().play(shopOwner.getUniqueId(), shop, Sounds.NAVIGATION);
                }))
                .build();

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle("[Slabby] Owner") //TODO: translate
                .setGui(gui)
                .build();

        window.open();
    }

    private void modifyShopUI(final Player shopOwner, final ShopWizard wizard) {
        final var gui = Gui.normal()
                .setStructure("it.gry.nb")
                .addIngredient('.', new SimpleItem(new ItemStack(Material.AIR)))
                .addIngredient('i', new SimpleItem(Bukkit.getItemFactory().createItemStack(wizard.item())))
                .addIngredient('t', new SimpleItem(itemStack(Material.NAME_TAG, (it, meta) -> {
                    meta.displayName(Component.text("Sellers note", NamedTextColor.GREEN));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(wizard.note(), NamedTextColor.DARK_PURPLE));
                    }});
                }).get(), c -> {
                    wizard.state(ShopWizard.WizardState.AWAITING_NOTE);
                    shopOwner.closeInventory();
                    shopOwner.sendMessage(Component.text("Please enter your note."));
                    api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
                }))
                .addIngredient('g', new SimpleItem(itemStack(Material.GREEN_STAINED_GLASS_PANE, (it, meta) -> {
                    meta.displayName(Component.text("Buy price", NamedTextColor.GREEN));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("$%.2f", wizard.buyPrice()), NamedTextColor.DARK_PURPLE));
                        add(Component.text("Click to set", NamedTextColor.DARK_PURPLE));
                        add(Component.text("(-1 means not for sale)", NamedTextColor.DARK_PURPLE));
                    }});
                }).get(), c -> {
                    wizard.state(ShopWizard.WizardState.AWAITING_BUY_PRICE);
                    shopOwner.closeInventory();
                    shopOwner.sendMessage(Component.text("Please enter your buy price."));
                    api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
                }))
                .addIngredient('r', new SimpleItem(itemStack(Material.RED_STAINED_GLASS_PANE, (it, meta) -> {
                    meta.displayName(Component.text("Sell price", NamedTextColor.RED));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("$%.2f", wizard.sellPrice()), NamedTextColor.DARK_PURPLE));
                        add(Component.text("Click to set", NamedTextColor.DARK_PURPLE));
                        add(Component.text("(-1 means not buying)", NamedTextColor.DARK_PURPLE));
                    }});
                }).get(), c -> {
                    wizard.state(ShopWizard.WizardState.AWAITING_SELL_PRICE);
                    shopOwner.closeInventory();
                    shopOwner.sendMessage(Component.text("Please enter your sell price."));
                    api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
                }))
                .addIngredient('y', new SimpleItem(itemStack(Material.YELLOW_STAINED_GLASS_PANE, (it, meta) -> {
                    meta.displayName(Component.text("Quantity", NamedTextColor.YELLOW));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("Amount per transaction: %d", wizard.quantity()), NamedTextColor.DARK_PURPLE));
                        add(Component.text("Click to set", NamedTextColor.DARK_PURPLE));
                        add(Component.text("(Amount of items per buy/sell)", NamedTextColor.DARK_PURPLE));
                    }});
                }).get(), c -> {
                    wizard.state(ShopWizard.WizardState.AWAITING_QUANTITY);
                    shopOwner.closeInventory();
                    shopOwner.sendMessage(Component.text("Please enter your quantity."));
                    api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
                }))
                .addIngredient('n', new SimpleItem(itemStack(Material.NETHER_STAR, (it, meta) -> {
                    meta.displayName(Component.text("Confirm", NamedTextColor.GREEN));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("New Shop", NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("%s,%d,%d,%d", wizard.world(), wizard.x(), wizard.y(), wizard.z()), NamedTextColor.DARK_PURPLE));
                    }});
                }).get(), c -> {
                    try {
                        api.repository().transaction(() -> {
                            final var shopOpt = api.repository().shopAt(wizard.x(), wizard.y(), wizard.z(), wizard.world());

                            shopOpt.ifPresentOrElse(shop -> {
                                shop.buyPrice(wizard.buyPrice());
                                shop.sellPrice(wizard.sellPrice());
                                shop.quantity(wizard.quantity());
                                shop.note(wizard.note());
                                try {
                                    api.repository().update(shop);
                                } catch (final Exception ignored) {}
                            }, () -> {
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

                                try {
                                    api.repository().createOrUpdate(shop);

                                    shop.owners().add(api.repository().<ShopOwner.Builder>builder(ShopOwner.Builder.class)
                                            .uniqueId(shopOwner.getUniqueId())
                                            .share(100)
                                            .build());
                                } catch (final Exception ignored) {}
                            });

                            return null;
                        });
                        wizard.destroy();
                        shopOwner.closeInventory();
                        api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.MODIFY_SUCCESS);
                    } catch (final Exception e) {
                        api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.BLOCKED);
                        //TODO: notify player
                    }
                }))
                .addIngredient('b', new SimpleItem(itemStack(Material.BARRIER, (it, meta) -> {
                    meta.displayName(Component.text("Cancel", NamedTextColor.RED));
                }).get(), c -> {
                    wizard.destroy();
                    shopOwner.closeInventory();
                    api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.CANCEL);
                }))
                .build();

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle("[Slabby] Editing Shop") //TODO: translate
                .setGui(gui)
                .build();

        Bukkit.getScheduler().runTask((Slabby)api, window::open);
    }

}
