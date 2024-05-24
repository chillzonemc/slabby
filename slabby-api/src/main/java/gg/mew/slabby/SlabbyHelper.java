package gg.mew.slabby;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

/**
 * SlabbyHelper provides a static singleton instance to the Slabby API service. It is only supposed to be used in cases where dependency injection is impossible.
 */
@UtilityClass
@Accessors(fluent = true)
public final class SlabbyHelper {

    @Getter
    private static SlabbyAPI api;

    /**
     * Used by Slabby for initializing the static API singleton. You are not supposed to call this method!
     */
    public static void init(final SlabbyAPI api) {
        if (api == null || SlabbyHelper.api != null) {
            throw new UnsupportedOperationException("API already initialized");
        }
        SlabbyHelper.api = api;
    }

}
