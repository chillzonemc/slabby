package gg.mew.slabby;

import gg.mew.slabby.config.SlabbyConfig;
import gg.mew.slabby.service.ExceptionService;
import gg.mew.slabby.shop.Shop;
import gg.mew.slabby.shop.ShopOperations;
import gg.mew.slabby.shop.ShopRepository;
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

    ShopRepository repository();

    ShopOperations operations();

    SlabbyConfig configuration();

    LocalDateTime now();

    Date legacyNow();

    DecimalFormat decimalFormat();

    File directory();

    ExceptionService exceptionService();

}
