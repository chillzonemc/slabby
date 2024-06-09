package gg.mew.slabby.gui;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.shop.ShopWizard;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

@UtilityClass
public final class CreateShopUI {

    public void open(final SlabbyAPI api, final Player shopOwner, final Block block) {
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

}
