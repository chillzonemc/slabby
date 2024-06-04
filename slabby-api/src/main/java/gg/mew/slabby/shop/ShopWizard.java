package gg.mew.slabby.shop;

@SuppressWarnings("UnusedReturnValue")
public interface ShopWizard {

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

    ShopWizard state(final WizardState state);

    ShopWizard x(final int x);
    ShopWizard y(final int y);
    ShopWizard z(final int z);
    ShopWizard world(final String world);

    ShopWizard item(final String item);
    ShopWizard note(final String note);

    ShopWizard buyPrice(final Double buyPrice);
    ShopWizard sellPrice(final Double sellPrice);
    ShopWizard quantity(final int quantity);

    void destroy();

    enum WizardState {
        AWAITING_ITEM,
        AWAITING_NOTE,
        AWAITING_BUY_PRICE,
        AWAITING_SELL_PRICE,
        AWAITING_QUANTITY,
        AWAITING_CONFIRMATION;

        public boolean awaitingTextInput() {
            return this == AWAITING_NOTE
                    || this == AWAITING_BUY_PRICE
                    || this == AWAITING_SELL_PRICE
                    || this == AWAITING_QUANTITY;
        }

    }

}
