package gg.mew.slabby.shop;

import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public interface ShopWizard {

    <T> T id();

    WizardState state();
    int x();
    int y();
    int z();
    String world();
    String item();
    String note();
    Double buyPrice();
    Double sellPrice();
    int quantity();
    Map<ShopLog.Action, Object> valueChanges();

    ShopWizard state(final WizardState state);

    ShopWizard location(final int x, final int y, final int z, final String world);

    ShopWizard item(final String item);
    ShopWizard note(final String note);

    ShopWizard buyPrice(final Double buyPrice);
    ShopWizard sellPrice(final Double sellPrice);
    ShopWizard quantity(final int quantity);

    enum WizardState {
        AWAITING_ITEM,
        AWAITING_NOTE,
        AWAITING_BUY_PRICE,
        AWAITING_SELL_PRICE,
        AWAITING_QUANTITY,
        AWAITING_INVENTORY_LINK,
        AWAITING_LOCATION,
        AWAITING_CONFIRMATION;

        public boolean awaitingTextInput() {
            return this == AWAITING_NOTE
                    || this == AWAITING_BUY_PRICE
                    || this == AWAITING_SELL_PRICE
                    || this == AWAITING_QUANTITY;
        }

    }

}
