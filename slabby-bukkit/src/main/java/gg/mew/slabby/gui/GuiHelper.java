package gg.mew.slabby.gui;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.shop.Shop;
import gg.mew.slabby.shop.ShopOperations;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@UtilityClass
public final class GuiHelper {

    public Supplier<? extends ItemProvider> itemStack(final Material material, final BiConsumer<ItemStack, ItemMeta> action) {
        return () -> s -> {
            final var itemStack = new ItemStack(material);
            final var meta = itemStack.getItemMeta();

            action.accept(itemStack, meta);

            itemStack.setItemMeta(meta);

            return itemStack;
        };
    }

    public Component localize(final ShopOperations.ShopOperationResult result) {
        return switch (result.cause()) {
            case INSUFFICIENT_BALANCE_TO_WITHDRAW -> Component.text("You don't have enough funds!", NamedTextColor.RED);
            case INSUFFICIENT_BALANCE_TO_DEPOSIT ->
                    Component.text("The shop doesn't have enough funds!", NamedTextColor.RED);
            case INSUFFICIENT_STOCK_TO_WITHDRAW -> Component.text("This shop is out of stock!", NamedTextColor.RED);
            case INSUFFICIENT_STOCK_TO_DEPOSIT -> Component.text("You don't have enough items", NamedTextColor.RED);
            case OPERATION_NO_PERMISSION -> Component.text("You don't have permission to do this!", NamedTextColor.RED);
            case OPERATION_NOT_ALLOWED, OPERATION_FAILED, NONE ->
                    Component.text("Something went wrong!", NamedTextColor.RED);
        };
    }

    public SimpleItem commandBlock(final SlabbyAPI api, final Shop shop, final ItemStack itemStack) {
        return new SimpleItem(itemStack(Material.COMMAND_BLOCK, (it, meta) -> {
            meta.displayName(Component.text("Slabby Shop", NamedTextColor.GOLD));

            final var owners = shop.owners().stream().map(o -> Bukkit.getOfflinePlayer(o.uniqueId()).getName()).toArray(String[]::new);

            meta.lore(new ArrayList<>() {{
                add(Component.text(String.format("Owned by %s", String.join(", ", owners)), NamedTextColor.GREEN));
                add(Component.text("Selling: ", NamedTextColor.DARK_PURPLE).append(itemStack.displayName()));

                if (shop.buyPrice() != null) {
                    final var buyPrice = api.decimalFormat().format(shop.buyPrice());
                    final var buyPriceEach = shop.buyPrice() == 0 ? "0" : api.decimalFormat().format(shop.buyPrice() / shop.quantity());

                    add(Component.text(String.format("Buy %d for $%s ($%s each)", shop.quantity(), buyPrice, buyPriceEach), NamedTextColor.DARK_PURPLE));
                }

                if (shop.sellPrice() != null) {
                    final var sellPrice = api.decimalFormat().format(shop.sellPrice());
                    final var sellPriceEach = shop.sellPrice() == 0 ? "0" : api.decimalFormat().format(shop.sellPrice() / shop.quantity());

                    add(Component.text(String.format("Sell %d for $%s ($%s each)", shop.quantity(), sellPrice, sellPriceEach), NamedTextColor.DARK_PURPLE));
                }
            }});
        }).get());
    }

}
