package yesman.epicfight.api.utils;

import java.util.List;
import java.util.Locale;

import net.minecraft.util.math.vector.Vector3d;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import yesman.epicfight.api.utils.math.Vec3f;

public class ParseUtil {
    public static int[] toIntArray(JsonArray array) {
        List<Integer> result = Lists.newArrayList();

        for (JsonElement je : array) {
            result.add(je.getAsInt());
        }

        return ArrayUtils.toPrimitive(result.toArray(new Integer[0]));
    }

    public static float[] toFloatArray(JsonArray array) {
        List<Float> result = Lists.newArrayList();

        for (JsonElement je : array) {
            result.add(je.getAsFloat());
        }

        return ArrayUtils.toPrimitive(result.toArray(new Float[0]));
    }

    public static Vec3f toVector3f(JsonArray array) {
        List<Float> result = Lists.newArrayList();

        for (JsonElement je : array) {
            result.add(je.getAsFloat());
        }

        if (result.size() < 3) {
            throw new IllegalArgumentException("Requires more than 3 elements to convert into 3d vector.");
        }

        return new Vec3f(result.get(0), result.get(1), result.get(2));
    }

    public static Vector3d toVector3d(JsonArray array) {
        List<Double> result = Lists.newArrayList();

        for (JsonElement je : array) {
            result.add(je.getAsDouble());
        }

        if (result.size() < 3) {
            throw new IllegalArgumentException("Requires more than 3 elements to convert into 3d vector.");
        }

        return new Vector3d(result.get(0), result.get(1), result.get(2));
    }


    public static String makeFirstLetterToUpper(String s) {
        StringBuilder sb = new StringBuilder();
        boolean upperNext = true;

        s = s.toLowerCase(Locale.ROOT);

        for (String sElement : s.split("")) {
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

    public static String toStringNvl(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}