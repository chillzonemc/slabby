package gg.mew.slabby.service;

@FunctionalInterface
public interface ExceptionService {

    void log(final Throwable throwable);

}
