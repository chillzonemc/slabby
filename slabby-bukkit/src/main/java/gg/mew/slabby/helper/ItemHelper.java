package gg.mew.slabby.helper;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

@UtilityClass
public final class ItemHelper {

    public int countSimilar(final Inventory inventory, final ItemStack itemStack) {
        return Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(itemStack::isSimilar)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

}
