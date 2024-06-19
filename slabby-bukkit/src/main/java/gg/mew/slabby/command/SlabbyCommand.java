package gg.mew.slabby.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.permission.SlabbyPermissions;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@CommandAlias("slabby")
public final class SlabbyCommand extends BaseCommand {

    private final SlabbyAPI api;

    @Subcommand("reload")
    @CommandPermission(SlabbyPermissions.ADMIN_RELOAD)
    private void onReload(final Player player) {
        api.reload();

        player.sendMessage("Slabby reloaded.");
    }

}
