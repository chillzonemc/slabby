package gg.mew.slabby.gui;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.SlabbyHelper;
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

    //TODO: Cleanup
    public Component localize(final ShopOperations.ShopOperationResult result) {
        return switch (result.cause()) {
            case INSUFFICIENT_BALANCE_TO_BUY -> SlabbyHelper.api().messages().client().buy().insufficientBalance();
            case INSUFFICIENT_BALANCE_TO_SELL -> SlabbyHelper.api().messages().client().sell().insufficientBalance();
            case INSUFFICIENT_STOCK_TO_WITHDRAW -> SlabbyHelper.api().messages().owner().withdraw().insufficientStock();
            case INSUFFICIENT_STOCK_TO_DEPOSIT -> SlabbyHelper.api().messages().owner().deposit().insufficientStock();
            case OPERATION_NO_PERMISSION -> Bukkit.permissionMessage();
            //TODO: Technically never happens, edge case.
            case OPERATION_FAILED, NONE -> Component.text("Something went wrong!", NamedTextColor.RED);
            //TODO: Technically never happens, edge case.
            case OPERATION_NOT_ALLOWED -> Bukkit.permissionMessage();
        };
    }

    public SimpleItem commandBlock(final SlabbyAPI api, final Shop shop, final ItemStack itemStack) {
        return new SimpleItem(itemStack(Material.COMMAND_BLOCK, (it, meta) -> {
            meta.displayName(api.messages().commandBlock().title());

            final var owners = shop.owners()
                    .stream()
                    //TODO: use player display name
                    .map(o -> Bukkit.getOfflinePlayer(o.uniqueId()).getName())
                    .toArray(String[]::new);

            meta.lore(new ArrayList<>() {{
                add(api.messages().commandBlock().owners(owners));
                add(api.messages().commandBlock().selling(itemStack.displayName()));

                if (shop.buyPrice() != null) {
                    final var buyPriceEach = shop.buyPrice() == 0 ? 0 : shop.buyPrice() / shop.quantity();
                    add(api.messages().commandBlock().buyPrice(shop.quantity(), shop.buyPrice(), buyPriceEach));
                }

                if (shop.sellPrice() != null) {
                    final var sellPriceEach = shop.sellPrice() == 0 ? 0 : shop.sellPrice() / shop.quantity();
                    add(api.messages().commandBlock().sellPrice(shop.quantity(), shop.sellPrice(), sellPriceEach));
                }
            }});
        }).get());
    }

}
