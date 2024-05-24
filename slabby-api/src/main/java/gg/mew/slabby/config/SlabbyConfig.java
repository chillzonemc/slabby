package gg.mew.slabby.config;

public interface SlabbyConfig {

    DisplayMode displayMode();

    int maxStock();

    String item();

    Database database();

    Defaults defaults();

    Restock restock();

    //TODO: minimum pricing for items.
    //TODO: permits
    //TODO: deny-listed items

    interface Database {

        String url();

    }

    interface Defaults {

        double buyPrice();

        double sellPrice();

        int quantity();

    }

    interface Restock {

        boolean canPunch();

        boolean bulk();

        Chests chests();

        interface Chests {

            boolean enabled();

            interface Hoppers {
                boolean enabled();
            }

        }

    }

    enum DisplayMode {

        QUANTITY,
        STACK,
        SINGLE,
        NONE

    }
}
