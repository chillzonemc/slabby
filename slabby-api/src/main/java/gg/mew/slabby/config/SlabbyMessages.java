package gg.mew.slabby.config;

import net.kyori.adventure.text.Component;

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

        interface SellersNote {
            Component title();
        }

        Buy buy();
        Sell sell();
        Funds funds();
        Component title();
        SellersNote sellersNote();
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
        Component title();
        Component player(final Component displayName);
    }

    interface Modify {

        interface SellersNote {
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

        SellersNote sellersNote();
        Buy buy();
        Sell sell();
        Quantity quantity();
        Confirm confirm();
        Cancel cancel();
    }

    interface Owner {
        Component title();
        Component depositTitle(final Component displayName);
        Component shiftBulkDeposit();
        Component inStock(final int stock);
        Component stacks(final int stacks);
        Component withdrawTitle(final Component displayName);
        Component shiftBulkWithdraw();
        Component changeRateTitle();
        Component amountPerClick(final int amount);
        Component logsTitle();
        Component cancelChestLinkTitle();
        Component cancelChestLinkingMessage();
        Component chestLinkTitle();
        Component chestLinkDescription();
        Component chestLinkMessage();
        Component modifyShopTitle();
        Component viewAsCustomerTitle();
    }

}
