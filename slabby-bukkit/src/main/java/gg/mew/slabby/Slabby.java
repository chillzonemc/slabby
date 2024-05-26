package gg.mew.slabby;

import co.aikar.commands.PaperCommandManager;
import gg.mew.slabby.command.SlabbyCommand;
import gg.mew.slabby.config.BukkitSlabbyConfig;
import gg.mew.slabby.config.SlabbyConfig;
import gg.mew.slabby.listener.SlabbyListener;
import gg.mew.slabby.service.ExceptionService;
import gg.mew.slabby.shop.BukkitShopOperations;
import gg.mew.slabby.shop.SQLiteShopRepository;
import gg.mew.slabby.shop.ShopOperations;
import gg.mew.slabby.wrapper.economy.EconomyWrapper;
import gg.mew.slabby.wrapper.economy.VaultEconomyWrapper;
import gg.mew.slabby.wrapper.permission.BukkitPermissionWrapper;
import gg.mew.slabby.wrapper.permission.PermissionWrapper;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.dependency.DependsOn;
import org.bukkit.plugin.java.annotation.permission.Permissions;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Plugin(name = "Slabby", version = "1.0-SNAPSHOT")
@ApiVersion(ApiVersion.Target.v1_20)
@DependsOn(value = {
        @Dependency("Vault")
})
@Permissions(value = {

})
@Commands(value = {
        @Command(name = "slabby", desc = "Slabby's command for everything", permission = "slabby")
})
@Accessors(fluent = true)
public final class Slabby extends JavaPlugin implements SlabbyAPI {

    @Getter
    private SQLiteShopRepository repository;

    @Getter
    private EconomyWrapper economy;

    @Getter
    private final PermissionWrapper permission = new BukkitPermissionWrapper();

    @Getter
    private SlabbyConfig configuration;

    @Getter //TODO: Implement better.
    private final ExceptionService exceptionService = Throwable::printStackTrace;

    @Getter
    private final ShopOperations operations = new BukkitShopOperations(this);

    private PaperCommandManager commandManager;

    private final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(Path.of(getDataFolder().getAbsolutePath(), "config.yml"))
            .build();

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().warning("Error while setting up economy");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupConfig()) {
            getLogger().warning("Error while setting up config");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupRepository()) {
            getLogger().warning("Error while setting up repository");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new SlabbyCommand(this));

        SlabbyHelper.init(this);

        getServer().getPluginManager().registerEvents(new SlabbyListener(this), this);

        getServer().getServicesManager().register(SlabbyAPI.class, this, this, ServicePriority.Highest);
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregister(this);
        HandlerList.unregisterAll(this);
        if (this.repository != null) {
            this.repository.close();
        }
    }

    private boolean setupRepository() {
        try {
            this.repository = new SQLiteShopRepository(this);
            this.repository.initialize();
        } catch (SQLException e) {
            exceptionService().log(e);
            return false;
        }

        return true;
    }

    private boolean setupConfig() {
        try {
            final var root = loader.load();

            this.configuration = root.get(BukkitSlabbyConfig.class);
        } catch (ConfigurateException e) {
            exceptionService().log(e);
            return false;
        }

        return true;
    }

    private boolean setupEconomy() {
        if (!getServer().getPluginManager().isPluginEnabled("Vault"))
            return false;

        final var economyRegistration = getServer().getServicesManager().getRegistration(Economy.class);

        if (economyRegistration == null)
            return false;

        this.economy = new VaultEconomyWrapper(economyRegistration.getProvider());

        return true;
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public File directory() {
        return getDataFolder();
    }

}
