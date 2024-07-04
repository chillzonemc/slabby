package gg.mew.slabby.wrapper.claim;

import lombok.RequiredArgsConstructor;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.type.Flags;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class LandsClaimWrapper implements ClaimWrapper {

    private final LandsIntegration lands;

    @Override
    public boolean canCreateShop(final UUID uniqueId, final int x, final int y, final int z, final String world) {
        final var bukkitWorld = Bukkit.getWorld(world);
        final var landWorld = lands.getWorld(Objects.requireNonNull(bukkitWorld));

        //TODO: Natural spawn protection? This is an issue beside just lands.

        if (landWorld == null)
            return true;

        return landWorld.hasRoleFlag(uniqueId, new Location(bukkitWorld, x, y, z), Flags.BLOCK_PLACE);
    }

}
