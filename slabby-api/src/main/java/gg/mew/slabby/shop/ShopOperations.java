package gg.mew.slabby.shop;

import java.util.UUID;

public interface ShopOperations {

    ShopWizard wizardFor(final UUID uniqueId);
    boolean wizardExists(final UUID uniqueId);
    void destroyWizard(final UUID uniqueId);

    ShopOperationResult buy(final UUID uniqueId, final Shop shop);
    ShopOperationResult sell(final UUID uniqueId, final Shop shop);

    ShopOperationResult withdraw(final UUID uniqueId, final Shop shop, final int amount);
    ShopOperationResult deposit(final UUID uniqueId, final Shop shop, final int amount);

    record ShopOperationResult(boolean success, Cause cause) {}

    enum Cause {
        INSUFFICIENT_BALANCE_WITHDRAW,
        INSUFFICIENT_BALANCE_DEPOSIT,

        INSUFFICIENT_STOCK_WITHDRAW,
        INSUFFICIENT_STOCK_DEPOSIT,

        NONE
    }

}
