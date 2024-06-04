package gg.mew.slabby.shop;

import gg.mew.slabby.SlabbyAPI;
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

    private final UUID uniqueId;
    private final SlabbyAPI api;

    private WizardState state;

    private int x;
    private int y;
    private int z;
    private String world;

    private String item;

    //TODO: Here, or use defaults in repository?
    private String note = this.api.configuration().defaults().note();
    private Double buyPrice = this.api.configuration().defaults().buyPrice();
    private Double sellPrice = this.api.configuration().defaults().sellPrice();
    private int quantity = this.api.configuration().defaults().quantity();

    @Override
    public void destroy() {
        this.api.operations().destroyWizard(this.uniqueId);
    }
}
