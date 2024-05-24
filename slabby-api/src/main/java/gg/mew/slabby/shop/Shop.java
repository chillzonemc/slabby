package gg.mew.slabby.shop;

import gg.mew.slabby.audit.Auditable;

import java.util.Collection;

public interface Shop extends Auditable {

    String item();

    int x();

    int y();

    int z();

    String dimension();

    Double buyPrice();

    void buyPrice(final Double buyPrice);

    Double sellPrice();

    void sellPrice(final Double sellPrice);

    int quantity();

    void quantity(final int quantity);

    int stock();

    void stock(final int stock);

    String note();

    void note(final String note);

    String name();

    void name(final String name);

    Collection<ShopOwner> owners();

     interface Builder {

         Builder item(final String item);
         Builder x(final int x);
         Builder y(final int y);
         Builder z(final int z);
         Builder dimension(final String dimension);
         Builder buyPrice(final Double buyPrice);
         Builder sellPrice(final Double sellPrice);
         Builder quantity(final int quantity);
         Builder stock(final int stock);
         Builder note(final String note);
         Builder name(final String name);
         Shop build();

     }

}