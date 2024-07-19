package yesman.epicfight.skill;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.IdMapper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.utils.ClearableIdMapper;
import yesman.epicfight.main.EpicFightMod;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SkillDataKey<T> extends ForgeRegistryEntry<SkillDataKey<?>> {
    private static final HashMultimap<Class<?>, SkillDataKey<?>> SKILL_DATA_KEYS = HashMultimap.create();
    private static final ResourceLocation CLASS_TO_DATA_KEYS = new ResourceLocation(EpicFightMod.MODID, "classtodatakeys");
    private static final ResourceLocation DATA_KEY_TO_ID = new ResourceLocation(EpicFightMod.MODID, "datakeytoid");

    private static class SkillDataKeyCallbacks implements IForgeRegistry.BakeCallback<SkillDataKey<?>>, IForgeRegistry.CreateCallback<SkillDataKey<?>>, IForgeRegistry.ClearCallback<SkillDataKey<?>> {
        static final SkillDataKeyCallbacks INSTANCE = new SkillDataKeyCallbacks();

        @Override
        @SuppressWarnings("unchecked")
        public void onBake(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
            final ClearableIdMapper<SkillDataKey<?>> skillDataKeyMap = owner.getSlaveMap(DATA_KEY_TO_ID, ClearableIdMapper.class);
            owner.forEach(skillDataKeyMap::add);
            final Map<Class<?>, Set<SkillDataKey<?>>> skillDataKeys = owner.getSlaveMap(CLASS_TO_DATA_KEYS, Map.class);

            SkillManager.getSkillRegistry().forEach((skill) -> {
                Class<?> skillClass = skill.getClass();
                Set<SkillDataKey<?>> dataKeySet = Sets.newHashSet();
                skillDataKeys.put(skillClass, dataKeySet);

                do {
                    if (SKILL_DATA_KEYS.containsKey(skillClass)) {
                        dataKeySet.addAll(SKILL_DATA_KEYS.get(skillClass));
                    }

                    skillClass = skillClass.getSuperclass();
                } while (Skill.class.isAssignableFrom(skillClass));
            });
        }

        @Override
        public void onCreate(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
            owner.setSlaveMap(CLASS_TO_DATA_KEYS, Maps.newHashMap());
            owner.setSlaveMap(DATA_KEY_TO_ID, new ClearableIdMapper<SkillDataKey<?>> (owner.getKeys().size()));
        }

        @Override
        public void onClear(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
            owner.getSlaveMap(CLASS_TO_DATA_KEYS, Map.class).clear();
            owner.getSlaveMap(DATA_KEY_TO_ID, ClearableIdMapper.class).clear();
        }
    }

    public static SkillDataKeyCallbacks getRegistryCallback() {
        return SkillDataKeyCallbacks.INSTANCE;
    }

    public static SkillDataKey<Integer> createIntKey(int defaultValue, boolean syncronizeTrackingPlayers, Class<?>... skillClass) {
        return createSkillDataKey((buffer, val) -> buffer.writeInt(val), (buffer) -> buffer.readInt(), defaultValue, syncronizeTrackingPlayers, skillClass);
    }

    public static SkillDataKey<Float> createFloatKey(float defaultValue, boolean syncronizeTrackingPlayers, Class<?>... skillClass) {
        return createSkillDataKey((buffer, val) -> buffer.writeFloat(val), (buffer) -> buffer.readFloat(), defaultValue, syncronizeTrackingPlayers, skillClass);
    }

    public static SkillDataKey<Double> createDoubleKey(double defaultValue, boolean syncronizeTrackingPlayers, Class<?>... skillClass) {
        return createSkillDataKey((buffer, val) -> buffer.writeDouble(val), (buffer) -> buffer.readDouble(), defaultValue, syncronizeTrackingPlayers, skillClass);
    }

    public static SkillDataKey<Boolean> createBooleanKey(boolean defaultValue, boolean syncronizeTrackingPlayers, Class<?>... skillClass) {
        return createSkillDataKey((buffer, val) -> buffer.writeBoolean(val), (buffer) -> buffer.readBoolean(), defaultValue, syncronizeTrackingPlayers, skillClass);
    }

    public static SkillDataKey<Vector3f> createVector3fKey(Vector3f defaultValue, boolean syncronizeTrackingPlayers, Class<?>... skillClass) {
        return createSkillDataKey((buffer, val) -> {
            buffer.writeFloat(val.x());
            buffer.writeFloat(val.y());
            buffer.writeFloat(val.z());
        }, (buffer) -> new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()), defaultValue, syncronizeTrackingPlayers, skillClass);
    }

    public static SkillDataKey<Vector3d> createVector3dKey(Vector3d defaultValue, boolean syncronizeTrackingPlayers, Class<?>... skillClass) {
        return createSkillDataKey((buffer, val) -> {
            buffer.writeDouble(val.x);
            buffer.writeDouble(val.y);
            buffer.writeDouble(val.z);
        }, (buffer) -> new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), defaultValue, syncronizeTrackingPlayers, skillClass);
    }

    public static <T> SkillDataKey<T> createSkillDataKey(BiConsumer<ByteBuf, T> encoder, Function<ByteBuf, T> decoder, T defaultValue, boolean syncronizeTrackingPlayers, Class<?>... skillClass) {
        SkillDataKey<T> key = new SkillDataKey<T>(encoder, decoder, defaultValue, syncronizeTrackingPlayers);

        for (Class<?> cls : skillClass) {
            SKILL_DATA_KEYS.put(cls, key);
        }

        return key;
    }

    @SuppressWarnings("unchecked")
    public static IdMapper<SkillDataKey<?>> getIdMap() {
        return SkillDataKeys.REGISTRY.getSlaveMap(DATA_KEY_TO_ID, IdMapper.class);
    }

    @SuppressWarnings("unchecked")
    public static Map<Class<?>, Set<SkillDataKey<?>>> getSkillDataKeyMap() {
        return SkillDataKeys.REGISTRY.getSlaveMap(CLASS_TO_DATA_KEYS, Map.class);
    }

    @SuppressWarnings("unchecked")
    public static SkillDataKey<Object> byId(int id) {
        return (SkillDataKey<Object>)getIdMap().byId(id);
    }

    private final BiConsumer<ByteBuf, T> encoder;
    private final Function<ByteBuf, T> decoder;
    private final T defaultValue;
    private final boolean syncronizeTrackingPlayers;

    public SkillDataKey(BiConsumer<ByteBuf, T> encoder, Function<ByteBuf, T> decoder, T defaultValue, boolean syncronizeTrackingPlayers) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.defaultValue = defaultValue;
        this.syncronizeTrackingPlayers = syncronizeTrackingPlayers;
    }

    public T readFromBuffer(ByteBuf buffer) {
        return this.decoder.apply(buffer);
    }

    public void writeToBuffer(ByteBuf buffer, T value) {
        this.encoder.accept(buffer, value);
    }

    public T defaultValue() {
        return this.defaultValue;
    }

    public int getId() {
        return getIdMap().getId(this);
    }

    public boolean syncronizeTrackingPlayers() {
        return this.syncronizeTrackingPlayers;
    }
}