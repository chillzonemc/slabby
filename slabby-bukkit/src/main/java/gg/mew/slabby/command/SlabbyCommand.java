package gg.mew.slabby.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import gg.mew.slabby.Slabby;
import org.bukkit.entity.Player;

@CommandAlias("slabby")
public final class SlabbyCommand extends BaseCommand {

    private final Slabby plugin;

    public SlabbyCommand(final Slabby plugin) {
        this.plugin = plugin;
    }

}
