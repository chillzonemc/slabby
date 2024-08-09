package gg.mew.slabby;

import gg.mew.slabby.config.SlabbyMessages;
import gg.mew.slabby.config.SlabbyConfig;
import gg.mew.slabby.service.ExceptionService;
import gg.mew.slabby.shop.ShopOperations;
import gg.mew.slabby.shop.ShopRepository;
import gg.mew.slabby.wrapper.claim.ClaimWrapper;
import gg.mew.slabby.wrapper.economy.EconomyWrapper;
import gg.mew.slabby.wrapper.permission.PermissionWrapper;
import gg.mew.slabby.wrapper.serialization.SerializationWrapper;
import gg.mew.slabby.wrapper.sound.SoundWrapper;

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public interface SlabbyAPI {

    EconomyWrapper economy();

    PermissionWrapper permission();

    SoundWrapper sound();

    ClaimWrapper claim();

    SerializationWrapper serialization();

    ShopRepository repository();

    ShopOperations operations();

    SlabbyConfig configuration();

    LocalDateTime now();

    Date legacyNow();

    File directory();

    ExceptionService exceptionService();

    <T> T fromJson(final String json, final Class<? extends T> theClass);

    String toJson(final Object data);

    SlabbyMessages messages();

    Logger logger();

    void reload();

    boolean isAdminMode(final UUID uniqueId);

    boolean setAdminMode(final UUID uniqueId, final boolean adminMode);

}
