package gg.mew.slabby.service;

import gg.mew.slabby.exception.SlabbyException;

import java.util.UUID;

public interface ExceptionService {

    void logToConsole(final String message, final Exception exception);

    void logToPlayer(final UUID uniqueId, final SlabbyException exception);

}
