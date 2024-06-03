package gg.mew.slabby.listener;

import gg.mew.slabby.Slabby;
import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.shop.Shop;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor
public final class SlabbyListener implements Listener {

    private final SlabbyAPI api;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(final PlayerInteractEvent event) {
        final var itemInHand = event.getItem();
        final var block = event.getClickedBlock();

        if (!event.hasBlock() || block.getType() == Material.AIR)
            return;

        boolean tryOpen = true;

        if (itemInHand != null) {
            final var configurationItem = Bukkit.getItemFactory().createItemStack(api.configuration().item());

            if (itemInHand.isSimilar(configurationItem)) {
                //TODO: don't allow if shop already exists, plus other filters.

                tryOpen = false;

                newShopUI(event.getPlayer(), block);
            }
        }

        if (tryOpen) {
            final var shopOpt = api.repository().shopAt(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());

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

                wizard.item(String.format("%s%s", item.getType().getKey().asString(), item.getItemMeta().getAsString()));
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

            if (!wizard.state().awaitingInput())
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

    private ItemStack itemStack(final Material material, final Consumer<ItemStack> action) {
        final var itemStack = new ItemStack(material);

        action.accept(itemStack);

        return itemStack;
    }

    private void ownerShopUI(final Player shopOwner, final Shop shop) {
        final var item = Bukkit.getItemFactory().createItemStack(shop.item());
        final var gui = Gui.normal()
                .setStructure("shm.icprt")
                .addIngredient('s', new SimpleItem(itemStack(Material.CHEST_MINECART, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Deposit '", NamedTextColor.GOLD)
                            .appendSpace()
                            .append(item.displayName())//TODO: reset?
                            .append(Component.text("'"))
                    );
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("+Shift for bulk deposit", NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("In stock: %d", shop.stock()), NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("(%d stacks)", shop.stock() / item.getMaxStackSize()), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {

                }))
                .addIngredient('h', new SimpleItem(itemStack(Material.HOPPER_MINECART, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Withdraw '", NamedTextColor.GOLD)
                            .appendSpace()
                            .append(item.displayName())//TODO: reset?
                            .append(Component.text("'"))
                    );
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("+Shift for bulk withdrawal", NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("In stock: %d", shop.stock()), NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("(%d stacks)", shop.stock() / item.getMaxStackSize()), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {

                }))
                .addIngredient('m', new SimpleItem(itemStack(Material.MINECART, it -> {
                    final var meta = it.getItemMeta();
                    meta.displayName(Component.text("Change rate", NamedTextColor.GOLD));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("Amount per click: %d", shop.quantity()), NamedTextColor.DARK_PURPLE));
                    }});
                    it.setItemMeta(meta);
                }), c -> {

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
                        //TODO: buy, sell
                    }});

                    it.setItemMeta(meta);
                }), c -> {

                }))
                .addIngredient('r', new SimpleItem(itemStack(Material.COMPARATOR, it -> {

                }), c -> {

                }))
                .addIngredient('t', new SimpleItem(itemStack(Material.OAK_SIGN, it -> {

                }), c -> {

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
