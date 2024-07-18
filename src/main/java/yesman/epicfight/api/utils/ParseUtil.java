package yesman.epicfight.api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import yesman.epicfight.api.utils.math.Vec3f;

public class ParseUtil {
    public static int[] toIntArray(JsonArray array) {
        IntList result = new IntArrayList();

        for (JsonElement je : array) {
            result.add(je.getAsInt());
        }

        return result.toIntArray();
    }

    public static float[] toFloatArray(JsonArray array) {
        FloatList result = new FloatArrayList();

        for (JsonElement je : array) {
            result.add(je.getAsFloat());
        }

        return result.toFloatArray();
    }

    public static JsonObject arrayToJsonObject(float[] array, int stride) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("stride", stride);
        jsonObject.addProperty("count", array.length / stride);
        JsonArray jsonArray = new JsonArray();

        for (float element : array) {
            jsonArray.add(element);
        }

        jsonObject.add("array", jsonArray);

        return jsonObject;
    }

    public static JsonObject arrayToJsonObject(int[] array, int stride) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("stride", stride);
        jsonObject.addProperty("count", array.length / stride);
        JsonArray jsonArray = new JsonArray();

        for (int element : array) {
            jsonArray.add(element);
        }

        jsonObject.add("array", jsonArray);

        return jsonObject;
    }

    public static Vec3f toVector3f(JsonArray array) {
        float[] result = toFloatArray(array);

        if (result.length < 3) {
            throw new IllegalArgumentException("Requires more than 3 elements to convert into 3d vector.");
        }

        return new Vec3f(result[0], result[1], result[2]);
    }

    public static Vector3d toVector3d(JsonArray array) {
        DoubleList result = new DoubleArrayList();

        for (JsonElement je : array) {
            result.add(je.getAsDouble());
        }

        if (result.size() < 3) {
            throw new IllegalArgumentException("Requires more than 3 elements to convert into 3d vector.");
        }

        return new Vector3d(result.getDouble(0), result.getDouble(1), result.getDouble(2));
    }

    public static AttributeModifier toAttributeModifier(CompoundNBT tag) {
        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(tag.getString("operation").toUpperCase(Locale.ROOT));

        return new AttributeModifier(UUID.fromString(tag.getString("uuid")), tag.getString("name"), tag.getDouble("amount"), operation);
    }

    public static <T> String nullOrToString(T obj, Function<T, String> toString) {
        return obj == null ? "" : toString.apply(obj);
    }

    public static <T, V> V nullOrApply(T obj, Function<T, V> apply) {
        if (obj == null) {
            return null;
        }

        try {
            return apply.apply(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public static String snakeToSpacedCamel(Object obj) {
        if (obj == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean upperNext = true;
        String toStr = obj.toString().toLowerCase(Locale.ROOT);

        for (String sElement : toStr.split("")) {
            if (upperNext) {
                sElement = sElement.toUpperCase(Locale.ROOT);
                upperNext = false;
            }

            if ("_".equals(sElement)) {
                upperNext = true;
                sb.append(" ");
            } else {
                sb.append(sElement);
            }
        }

        return sb.toString();
    }

    public static boolean compareNullables(@Nullable Object obj1, @Nullable Object obj2) {
        if (obj1 == null) {
            if (obj2 == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return obj1.equals(obj2);
        }
    }

    public static String nullParam(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    public static <T extends IForgeRegistryEntry<T>> String getRegistryName(T obj, IForgeRegistry<T> registry) {
        return obj == null ? "" : registry.getKey(obj).toString();
    }

    public static <T extends INBT> T getOrSupply(CompoundNBT compTag, String name, Supplier<T> tag) {
        return getOrDefaultTag(compTag, name, tag.get());
    }

    @SuppressWarnings("unchecked")
    public static <T extends INBT> T getOrDefaultTag(CompoundNBT compTag, String name, T tag) {
        if (compTag.contains(name)) {
            return (T)compTag.get(name);
        }

        compTag.put(name, tag);

        return tag;
    }

    public static <T> boolean isParsableAllowingMinus(String s, Function<String, T> parser) {
        if ("-".equals(s)) {
            return true;
        }

        try {
            parser.apply(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static <T> boolean isParsable(String s, Function<String, T> parser) {
        try {
            parser.apply(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static <T> String valueOfOmittingType(T value) {
        try {
            return String.valueOf(value).replaceAll("[df]", "");
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static <T> T parseOrGet(String value, Function<String, T> parseFunction, T defaultValue) {
        try {
            return parseFunction.apply(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static <K, V> Set<Pair<K, V>> mapEntryToPair(Set<Map.Entry<K, V>> entrySet) {
        return entrySet.stream().map((entry) -> Pair.of(entry.getKey(), entry.getValue())).collect(Collectors.toSet());
    }

    public static <T> List<T> remove(Collection<T> collection, T object) {
        List<T> copied = new ArrayList<> (collection);
        copied.remove(object);
        return copied;
    }

    public static <T extends Enum<T>> T enumValueOfOrNull(Class<T> enumCls, String enumName) {
        try {
            return Enum.valueOf(enumCls, enumName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    public static JsonObject convertToJsonObject(CompoundNBT compoundtag) {
        JsonObject root = CompoundNBT.CODEC.encodeStart(JsonOps.INSTANCE, compoundtag).get().left().get().getAsJsonObject();

        for (String entry : compoundtag.getAllKeys()) {
            INBT tag = compoundtag.get(entry);
            if (tag instanceof ByteNBT byteTag && (byteTag.getAsByte() == 0 || byteTag.getAsByte() == 1)) {
                root.remove(entry);
                root.addProperty(entry, byteTag.getAsByte() == 1);
            }
        }

        return root;
    }

    private ParseUtil() {}
}