package gg.mew.slabby.wrapper.economy;

import java.util.Map;
import java.util.UUID;

public interface EconomyWrapper {

    boolean hasAccount(final UUID uniqueId);

    double balance(final UUID uniqueId);

    boolean hasAmount(final UUID uniqueId, final double amount);

    ActionResult withdraw(final UUID uniqueId, final double amount);

    ActionResult deposit(final UUID uniqueId, final double amount);

    record ActionResult(double amount, double balance, boolean success) {}

    Map<UUID, ActionResult> withdraw(final Map<UUID, Double> toWithdraw);
    Map<UUID, ActionResult> deposit(final Map<UUID, Double> toDeposit);

}
