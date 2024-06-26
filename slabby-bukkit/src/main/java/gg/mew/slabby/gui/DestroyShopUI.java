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
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import static gg.mew.slabby.gui.GuiHelper.*;

import java.util.ArrayList;

@UtilityClass
public final class DestroyShopUI {

    public void open(final SlabbyAPI api, final Player shopOwner, final Shop shop) {
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());

        final var gui = Gui.empty(9, 1);

        gui.setItem(3, 0, new SimpleItem(itemStack(Material.GREEN_STAINED_GLASS_PANE, (it, meta) -> {
            meta.displayName(Component.text("Destroy Shop", NamedTextColor.GREEN));
            meta.lore(new ArrayList<>() {{
                add(Component.text("This will destroy your items.", NamedTextColor.RED));
            }});
        }).get(), c -> {
            try {
                api.repository().delete(shop);
                api.sound().play(shopOwner.getUniqueId(), shop, Sounds.DESTROY);
                gui.closeForAllViewers();
            } catch (final Exception e) {
                //TODO: explain to uniqueId what happened
                api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BLOCKED);
            }
        }));

        gui.setItem(4, 0, commandBlock(api, shop, itemStack));

        gui.setItem(5, 0, new SimpleItem(itemStack(Material.BARRIER, (it, meta) -> {
            meta.displayName(Component.text("Cancel", NamedTextColor.RED));
        }).get(), c -> {
            gui.closeForAllViewers();
            api.sound().play(shopOwner.getUniqueId(), shop, Sounds.CANCEL);
        }));

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle("[Slabby] Destroy Shop")
                .setGui(gui)
                .build();

        window.open();
    }

}
