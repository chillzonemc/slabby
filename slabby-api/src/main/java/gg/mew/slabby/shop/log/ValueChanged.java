package gg.mew.slabby.shop.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

//TODO: We can't use generics with serialization
@Accessors(fluent = true, chain = false)
@Getter
@Setter
@RequiredArgsConstructor
public abstract class ValueChanged<T> {
    private final T from;
    private final T to;
}
