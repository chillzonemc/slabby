package gg.mew.slabby.gui;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.audit.Auditable;
import gg.mew.slabby.shop.Shop;
import gg.mew.slabby.shop.log.IntValueChanged;
import gg.mew.slabby.shop.log.LinkedInventoryChanged;
import gg.mew.slabby.shop.log.Transaction;
import gg.mew.slabby.shop.log.ValueChanged;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@UtilityClass
public final class LogShopUI {

    //TODO: category menu

    public void open(final SlabbyAPI api, final Player shopOwner, final Shop shop) {
        final var items = shop.logs().stream().sorted(Comparator.comparing(Auditable::createdOn, Comparator.reverseOrder())).map(it -> {
            final var item = new ItemStack(Material.PAPER);
            final var meta = item.getItemMeta();

            meta.displayName(Component.text(it.action().name(), NamedTextColor.GOLD));

            meta.lore(new ArrayList<>() {{
                add(Component.text("Player: %s".formatted(Bukkit.getOfflinePlayer(it.uniqueId()).getName())));

                switch (it.action()) {
                    case BUY, SELL -> {
                        final var data = api.gson().fromJson(it.data(), Transaction.class);
                        add(Component.text("Price: $%s".formatted(data.price())));
                        add(Component.text("Quantity: %d".formatted(data.quantity())));
                    }
                    case DEPOSIT -> {
                        final var data = api.gson().fromJson(it.data(), IntValueChanged.class);
                        final var deposited = data.to() - data.from();
                        add(Component.text("Deposited: %s".formatted(api.decimalFormat().format(deposited))));
                    }
                    case WITHDRAW -> {
                        final var data = api.gson().fromJson(it.data(), IntValueChanged.class);
                        final var withdrew = data.from() - data.to();
                        add(Component.text("Withdrew: %s".formatted(api.decimalFormat().format(withdrew))));
                    }
                    case LINKED_INVENTORY_CHANGED -> {
                        final var data = api.gson().fromJson(it.data(), LinkedInventoryChanged.class);
                        if (data.isRemoved()) {
                            add(Component.text("Inventory Link removed"));
                        } else {
                            add(Component.text("X: %d".formatted(data.x())));
                            add(Component.text("Y: %d".formatted(data.y())));
                            add(Component.text("Z: %d".formatted(data.z())));
                            add(Component.text("World: %s".formatted(data.world())));
                        }
                    }
                    case LOCATION_CHANGED, BUY_PRICE_CHANGED, SELL_PRICE_CHANGED, QUANTITY_CHANGED, NOTE_CHANGED, NAME_CHANGED -> {
                        final var data = (ValueChanged<?>) api.gson().fromJson(it.data(), it.action().dataClass());
                        add(Component.text("From: %s".formatted(data.from().toString())));
                        add(Component.text("To: %s".formatted(data.to().toString())));
                    }
                    default -> add(Component.text(it.data()));
                }

                add(Component.text(it.createdOn().toString()));
            }});

            item.setItemMeta(meta);

            return (Item) new SimpleItem(item);
        }).collect(Collectors.toList());

        final var gui = PagedGui.items()
                .setStructure(
                        "X X X X X X X X X",
                        "X X X X X X X X X",
                        "X X X X X X X X X",
                        "X X X X X X X X X",
                        "X X X X X X X X X",
                        "# # # < # > # # #")
                .addIngredient('X', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', new PageItem(false) {
                    @Override
                    public ItemProvider getItemProvider(PagedGui<?> pagedGui) {
                        return new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("Previous Page");
                    }
                })
                .addIngredient('>', new PageItem(true) {
                    @Override
                    public ItemProvider getItemProvider(PagedGui<?> pagedGui) {
                        return new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("Next Page");
                    }
                })
                .setContent(items)
                .build();

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle("[Slabby] Logs")
                .setGui(gui)
                .build();

        window.open();
    }

}
