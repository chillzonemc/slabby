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
        Component title();
        Component confirm();
        Component confirmWarning();
        Component cancel();
    }

    interface Log {
        Component title();
        Component player(final Component displayName);
    }

    interface Modify {
        Component title();
        Component sellersNote();
        Component requestNote();
        Component requestBuyPrice();
        Component requestSellPrice();
        Component requestQuantity();
        Component buyPriceTitle();
        Component buyPriceAmount(final double amount);
        Component clickToSet();
        Component notForSale();
        Component notBuying();
        Component sellPriceTitle();
        Component sellPriceAmount(final double amount);
        Component quantityTitle();
        Component amountPerTransaction(final int quantity);
        Component amountPerTransactionDescription();
        Component confirmTitle();
        Component confirmDescription();
        Component confirmLocation(final String world, final int x, final int y, final int z);
        Component cancelTitle();
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
