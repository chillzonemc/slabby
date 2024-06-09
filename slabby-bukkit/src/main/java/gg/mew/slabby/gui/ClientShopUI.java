package gg.mew.slabby.gui;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.shop.Shop;
import gg.mew.slabby.wrapper.sound.Sounds;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AutoUpdateItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.item.impl.SuppliedItem;
import xyz.xenondevs.invui.window.Window;

import static gg.mew.slabby.gui.GuiHelper.*;

import java.util.ArrayList;

@UtilityClass
public final class ClientShopUI {

    public void open(final SlabbyAPI api, final Player client, final Shop shop) {
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
                .addIngredient('6', commandBlock(api, shop, item))
                .build();

        final var window = Window.single()
                .setViewer(client)
                .setTitle("[Slabby] Client")
                .setGui(gui)
                .build();

        window.open();
    }

}
