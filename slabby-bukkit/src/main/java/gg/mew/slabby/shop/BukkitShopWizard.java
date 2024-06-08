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
public final class BukkitShopWizard implements ShopWizard {

    private final UUID uniqueId;
    private final SlabbyAPI api;

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

    public BukkitShopWizard(final UUID uniqueId, final SlabbyAPI api) {
        this.uniqueId = uniqueId;
        this.api = api;

        //TODO: Here, or use defaults in repository?
        this.note = this.api.configuration().defaults().note();
        this.buyPrice = this.api.configuration().defaults().buyPrice();
        this.sellPrice = this.api.configuration().defaults().sellPrice();
        this.quantity = this.api.configuration().defaults().quantity();
    }

    @Override
    public void destroy() {
        this.api.operations().destroyWizard(this.uniqueId);
    }
}
