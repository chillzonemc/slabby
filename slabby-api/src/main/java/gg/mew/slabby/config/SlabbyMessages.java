package gg.mew.slabby.config;

import net.kyori.adventure.text.Component;

import java.util.Date;

public interface SlabbyMessages {

    Client client();
    Create create();
    Destroy destroy();
    Log log();
    Modify modify();
    Owner owner();

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

        interface Funds {
            Component title();
            Component balance(final double balance);
        }

        interface Note {
            Component title();
        }

        Buy buy();
        Sell sell();
        Funds funds();
        Component title();
        Note note();
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

        interface Confirm {
            Component title();
            Component description();
        }

        interface Cancel {
            Component title();
        }

        Component title();
        Confirm confirm();
        Cancel cancel();
    }

    interface Log {

        interface Buy {
            Component title();
            Component price(final double buyPrice);
            Component quantity(final int amount);
        }

        interface Sell {
            Component title();
            Component price(final double buyPrice);
            Component quantity(final int amount);
        }

        interface Deposit {
            Component amount(final int amount);
        }

        interface Withdraw {
            Component amount(final int amount);
        }

        interface LocationChanged {
            Component x();
            Component y();
            Component z();
            Component world();
        }

        interface LinkedInventory {
            Component x();
            Component y();
            Component z();
            Component world();
            Component removed();
        }

        interface BuyPriceChanged {}
        interface SellPriceChanged {}
        interface QuantityChanged {}
        interface NoteChanged {}
        interface NameChanged {}

        Component title();
        Component player(final Component displayName);
        Component date(final Date date);
    }

    interface Modify {

        interface Note {
            Component title();
            Component request();
        }

        interface Buy {
            Component title();
            Component amount(final double amount);
            Component notForSale();
            Component request();
        }

        interface Sell {
            Component title();
            Component amount(final double amount);
            Component request();
            Component notBuying();
        }

        interface Quantity {
            Component title();
            Component amount(final int quantity);
            Component description();
            Component request();
        }

        interface Confirm {
            Component title();
            Component description();
            Component location(final String world, final int x, final int y, final int z);
        }

        interface Cancel {
            Component title();
        }

        Component title();
        Component clickToSet();

        Note note();
        Buy buy();
        Sell sell();
        Quantity quantity();
        Confirm confirm();
        Cancel cancel();
    }

    interface Owner {

        interface Deposit {
            Component title(final Component displayName);
            Component bulk();
        }

        interface Withdraw {
            Component title(final Component displayName);
            Component bulk();
        }

        interface ChangeRate {
            Component title();
            Component amount(final int amount);
        }

        interface Logs {
            Component title();
        }

        interface Inventory {

            interface Cancel {
                Component title();
                Component message();
            }

            Component title();
            Component description();
            Component message();

            Cancel cancel();
        }

        interface Modify {
            Component title();
        }

        interface Customer {
            Component title();
        }

        Component title();
        Component stock(final int stock);
        Component stacks(final int stacks);

        Deposit deposit();
        Withdraw withdraw();
        ChangeRate changeRate();
        Logs logs();
        Inventory inventory();
        Modify modify();
        Customer customer();
    }

}
