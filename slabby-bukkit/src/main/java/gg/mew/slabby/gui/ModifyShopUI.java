package gg.mew.slabby.gui;

import gg.mew.slabby.Slabby;
import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.shop.Shop;
import gg.mew.slabby.shop.ShopOwner;
import gg.mew.slabby.shop.ShopWizard;
import gg.mew.slabby.wrapper.sound.Sounds;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import static gg.mew.slabby.gui.GuiHelper.*;

import java.util.ArrayList;

@UtilityClass
public final class ModifyShopUI {

    public void open(final SlabbyAPI api, final Player shopOwner, final ShopWizard wizard) {
        final var gui = Gui.empty(9, 1);

        gui.setItem(0, 0, new SimpleItem(Bukkit.getItemFactory().createItemStack(wizard.item())));
        gui.setItem(1, 0, new SimpleItem(itemStack(Material.NAME_TAG, (it, meta) -> {
            meta.displayName(Component.text("Sellers note", NamedTextColor.GREEN));
            meta.lore(new ArrayList<>() {{
                add(Component.text(wizard.note(), NamedTextColor.DARK_PURPLE));
            }});
        }).get(), c -> {
            wizard.state(ShopWizard.WizardState.AWAITING_NOTE);
            gui.closeForAllViewers();
            shopOwner.sendMessage(Component.text("Please enter your note."));
            api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
        }));

        gui.setItem(3, 0, new SimpleItem(itemStack(Material.GREEN_STAINED_GLASS_PANE, (it, meta) -> {
            meta.displayName(Component.text("Buy price", NamedTextColor.GREEN));
            meta.lore(new ArrayList<>() {{
                add(Component.text(String.format("$%.2f", wizard.buyPrice() == null ? -1d : wizard.buyPrice()), NamedTextColor.DARK_PURPLE));
                add(Component.text("Click to set", NamedTextColor.DARK_PURPLE));
                add(Component.text("(-1 means not for sale)", NamedTextColor.DARK_PURPLE));
            }});
        }).get(), c -> {
            wizard.state(ShopWizard.WizardState.AWAITING_BUY_PRICE);
            gui.closeForAllViewers();
            shopOwner.sendMessage(Component.text("Please enter your buy price."));
            api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
        }));

        gui.setItem(4, 0, new SimpleItem(itemStack(Material.RED_STAINED_GLASS_PANE, (it, meta) -> {
            meta.displayName(Component.text("Sell price", NamedTextColor.RED));
            meta.lore(new ArrayList<>() {{
                add(Component.text(String.format("$%.2f", wizard.sellPrice() == null ? -1d : wizard.sellPrice()), NamedTextColor.DARK_PURPLE));
                add(Component.text("Click to set", NamedTextColor.DARK_PURPLE));
                add(Component.text("(-1 means not buying)", NamedTextColor.DARK_PURPLE));
            }});
        }).get(), c -> {
            wizard.state(ShopWizard.WizardState.AWAITING_SELL_PRICE);
            gui.closeForAllViewers();
            shopOwner.sendMessage(Component.text("Please enter your sell price."));
            api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
        }));

        gui.setItem(5, 0, new SimpleItem(itemStack(Material.YELLOW_STAINED_GLASS_PANE, (it, meta) -> {
            meta.displayName(Component.text("Quantity", NamedTextColor.YELLOW));
            meta.lore(new ArrayList<>() {{
                add(Component.text(String.format("Amount per transaction: %d", wizard.quantity()), NamedTextColor.DARK_PURPLE));
                add(Component.text("Click to set", NamedTextColor.DARK_PURPLE));
                add(Component.text("(Amount of items per buy/sell)", NamedTextColor.DARK_PURPLE));
            }});
        }).get(), c -> {
            wizard.state(ShopWizard.WizardState.AWAITING_QUANTITY);
            gui.closeForAllViewers();
            shopOwner.sendMessage(Component.text("Please enter your quantity."));
            api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
        }));

        gui.setItem(7, 0, new SimpleItem(itemStack(Material.NETHER_STAR, (it, meta) -> {
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
                gui.closeForAllViewers();
                api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.MODIFY_SUCCESS);
            } catch (final Exception e) {
                api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.BLOCKED);
                //TODO: notify player
            }
        }));

        gui.setItem(8, 0, new SimpleItem(itemStack(Material.BARRIER, (it, meta) -> {
            meta.displayName(Component.text("Cancel", NamedTextColor.RED));
        }).get(), c -> {
            wizard.destroy();
            gui.closeForAllViewers();
            api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.CANCEL);
        }));

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle("[Slabby] Editing Shop") //TODO: translate
                .setGui(gui)
                .build();

        Bukkit.getScheduler().runTask((Slabby)api, window::open);
    }

}
