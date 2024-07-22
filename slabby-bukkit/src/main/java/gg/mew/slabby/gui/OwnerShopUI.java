package gg.mew.slabby.gui;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.permission.SlabbyPermissions;
import gg.mew.slabby.shop.Shop;
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
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.item.impl.SuppliedItem;
import xyz.xenondevs.invui.window.Window;

import static gg.mew.slabby.gui.GuiHelper.*;

import java.util.ArrayList;

@UtilityClass
public final class OwnerShopUI {

    public void open(final SlabbyAPI api, final Player shopOwner, final Shop shop) {
        final var itemStack = Bukkit.getItemFactory().createItemStack(shop.item());
        final var uniqueId = shopOwner.getUniqueId();
        
        final var gui = Gui.empty(9, 2);

        //TODO: allow bulk withdraw/deposit

        if (shop.stock() != null) {
            gui.setItem(0, 0, new SuppliedItem(itemStack(Material.CHEST_MINECART, (it, meta) -> {
                meta.displayName(api.messages().owner().deposit().title(itemStack.displayName()));
                meta.lore(new ArrayList<>() {{
                    add(api.messages().owner().deposit().bulk());
                    add(api.messages().owner().stock(shop.stock()));
                    add(api.messages().owner().stacks(shop.stock() / itemStack.getMaxStackSize()));
                }});
            }), c -> api.exceptionService().tryCatch(uniqueId, () -> api.operations().deposit(uniqueId, shop, shop.quantity()))));

            gui.setItem(1, 0, new SuppliedItem(itemStack(Material.HOPPER_MINECART, (it, meta) -> {
                meta.displayName(api.messages().owner().withdraw().title(itemStack.displayName()));
                meta.lore(new ArrayList<>() {{
                    add(api.messages().owner().withdraw().bulk());
                    add(api.messages().owner().stock(shop.stock()));
                    add(api.messages().owner().stacks(shop.stock() / itemStack.getMaxStackSize()));
                }});
            }), c -> api.exceptionService().tryCatch(uniqueId, () -> api.operations().withdraw(uniqueId, shop, shop.quantity()))));

            gui.setItem(2, 0, new SuppliedItem(itemStack(Material.MINECART, (it, meta) -> {
                meta.displayName(api.messages().owner().changeRate().title());
                meta.lore(new ArrayList<>() {{
                    add(api.messages().owner().changeRate().amount(shop.quantity()));
                }});
            }), c -> {
                //TODO: I could just use the wizard for this
                shopOwner.sendMessage(Component.text("This feature is not available", NamedTextColor.RED));

                return false;
            }));
        }

        api.permission().ifPermission(uniqueId, SlabbyPermissions.SHOP_LOGS, () -> {
            gui.setItem(0, 1, new SimpleItem(GuiHelper.itemStack(Material.BOOK, (it, meta) -> {
                meta.displayName(api.messages().owner().logs().title());
            }).get(), c -> LogShopUI.open(api, shopOwner, shop)));
        });

        gui.setItem(4, 0, new SimpleItem(new ItemBuilder(Bukkit.getItemFactory().createItemStack(shop.item()))));

        api.permission().ifPermission(uniqueId, SlabbyPermissions.SHOP_LINK, () -> {
            if (shop.stock() == null)
                return;

            gui.setItem(5, 0, new SuppliedItem(() -> {
                if (shop.hasInventory()) {
                    return itemStack(Material.ENDER_CHEST, (it, meta) -> {
                        meta.displayName(api.messages().owner().inventoryLink().cancel().title());
                    }).get();
                } else {
                    return itemStack(Material.CHEST, (it, meta) -> {
                        meta.displayName(api.messages().owner().inventoryLink().title());
                        meta.lore(new ArrayList<>() {{
                            add(api.messages().owner().inventoryLink().description());
                        }});
                    }).get();
                }
            }, c -> {
                if (shop.hasInventory()) {
                    return api.exceptionService().tryCatch(uniqueId, () -> api.operations().unlinkShop(uniqueId, shop));
                }

                api.operations()
                        .wizardFrom(uniqueId, shop)
                        .wizardState(ShopWizard.WizardState.AWAITING_INVENTORY_LINK);

                api.sound().play(uniqueId, shop, Sounds.AWAITING_INPUT);
                shopOwner.sendMessage(api.messages().owner().inventoryLink().message());
                gui.closeForAllViewers();

                return true;
            }));
        });

        gui.setItem(6, 0, commandBlock(api, shop, itemStack));

        gui.setItem(7, 0, new SimpleItem(itemStack(Material.COMPARATOR, (it, meta) -> {
            meta.displayName(api.messages().owner().modify().title());
        }).get(), c -> {
            ModifyShopUI.open(api, shopOwner, api.operations().wizardFrom(uniqueId, shop));
            api.sound().play(uniqueId, shop, Sounds.NAVIGATION);
        }));

        gui.setItem(8, 0, new SimpleItem(itemStack(Material.OAK_SIGN, (it, meta) -> {
            meta.displayName(api.messages().owner().customer().title());
        }).get(), c -> {
            ClientShopUI.open(api, shopOwner, shop);
            api.sound().play(uniqueId, shop, Sounds.NAVIGATION);
        }));

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle(new AdventureComponentWrapper(api.messages().owner().title()))
                .setGui(gui)
                .build();

        window.open();
    }

}
