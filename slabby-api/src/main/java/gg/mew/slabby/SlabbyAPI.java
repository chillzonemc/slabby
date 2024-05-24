package gg.mew.slabby;

import gg.mew.slabby.config.SlabbyConfig;
import gg.mew.slabby.service.ExceptionService;
import gg.mew.slabby.shop.ShopRepository;
import gg.mew.slabby.wrapper.economy.EconomyWrapper;

import java.io.File;
import java.time.LocalDateTime;

public interface SlabbyAPI {

    EconomyWrapper economy();

    ShopRepository repository();

    SlabbyConfig configuration();

    LocalDateTime now();

    File directory();

    ExceptionService exceptionService();

}
