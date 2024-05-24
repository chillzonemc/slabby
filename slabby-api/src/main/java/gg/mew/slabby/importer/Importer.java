package gg.mew.slabby.importer;

import gg.mew.slabby.SlabbyAPI;

@FunctionalInterface
public interface Importer {

    void onImport(final SlabbyAPI api);

}
