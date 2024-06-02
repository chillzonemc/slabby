package gg.mew.slabby.shop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(fluent = true)
@Getter
@Setter
@RequiredArgsConstructor
public class BukkitShopWizard implements ShopWizard {

    private final UUID uniqueId;
    private final BukkitShopOperations operations;

    private WizardState state;

    private int x;
    private int y;
    private int z;
    private String world;
    private String item;
    private String note;
    private Double buyPrice;
    private Double sellPrice;
    private int quantity;

    @Override
    public void destroy() {
        this.operations.destroyWizard(this.uniqueId);
    }
}
