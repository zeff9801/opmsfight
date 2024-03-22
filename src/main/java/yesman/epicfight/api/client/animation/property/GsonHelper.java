package yesman.epicfight.api.client.animation.property;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import yesman.epicfight.api.client.animation.AnimationDataReader;

import java.io.Reader;

public class GsonHelper {
    private static final Gson gson = new Gson();

    public static float getAsFloat(JsonObject jsonObject, String memberName) {
        return jsonObject.get(memberName).getAsFloat();
    }

    public static int getAsInt(JsonObject jsonObject, String memberName) {
        return jsonObject.get(memberName).getAsInt();
    }

    public static String getAsString(JsonObject jsonObject, String memberName) {
        return jsonObject.get(memberName).getAsString();
    }

    public static JsonArray getAsJsonArray(JsonObject jsonObject, String memberName) {
        return jsonObject.get(memberName).getAsJsonArray();
    }

    public static AnimationDataReader fromJson(Gson gson, Reader reader, TypeToken<AnimationDataReader> type) {
        return gson.fromJson(reader, type.getType());
    }
}

