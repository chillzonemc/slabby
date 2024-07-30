package gg.mew.slabby.helper;

import lombok.experimental.UtilityClass;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;

@UtilityClass
public final class BlockHelper {

    public boolean isSlabOrStair(final Block block) {
        return (block.getBlockData() instanceof Stairs stairs && stairs.getHalf() == Bisected.Half.TOP)|| block.getBlockData() instanceof Slab;
    }

}
