package gg.mew.slabby.shop;

import java.util.UUID;

public interface ShopOperations {

    void buy(final UUID uniqueId, final Shop shop);
    void sell(final UUID uniqueId, final Shop shop);

    //TODO: OperationResult

}
