package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class IdMapper<T> implements IdMap<T> {
    protected int nextId;
    protected final Object2IntMap<T> tToId;
    protected final List<T> idToT;

    public IdMapper() {
        this(512);
    }

    public IdMapper(int p_122658_) {
        this.idToT = Lists.newArrayListWithExpectedSize(p_122658_);
        this.tToId = new Object2IntOpenCustomHashMap(p_122658_, IdMapper.identityStrategy());
        this.tToId.defaultReturnValue(-1);
    }

    public void addMapping(T p_122665_, int p_122666_) {
        this.tToId.put(p_122665_, p_122666_);

        while(this.idToT.size() <= p_122666_) {
            this.idToT.add(null);
        }

        this.idToT.set(p_122666_, p_122665_);
        if (this.nextId <= p_122666_) {
            this.nextId = p_122666_ + 1;
        }

    }

    public void add(T p_122668_) {
        this.addMapping(p_122668_, this.nextId);
    }

    public int getId(T p_122663_) {
        return this.tToId.getInt(p_122663_);
    }

    @Nullable
    public final T byId(int p_122661_) {
        return p_122661_ >= 0 && p_122661_ < this.idToT.size() ? this.idToT.get(p_122661_) : null;
    }

    public Iterator<T> iterator() {
        return Iterators.filter(this.idToT.iterator(), Objects::nonNull);
    }

    public boolean contains(int p_175381_) {
        return this.byId(p_175381_) != null;
    }

    public int size() {
        return this.tToId.size();
    }

    public static <K> Hash.Strategy<K> identityStrategy() {
        return (Hash.Strategy<K>) IdentityStrategy.INSTANCE;
    }

    static enum IdentityStrategy implements Hash.Strategy<Object> {
        INSTANCE;

        private IdentityStrategy() {
        }

        public int hashCode(Object p_137626_) {
            return System.identityHashCode(p_137626_);
        }

        public boolean equals(Object p_137623_, Object p_137624_) {
            return p_137623_ == p_137624_;
        }
    }
}
