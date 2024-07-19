package net.minecraft.core;

import javax.annotation.Nullable;

public interface IdMap<T> extends Iterable<T> {
    int DEFAULT = -1;

    int getId(T var1);

    @Nullable
    T byId(int var1);

    default T byIdOrThrow(int p_200958_) {
        T $$1 = this.byId(p_200958_);
        if ($$1 == null) {
            throw new IllegalArgumentException("No value with id " + p_200958_);
        } else {
            return $$1;
        }
    }

    int size();
}
