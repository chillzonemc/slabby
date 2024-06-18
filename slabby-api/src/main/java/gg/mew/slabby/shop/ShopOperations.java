package gg.mew.slabby.shop;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface ShopOperations {

    Map<UUID, ShopWizard> wizards();

    ShopWizard wizard(final UUID uniqueId);
    ShopWizard wizardFrom(final UUID uniqueId, final Shop shop);

    void ifWizard(final UUID uniqueId, Consumer<ShopWizard> action);

    ShopOperationResult buy(final UUID uniqueId, final Shop shop);
    ShopOperationResult sell(final UUID uniqueId, final Shop shop);

    ShopOperationResult withdraw(final UUID uniqueId, final Shop shop, final int amount);
    ShopOperationResult deposit(final UUID uniqueId, final Shop shop, final int amount);

    Map<UUID, Double> splitCost(final double amount, final Shop shop);

    record ShopOperationResult(boolean success, Cause cause) {}

    enum Cause {
        INSUFFICIENT_BALANCE_TO_WITHDRAW,
        INSUFFICIENT_BALANCE_TO_DEPOSIT,

        INSUFFICIENT_STOCK_TO_WITHDRAW,
        INSUFFICIENT_STOCK_TO_DEPOSIT,

        OPERATION_NOT_ALLOWED,
        OPERATION_FAILED,
        OPERATION_NO_PERMISSION,

        NONE
    }

}
