package gg.mew.slabby.config;

import net.kyori.adventure.text.Component;

public interface SlabbyMessages {

    Client client();
    Create create();
    Destroy destroy();

    CommandBlock commandBlock();

    interface Client {

        interface Buy {
            Component title(final Component displayName, final int quantity);
            Component price(final double price);
            Component stock(final int stock);
            Component stacks(final int stacks);
            Component message(final Component displayName, final int quantity, final double buyPrice);
        }

        interface Sell {
            Component title(final Component displayName, final int quantity);
            Component price(final double price);
            Component stock(final int stock);
            Component stacks(final int stacks);
            Component message(final Component displayName, final int quantity, final double sellPrice);
        }

        Buy buy();
        Sell sell();
        Component title();
        Component sellersNote();
        Component currentFundsTitle();
        Component currentFundsBalance(final double balance);
    }

    interface CommandBlock {
        Component title();
        Component owners(final String[] names);
        Component selling(final Component displayName);
        Component buyPrice(final int quantity, final double buyPrice, final double eachPrice);
        Component sellPrice(final int quantity, final double sellPrice, final double eachPrice);
    }

    interface Create {
        Component title();
    }

    interface Destroy {
        Component title();
        Component confirm();
        Component confirmWarning();
        Component cancel();
    }

}
