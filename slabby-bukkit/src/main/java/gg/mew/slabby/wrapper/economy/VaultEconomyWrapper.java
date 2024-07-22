package gg.mew.slabby.wrapper.economy;

import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public final class VaultEconomyWrapper implements EconomyWrapper {

    private final Economy economy;

    @Override
    public boolean hasAccount(final UUID uniqueId) {
        return this.economy.hasAccount(Bukkit.getOfflinePlayer(uniqueId));
    }

    @Override
    public double balance(final UUID uniqueId) {
        return this.economy.getBalance(Bukkit.getOfflinePlayer(uniqueId));
    }

    @Override
    public boolean hasAmount(final UUID uniqueId, final double amount) {
        return this.economy.has(Bukkit.getOfflinePlayer(uniqueId), amount);
    }

    @Override
    public ActionResult withdraw(final UUID uniqueId, final double amount) {
        final var result = this.economy.withdrawPlayer(Bukkit.getOfflinePlayer(uniqueId), amount);

        return new ActionResult(result.amount, result.balance, result.transactionSuccess());
    }

    @Override
    public ActionResult deposit(final UUID uniqueId, final double amount) {
        final var result = this.economy.depositPlayer(Bukkit.getOfflinePlayer(uniqueId), amount);

        return new ActionResult(result.amount, result.balance, result.transactionSuccess());
    }

    @Override
    public Map<UUID, ActionResult> withdraw(final Map<UUID, Double> toWithdraw) {
        final var result = new HashMap<UUID, ActionResult>();

        for (final var entry : toWithdraw.entrySet()) {
            final var depositResult = this.deposit(entry.getKey(), entry.getValue());

            result.put(entry.getKey(), depositResult);
        }

        

        return result;
    }

    @Override
    public Map<UUID, ActionResult> deposit(Map<UUID, Double> toDeposit) {
        final var result = new HashMap<UUID, ActionResult>();

        return result;
    }

}
