package gg.mew.slabby.shop;

public interface ShopWizard {

    WizardState state();
    void state(final WizardState state);

    void x(final int x);
    void y(final int y);
    void z(final int z);
    void world(final String world);

    void item(final String item);
    void note(final String note);

    void buyPrice(final Double buyPrice);
    void sellPrice(final Double sellPrice);
    void quantity(final int quantity);

    void destroy();

    enum WizardState {
        SELECT_ITEM,
        CONFIRMED
    }

}
