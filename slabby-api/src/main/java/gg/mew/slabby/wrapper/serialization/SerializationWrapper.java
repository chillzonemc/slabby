package gg.mew.slabby.wrapper.serialization;

import gg.mew.slabby.exception.SlabbyException;

public interface SerializationWrapper {

    //TODO: catch these slabby exceptions

    String serialize(final Object obj) throws SlabbyException;

    <T> T deserialize(final String obj) throws SlabbyException;

}
