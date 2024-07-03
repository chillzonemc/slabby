package gg.mew.slabby.shop;

import gg.mew.slabby.SlabbyAPI;
import gg.mew.slabby.shop.log.ValueChanged;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Accessors(fluent = true, chain = true)
@Getter
public final class BukkitShopWizard implements ShopWizard {

    private final SlabbyAPI api;

    private final Map<ShopLog.Action, ValueChanged<?>> valueChanges = new HashMap<>();

    @Setter
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

    public BukkitShopWizard(final SlabbyAPI api) {
        this.api = api;

        this.note = this.api.configuration().defaults().note();
        this.buyPrice = this.api.configuration().defaults().buyPrice();
        this.sellPrice = this.api.configuration().defaults().sellPrice();
        this.quantity = this.api.configuration().defaults().quantity();
    }

    public BukkitShopWizard(final SlabbyAPI api, final Shop shop) {
        this.api = api;

        this.x = shop.x();
        this.y = shop.y();
        this.z = shop.z();
        this.world = shop.world();
        this.note = shop.note();
        this.buyPrice = shop.buyPrice();
        this.sellPrice = shop.sellPrice();
        this.quantity = shop.quantity();
        this.item = shop.item();
    }

    @Override
    public ShopWizard location(final int x, final int y, final int z, final String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        return this;
    }

    @Override
    public ShopWizard item(final String item) {
        this.item = item;
        return this;
    }

    @Override
    public ShopWizard note(final String note) {
        if (!this.note.equals(note))
            this.valueChanges.put(ShopLog.Action.NOTE_CHANGED, new ValueChanged.String(this.note, note));
        else
            this.valueChanges.remove(ShopLog.Action.NOTE_CHANGED);

        this.note = note;
        return this;
    }

    //TODO: handle null values

    @Override
    public ShopWizard buyPrice(final Double buyPrice) {
        if (!this.buyPrice.equals(buyPrice))
            this.valueChanges.put(ShopLog.Action.BUY_PRICE_CHANGED, new ValueChanged.Double(this.buyPrice, buyPrice));
        else
            this.valueChanges.remove(ShopLog.Action.BUY_PRICE_CHANGED);

        this.buyPrice = buyPrice;
        return this;
    }

    @Override
    public ShopWizard sellPrice(final Double sellPrice) {
        if (!this.sellPrice.equals(sellPrice))
            this.valueChanges.put(ShopLog.Action.SELL_PRICE_CHANGED, new ValueChanged.Double(this.sellPrice, sellPrice));
        else
            this.valueChanges.remove(ShopLog.Action.SELL_PRICE_CHANGED);

        this.sellPrice = sellPrice;
        return this;
    }

    @Override
    public ShopWizard quantity(final int quantity) {
        if (this.quantity != quantity)
            this.valueChanges.put(ShopLog.Action.QUANTITY_CHANGED, new ValueChanged.Int(this.quantity, quantity));
        else
            this.valueChanges.remove(ShopLog.Action.QUANTITY_CHANGED);

        this.quantity = quantity;
        return this;
    }

}
