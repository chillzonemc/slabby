package gg.mew.slabby.shop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(fluent = true, chain = true)
@Getter
@Setter
@RequiredArgsConstructor
public final class BukkitShopWizard implements ShopWizard {

    //TODO: proper way of handling defaults

    private final UUID uniqueId;
    private final BukkitShopOperations operations;

    private WizardState state;

    private int x;
    private int y;
    private int z;
    private String world;
    private String item;
    private String note = "Let's trade!";
    private Double buyPrice = (double) 0;
    private Double sellPrice = (double) 0;
    private int quantity;

    @Override
    public void destroy() {
        this.operations.destroyWizard(this.uniqueId);
    }
}
