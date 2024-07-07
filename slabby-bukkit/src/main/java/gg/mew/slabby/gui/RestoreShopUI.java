package gg.mew.slabby.gui;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.audit.Auditable;
import gg.mew.slabby.shop.Shop;
import gg.mew.slabby.shop.ShopWizard;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
import java.util.UUID;

@UtilityClass
public final class RestoreShopUI {

    //TODO: localize
    public void open(final SlabbyAPI api, final Player viewer, final UUID uniqueId) {
        try {
            final var deletedShops = api.repository()
                    .shopsOf(uniqueId, Shop.State.DELETED)
                    .stream()
                    .sorted(Comparator.comparing(Auditable::createdOn, Comparator.reverseOrder()))
                    .map(it -> {
                        final var itemStack = Bukkit.getItemFactory().createItemStack(it.item());

                        final var owners = it.owners()
                                .stream()
                                .map(i -> Bukkit.getOfflinePlayer(i.uniqueId()).getName())
                                .toArray(String[]::new);

                        itemStack.lore(new ArrayList<>() {{
                            add(Component.text("Buy Price: $%.2f".formatted(it.buyPrice())));
                            add(Component.text("Sell Price: $%.2f".formatted(it.sellPrice())));
                            add(Component.text("Quantity: %d".formatted(it.quantity())));
                            add(Component.text("Stock: %d".formatted(it.stock())));
                            add(Component.text("Note: %s".formatted(it.note())));
                            add(Component.text("Owners: %s".formatted(String.join(", ", owners))));
                        }});

                        return (Item) new SimpleItem(itemStack, c -> {
                            api.operations()
                                    .wizardFrom(viewer.getUniqueId(), it)
                                    .state(Shop.State.ACTIVE)
                                    .wizardState(ShopWizard.WizardState.AWAITING_LOCATION);

                            //TODO: close ui
                        });
                    })
                    .toList();

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
                    .setContent(deletedShops)
                    .build();

            final var window = Window.single()
                    .setViewer(viewer)
                    .setTitle("[Slabby] Deleted Shops")
                    .setGui(gui)
                    .build();

            window.open();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
