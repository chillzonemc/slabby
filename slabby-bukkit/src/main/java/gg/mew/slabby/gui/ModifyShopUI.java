package gg.mew.slabby.gui;

import gg.mew.slabby.Slabby;
import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.shop.Shop;
import gg.mew.slabby.shop.ShopLog;
import gg.mew.slabby.shop.ShopOwner;
import gg.mew.slabby.shop.ShopWizard;
import gg.mew.slabby.wrapper.sound.Sounds;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
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
            meta.displayName(api.messages().modify().sellersNote().title());
            meta.lore(new ArrayList<>() {{
                add(Component.text(wizard.note(), NamedTextColor.DARK_PURPLE));
            }});
        }).get(), c -> {
            wizard.state(ShopWizard.WizardState.AWAITING_NOTE);
            gui.closeForAllViewers();
            shopOwner.sendMessage(api.messages().modify().sellersNote().request());
            api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
        }));

        gui.setItem(3, 0, new SimpleItem(itemStack(Material.GREEN_STAINED_GLASS_PANE, (it, meta) -> {
            meta.displayName(api.messages().modify().buy().title());
            meta.lore(new ArrayList<>() {{
                final var buyPrice = wizard.buyPrice() == null ? -1d : wizard.buyPrice();
                add(api.messages().modify().buy().amount(buyPrice));
                add(api.messages().modify().clickToSet());
                add(api.messages().modify().buy().notForSale());
            }});
        }).get(), c -> {
            wizard.state(ShopWizard.WizardState.AWAITING_BUY_PRICE);
            gui.closeForAllViewers();
            shopOwner.sendMessage(api.messages().modify().buy().request());
            api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
        }));

        gui.setItem(4, 0, new SimpleItem(itemStack(Material.RED_STAINED_GLASS_PANE, (it, meta) -> {
            meta.displayName(api.messages().modify().sell().title());
            meta.lore(new ArrayList<>() {{
                final var sellPrice = wizard.sellPrice() == null ? -1d : wizard.sellPrice();
                add(api.messages().modify().sell().amount(sellPrice));
                add(api.messages().modify().clickToSet());
                add(api.messages().modify().sell().notBuying());
            }});
        }).get(), c -> {
            wizard.state(ShopWizard.WizardState.AWAITING_SELL_PRICE);
            gui.closeForAllViewers();
            shopOwner.sendMessage(api.messages().modify().sell().request());
            api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
        }));

        gui.setItem(5, 0, new SimpleItem(itemStack(Material.YELLOW_STAINED_GLASS_PANE, (it, meta) -> {
            meta.displayName(api.messages().modify().quantity().title());
            meta.lore(new ArrayList<>() {{
                add(api.messages().modify().quantity().amount(wizard.quantity()));
                add(api.messages().modify().clickToSet());
                add(api.messages().modify().quantity().description());
            }});
        }).get(), c -> {
            wizard.state(ShopWizard.WizardState.AWAITING_QUANTITY);
            gui.closeForAllViewers();
            shopOwner.sendMessage(api.messages().modify().quantity().request());
            api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.AWAITING_INPUT);
        }));

        gui.setItem(7, 0, new SimpleItem(itemStack(Material.NETHER_STAR, (it, meta) -> {
            meta.displayName(api.messages().modify().confirm().title());
            meta.lore(new ArrayList<>() {{
                add(api.messages().modify().confirm().description());
                add(api.messages().modify().confirm().location(wizard.world(), wizard.x(), wizard.y(), wizard.z()));
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

                        for (final var entry : wizard.valueChanges().entrySet()) {
                            final var log = api.repository().<ShopLog.Builder>builder(ShopLog.Builder.class)
                                    .action(entry.getKey())
                                    .uniqueId(shopOwner.getUniqueId())
                                    .serialized(entry.getValue())
                                    .build();

                            shop.logs().add(log);
                        }

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

                api.operations().wizards().remove(shopOwner.getUniqueId());

                gui.closeForAllViewers();
                api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.MODIFY_SUCCESS);
            } catch (final Exception e) {
                api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.BLOCKED);
                //TODO: notify uniqueId
            }
        }));

        gui.setItem(8, 0, new SimpleItem(itemStack(Material.BARRIER, (it, meta) -> {
            meta.displayName(api.messages().modify().cancel().title());
        }).get(), c -> {
            api.operations().wizards().remove(shopOwner.getUniqueId());
            gui.closeForAllViewers();
            api.sound().play(shopOwner.getUniqueId(), wizard.x(), wizard.y(), wizard.z(), wizard.world(), Sounds.CANCEL);
        }));

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle(new AdventureComponentWrapper(api.messages().modify().title()))
                .setGui(gui)
                .build();

        Bukkit.getScheduler().runTask((Slabby)api, window::open);
    }

}
