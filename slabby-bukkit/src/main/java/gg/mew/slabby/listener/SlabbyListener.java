package gg.mew.slabby.listener;

import gg.mew.slabby.SlabbyAPI;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@RequiredArgsConstructor
public final class SlabbyListener implements Listener {

    private final SlabbyAPI api;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(final PlayerInteractEvent event) {
        final var itemInHand = event.getItem();

        if (itemInHand != null) {
            final var configurationItem = Bukkit.getItemFactory().createItemStack(api.configuration().item());

            if (itemInHand.isSimilar(configurationItem)) {
                //TODO: start Wizard
            }
        }

        //TODO: else open ui
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryClick(final InventoryClickEvent event) {
 
    }

}
