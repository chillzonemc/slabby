package gg.mew.slabby.service;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.exception.*;
import gg.mew.slabby.wrapper.sound.Sounds;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
public final class BukkitExceptionService implements ExceptionService {

    private final SlabbyAPI api;

    @Override
    public void logToConsole(final String message, final Exception exception) {
        api.logger().log(Level.SEVERE, message, exception);
    }

    @Override
    public void logToPlayer(final UUID uniqueId, final SlabbyException exception) {
        final var player = Objects.requireNonNull(Bukkit.getPlayer(uniqueId));

        player.sendMessage(switch (exception) {
            case InsufficientBalanceToBuyException ignored -> api.messages().client().buy().insufficientBalance();
            case InsufficientBalanceToSellException ignored -> api.messages().client().sell().insufficientBalance();
            case NoPermissionException ignored -> Bukkit.permissionMessage();
            case PlayerOutOfStockException ignored -> api.messages().owner().deposit().insufficientStock();
            case ShopOutOfStockException ignored -> api.messages().owner().withdraw().insufficientStock();
            case UnrecoverableException ignored -> Component.text("TODO(translate): A problem occurred while performing this action", NamedTextColor.RED);
            default -> Component.text("TODO(translate): Default");
        });

        api.sound().play(uniqueId, player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ(),
                player.getLocation().getWorld().getName(),
                Sounds.BLOCKED);
    }
}
