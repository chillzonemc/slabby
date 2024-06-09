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

        final var gui = Gui.normal()
                .setStructure("shm.icprt")
                .addIngredient('s', new SuppliedItem(itemStack(Material.CHEST_MINECART, (it, meta) -> {
                    meta.displayName(Component.text("Deposit '", NamedTextColor.GOLD)
                            .append(itemStack.displayName()) //TODO: reset?
                            .append(Component.text("'"))
                    );
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("+Shift for bulk deposit", NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("In stock: %d", shop.stock()), NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("(%d stacks)", shop.stock() / itemStack.getMaxStackSize()), NamedTextColor.DARK_PURPLE));
                    }});
                }), c -> {
                    final var result = api.operations().deposit(shopOwner.getUniqueId(), shop, shop.quantity());

                    if (!result.success()) {
                        shopOwner.sendMessage(localize(result));
                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BLOCKED);
                    } else {
                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BUY_SELL_SUCCESS);
                    }

                    return true;
                }))
                .addIngredient('h', new SuppliedItem(itemStack(Material.HOPPER_MINECART, (it, meta) -> {
                    meta.displayName(Component.text("Withdraw '", NamedTextColor.GOLD)
                            .append(itemStack.displayName()) //TODO: reset?
                            .append(Component.text("'"))
                    );
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("+Shift for bulk withdrawal", NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("In stock: %d", shop.stock()), NamedTextColor.DARK_PURPLE));
                        add(Component.text(String.format("(%d stacks)", shop.stock() / itemStack.getMaxStackSize()), NamedTextColor.DARK_PURPLE));
                    }});
                }), c -> {
                    final var result = api.operations().withdraw(shopOwner.getUniqueId(), shop, shop.quantity());

                    if (!result.success()) {
                        shopOwner.sendMessage(localize(result));
                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BLOCKED);
                    } else {
                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BUY_SELL_SUCCESS);
                    }

                    return true;
                }))
                .addIngredient('m', new SuppliedItem(itemStack(Material.MINECART, (it, meta) -> {
                    meta.displayName(Component.text("Change rate", NamedTextColor.GOLD));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text(String.format("Amount per click: %d", shop.quantity()), NamedTextColor.DARK_PURPLE));
                    }});
                }), c -> {
                    shopOwner.sendMessage(Component.text("This feature is not available", NamedTextColor.RED));

                    return false;
                }))
                .addIngredient('.', new SimpleItem(new ItemBuilder(Material.AIR)))
                .addIngredient('i', new SimpleItem(new ItemBuilder(Bukkit.getItemFactory().createItemStack(shop.item()))))
                .addIngredient('c', new SimpleItem(itemStack(Material.CHEST, (it, meta) -> {
                    meta.displayName(Component.text("Link chest", NamedTextColor.GOLD));
                    meta.lore(new ArrayList<>() {{
                        add(Component.text("Link a chest for refilling!", NamedTextColor.GREEN));
                    }});
                }).get(), c -> {
                    if (!api.permission().hasPermission(shopOwner.getUniqueId(), SlabbyPermissions.SHOP_LINK)) {
                        api.sound().play(shopOwner.getUniqueId(), shop, Sounds.BLOCKED);
                        shopOwner.sendMessage(Component.text("You do not have permission!", NamedTextColor.RED));
                        return;
                    }
                    //TODO: allow for destruction of chest link
                    api.operations().wizardFor(shopOwner.getUniqueId())
                            .useExisting(shop)
                            .state(ShopWizard.WizardState.AWAITING_INVENTORY_LINK);
                    api.sound().play(shopOwner.getUniqueId(), shop, Sounds.AWAITING_INPUT);
                    shopOwner.sendMessage(Component.text("Please crouch and punch the chest you want to link.", NamedTextColor.GREEN));
                    shopOwner.closeInventory();
                }))
                .addIngredient('p', commandBlock(api, shop, itemStack))
                .addIngredient('r', new SimpleItem(itemStack(Material.COMPARATOR, (it, meta) -> {
                    meta.displayName(Component.text("Modify Shop", NamedTextColor.GOLD));
                }).get(), c -> {
                    ModifyShopUI.open(api, shopOwner, api.operations().wizardFor(shopOwner.getUniqueId()).useExisting(shop));
                    api.sound().play(shopOwner.getUniqueId(), shop, Sounds.NAVIGATION);
                }))
                .addIngredient('t', new SimpleItem(itemStack(Material.OAK_SIGN, (it, meta) -> {
                    meta.displayName(Component.text("View as customer", NamedTextColor.GOLD));
                }).get(), c -> {
                    ClientShopUI.open(api, shopOwner, shop);
                    api.sound().play(shopOwner.getUniqueId(), shop, Sounds.NAVIGATION);
                }))
                .build();

        final var window = Window.single()
                .setViewer(shopOwner)
                .setTitle("[Slabby] Owner") //TODO: translate
                .setGui(gui)
                .build();

        window.open();
    }

}
