package gg.mew.slabby;

import gg.mew.slabby.config.SlabbyConfig;
import gg.mew.slabby.service.ExceptionService;
import gg.mew.slabby.shop.ShopOperations;
import gg.mew.slabby.shop.ShopRepository;
import gg.mew.slabby.wrapper.economy.EconomyWrapper;
import gg.mew.slabby.wrapper.permission.PermissionWrapper;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;

public interface SlabbyAPI {

    EconomyWrapper economy();

    PermissionWrapper permission();

    ShopRepository repository();

    ShopOperations operations();

    SlabbyConfig configuration();

    LocalDateTime now();

    Date legacyNow();

    File directory();

    ExceptionService exceptionService();

}
