package gg.mew.slabby;

import com.google.gson.Gson;
import gg.mew.slabby.config.SlabbyMessages;
import gg.mew.slabby.config.SlabbyConfig;
import gg.mew.slabby.service.ExceptionService;
import gg.mew.slabby.shop.ShopOperations;
import gg.mew.slabby.shop.ShopRepository;
import gg.mew.slabby.wrapper.claim.ClaimWrapper;
import gg.mew.slabby.wrapper.economy.EconomyWrapper;
import gg.mew.slabby.wrapper.permission.PermissionWrapper;
import gg.mew.slabby.wrapper.sound.SoundWrapper;

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public interface SlabbyAPI {

    EconomyWrapper economy();

    PermissionWrapper permission();

    SoundWrapper sound();

    ClaimWrapper claim();

    ShopRepository repository();

    ShopOperations operations();

    SlabbyConfig configuration();

    LocalDateTime now();

    Date legacyNow();

    @Deprecated
    DecimalFormat decimalFormat();

    File directory();

    ExceptionService exceptionService();

    Gson gson();

    SlabbyMessages messages();

    void reload();

    boolean isAdminMode(final UUID uniqueId);

    boolean setAdminMode(final UUID uniqueId, final boolean adminMode);

}
