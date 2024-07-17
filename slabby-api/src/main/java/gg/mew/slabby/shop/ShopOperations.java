package gg.mew.slabby.shop;

import gg.mew.slabby.exception.SlabbyException;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface ShopOperations {

    Map<UUID, ShopWizard> wizards();

    ShopWizard wizard(final UUID uniqueId);
    ShopWizard wizardFrom(final UUID uniqueId, final Shop shop);

    void ifWizard(final UUID uniqueId, final Consumer<ShopWizard> action);
    void ifWizardOrElse(final UUID uniqueId, final Consumer<ShopWizard> action, final Runnable orElse);

    void buy(final UUID uniqueId, final Shop shop) throws SlabbyException;
    void sell(final UUID uniqueId, final Shop shop) throws SlabbyException;

    void withdraw(final UUID uniqueId, final Shop shop, final int amount) throws SlabbyException;
    void deposit(final UUID uniqueId, final Shop shop, final int amount) throws SlabbyException;

    Map<UUID, Double> splitCost(final double amount, final Shop shop);

    void createOrUpdateShop(final UUID uniqueId, final ShopWizard wizard) throws SlabbyException;

    void removeShop(final Shop shop) throws SlabbyException;

    void removeAndSpawnDisplayItem(final Shop shop);


}
