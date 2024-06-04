package gg.mew.slabby.invui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.item.Click;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.function.Consumer;

public final class ReloadableItem extends SimpleItem {

    public ReloadableItem(final @NotNull ItemProvider itemProvider, final @Nullable Consumer<@NotNull Click> clickHandler) {
        super(itemProvider, clickHandler);
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        super.handleClick(clickType, player, event);

        notifyWindows();
    }

}
