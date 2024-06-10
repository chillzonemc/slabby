package gg.mew.slabby.shop;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface ShopOperations {

    Map<UUID, ShopWizard> wizards();

    ShopWizard wizard(final UUID uniqueId);
    ShopWizard wizardFrom(final UUID uniqueId, final Shop shop);

    void ifWizard(final UUID uniqueId, final Consumer<ShopWizard> action);
    void ifWizardOrElse(final UUID uniqueId, final Consumer<ShopWizard> action, final Runnable orElse);

    ShopOperationResult buy(final UUID uniqueId, final Shop shop);
    ShopOperationResult sell(final UUID uniqueId, final Shop shop);

    ShopOperationResult withdraw(final UUID uniqueId, final Shop shop, final int amount);
    ShopOperationResult deposit(final UUID uniqueId, final Shop shop, final int amount);

    Map<UUID, Double> splitCost(final double amount, final Shop shop);

    void createOrUpdateShop(final UUID uniqueId, final ShopWizard wizard) throws Exception;

    void removeShop(final Shop shop) throws Exception;

    void removeAndSpawnDisplayItem(final Shop shop);

    //TODO: We need something better than this. Custom exception?
    record ShopOperationResult(boolean success, Cause cause) {}

    enum Cause {
        INSUFFICIENT_BALANCE_TO_BUY,
        INSUFFICIENT_BALANCE_TO_SELL,

        INSUFFICIENT_STOCK_TO_WITHDRAW,
        INSUFFICIENT_STOCK_TO_DEPOSIT,

        OPERATION_NOT_ALLOWED,
        OPERATION_FAILED,
        OPERATION_NO_PERMISSION,

        NONE
    }

}
