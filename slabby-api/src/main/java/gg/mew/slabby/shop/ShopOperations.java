package gg.mew.slabby.shop;

import java.util.UUID;

public interface ShopOperations {

    // wizard(final UUID uniqueId);

    ShopOperationResult buy(final UUID uniqueId, final Shop shop);
    ShopOperationResult sell(final UUID uniqueId, final Shop shop);

    ShopOperationResult withdraw(final UUID uniqueId, final Shop shop, final int amount);
    ShopOperationResult deposit(final UUID uniqueId, final Shop shop, final int amount);

    record ShopOperationResult(boolean success, Cause cause) {}

    enum Cause {
        INSUFFICIENT_BALANCE_BUYER,
        INSUFFICIENT_BALANCE_SELLER,

        INSUFFICIENT_STOCK_SELLER,
        INSUFFICIENT_STOCK_BUYER,

        NONE
    }

}
