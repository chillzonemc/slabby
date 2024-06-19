package gg.mew.slabby.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import gg.mew.slabby.SlabbyAPI;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@CommandAlias("slabby")
public final class SlabbyCommand extends BaseCommand {

    private final SlabbyAPI api;

}
