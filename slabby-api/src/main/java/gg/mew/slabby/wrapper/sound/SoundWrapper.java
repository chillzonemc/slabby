package gg.mew.slabby.wrapper.sound;

import gg.mew.slabby.shop.Shop;

import java.util.UUID;

public interface SoundWrapper {

    void play(final UUID uniqueId, final Shop shop, final Sounds sound);

    void play(final UUID uniqueId, final int x, final int y, final int z, final String world, final Sounds sound);

}
