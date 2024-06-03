package gg.mew.slabby.listener;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.shop.BukkitShopOperations;
import gg.mew.slabby.shop.ShopWizard;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Objects;

@RequiredArgsConstructor
public final class SlabbyListener implements Listener {

    private final SlabbyAPI api;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(final PlayerInteractEvent event) {
        final var itemInHand = event.getItem();

        if (itemInHand != null) {
            final var configurationItem = Bukkit.getItemFactory().createItemStack(api.configuration().item());

            if (itemInHand.isSimilar(configurationItem)) {
                final var gui = Gui.normal()
                        .setStructure(
                                "........."
                        )
                        .addIngredient('.', new SimpleItem(new ItemBuilder(Material.RED_STAINED_GLASS_PANE)))
                        .build();

                final var window = Window.single()
                        .setViewer(event.getPlayer())
                        .setTitle("[Slabby] New Shop")//TODO: translate
                        .setGui(gui)
                        .build();

                window.open();

                api.operations().wizardFor(event.getPlayer().getUniqueId()).state(ShopWizard.WizardState.SELECT_ITEM);
            }
        }

        //TODO: else open ui
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryClick(final InventoryClickEvent event) {
        if (api.operations().wizardExists(event.getWhoClicked().getUniqueId())) {
            final var item = Objects.requireNonNull(event.getCurrentItem()).getItemMeta().getAsString();
            api.operations().wizardFor(event.getWhoClicked().getUniqueId()).item(item);

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryClose(final InventoryCloseEvent event) {
        //TODO: be sure this doesn't trigger
        api.operations().destroyWizard(event.getPlayer().getUniqueId());
    }

}
