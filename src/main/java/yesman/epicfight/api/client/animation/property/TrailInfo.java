package yesman.epicfight.api.client.animation.property;

import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.netty.util.internal.StringUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;

@OnlyIn(Dist.CLIENT)
public class TrailInfo {
    public static final TrailInfo PREVIEWER_DEFAULT_TRAIL = TrailInfo.builder()
            .startPos(new Vector3d(0.0D, 0.0D, 0.0D))
            .endPos(new Vector3d(0.0D, 0.0D, -1.0D))
            .interpolations(4)
            .lifetime(4)
            .r(0.75F)
            .g(0.75F)
            .b(0.75F)
            .texture(new ResourceLocation(EpicFightMod.MODID, "textures/particle/swing_trail.png"))
            .type(EpicFightParticles.SWING_TRAIL.get())
            .create();

    public static final TrailInfo ANIMATION_DEFAULT_TRAIL = TrailInfo.builder()
            .time(0.1F, 0.2F)
            .joint("Tool_R")
            .itemSkinHand(Hand.MAIN_HAND)
            .create();

    public final Vector3d start;
    public final Vector3d end;
    public final IParticleData particle;
    public final String joint;
    public final float startTime;
    public final float endTime;
    public final float fadeTime;
    public final float rCol;
    public final float gCol;
    public final float bCol;
    public final int interpolateCount;
    public final int trailLifetime;
    public final ResourceLocation texturePath;
    public final Hand hand;

    private TrailInfo(TrailInfo.Builder builder) {
        this.start = builder.start;
        this.end = builder.end;
        this.joint = builder.joint;
        this.particle = builder.particle;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.fadeTime = builder.fadeTime;
        this.rCol = builder.rCol;
        this.gCol = builder.gCol;
        this.bCol = builder.bCol;
        this.interpolateCount = builder.interpolateCount;
        this.trailLifetime = builder.trailLifetime;
        this.texturePath = builder.texturePath;
        this.hand = builder.hand;
    }

    public TrailInfo overwrite(TrailInfo trailInfo) {
        boolean validTime = isValidTime(trailInfo.startTime) && isValidTime(trailInfo.endTime);
        boolean validColor = trailInfo.rCol >= 0.0F && trailInfo.gCol >= 0.0F && trailInfo.bCol >= 0.0F;
        TrailInfo.Builder builder = new TrailInfo.Builder();

        builder.startPos((trailInfo.start == null) ? this.start : trailInfo.start);
        builder.endPos((trailInfo.end == null) ? this.end : trailInfo.end);
        builder.joint((trailInfo.joint == null) ? this.joint : trailInfo.joint);
        builder.type((trailInfo.particle == null) ? this.particle : trailInfo.particle);
        builder.time((!validTime) ? this.startTime : trailInfo.startTime, (!validTime) ? this.endTime : trailInfo.endTime);
        builder.fadeTime((!isValidTime(trailInfo.fadeTime)) ? this.fadeTime : trailInfo.fadeTime);
        builder.r(!(validColor) ? this.rCol : trailInfo.rCol);
        builder.g(!(validColor) ? this.gCol : trailInfo.gCol);
        builder.b(!(validColor) ? this.bCol : trailInfo.bCol);
        builder.interpolations((trailInfo.interpolateCount < 0) ? this.interpolateCount : trailInfo.interpolateCount);
        builder.lifetime((trailInfo.trailLifetime < 0) ? this.trailLifetime : trailInfo.trailLifetime);
        builder.texture((trailInfo.texturePath == null) ? this.texturePath : trailInfo.texturePath);
        builder.itemSkinHand((trailInfo.hand == null) ? this.hand : trailInfo.hand);

        return builder.create();
    }

    public static boolean isValidTime(float time) {
        return !Float.isNaN(time) && time >= 0.0F;
    }

    public boolean playable() {
        return this.start != null && this.end != null && this.particle != null && !StringUtil.isNullOrEmpty(this.joint) && isValidTime(this.startTime) && isValidTime(this.endTime) && this.interpolateCount > 0 && this.trailLifetime > 0 && this.texturePath != null;
    }

    public static TrailInfo.Builder builder() {
        return new TrailInfo.Builder();
    }

    public static TrailInfo deserialize(JsonElement json) {
        JsonObject trailObj = json.getAsJsonObject();
        TrailInfo.Builder trailBuilder = TrailInfo.builder();

        if (trailObj.has("start_time") && trailObj.has("end_time")) {
            float startTime = GsonHelper.getAsFloat(trailObj, "start_time");
            float endTime = GsonHelper.getAsFloat(trailObj, "end_time");
            trailBuilder.time(startTime, endTime);
        }

        if (trailObj.has("fade_time")) {
            float fadeTime = trailObj.get("fade_time").getAsFloat();
            trailBuilder.fadeTime(fadeTime);
        }

        if (trailObj.has("lifetime")) {
            trailBuilder.lifetime(GsonHelper.getAsInt(trailObj, "lifetime"));
        }

        if (trailObj.has("interpolations")) {
            trailBuilder.interpolations(GsonHelper.getAsInt(trailObj, "interpolations"));
        }

        if (trailObj.has("joint")) {
            trailBuilder.joint(GsonHelper.getAsString(trailObj, "joint"));
        }

        if (trailObj.has("texture_path")) {
            trailBuilder.texture(GsonHelper.getAsString(trailObj, "texture_path"));
        }

        if (trailObj.has("particle_type")) {
            String particleTypeName = GsonHelper.getAsString(trailObj, "particle_type");
            IParticleData particleType = (IParticleData)ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(particleTypeName));
            trailBuilder.type(particleType);
        }

        if (trailObj.has("color")) {
            JsonArray color = trailObj.get("color").getAsJsonArray();
            Vec3f colorVec = ParseUtil.toVector3f(color);
            trailBuilder.r(colorVec.x / 255F);
            trailBuilder.g(colorVec.y / 255F);
            trailBuilder.b(colorVec.z / 255F);
        }

        if (trailObj.has("begin_pos")) {
            JsonArray beginPos = trailObj.get("begin_pos").getAsJsonArray();
            Vector3d begin = ParseUtil.toVector3d(beginPos);
            trailBuilder.startPos(begin);
        }

        if (trailObj.has("end_pos")) {
            JsonArray endPos = trailObj.get("end_pos").getAsJsonArray();
            Vector3d end = ParseUtil.toVector3d(endPos);
            trailBuilder.endPos(end);
        }

        if (trailObj.has("item_skin_hand")) {
            String itemSkinHand = trailObj.get("item_skin_hand").getAsString();
            Hand hand = Hand.valueOf(itemSkinHand.toUpperCase(Locale.ROOT));
            trailBuilder.itemSkinHand(hand);
        }

        return trailBuilder.create();
    }

    public static TrailInfo deserialize(CompoundNBT compoundTag) {
        TrailInfo.Builder trailBuilder = TrailInfo.builder();

        if (compoundTag.contains("start_time") && compoundTag.contains("end_time")) {
            float startTime = compoundTag.getFloat("start_time");
            float endTime = compoundTag.getFloat("end_time");
            trailBuilder.time(startTime, endTime);
        }

        if (compoundTag.contains("fade_time")) {
            float fadeTime = compoundTag.getFloat("fade_time");
            trailBuilder.fadeTime(fadeTime);
        }

        if (compoundTag.contains("lifetime")) {
            trailBuilder.lifetime(compoundTag.getInt("lifetime"));
        }

        if (compoundTag.contains("interpolations")) {
            trailBuilder.interpolations(compoundTag.getInt("interpolations"));
        }

        if (compoundTag.contains("joint")) {
            trailBuilder.joint(compoundTag.getString("joint"));
        }

        if (compoundTag.contains("texture_path")) {
            trailBuilder.texture(compoundTag.getString("texture_path"));
        }

        if (compoundTag.contains("particle_type")) {
            String particleTypeName = compoundTag.getString("particle_type");
            IParticleData particleType = (IParticleData)ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(particleTypeName));
            trailBuilder.type(particleType);
        }

        if (compoundTag.contains("color")) {
            ListNBT color = compoundTag.getList("color", Constants.NBT.TAG_INT);
            trailBuilder.r(color.getInt(0) / 255F);
            trailBuilder.g(color.getInt(1) / 255F);
            trailBuilder.b(color.getInt(2) / 255F);
        }

        if (compoundTag.contains("begin_pos")) {
            ListNBT beginPos = compoundTag.getList("begin_pos", Constants.NBT.TAG_DOUBLE);
            trailBuilder.startPos(new Vector3d(beginPos.getDouble(0), beginPos.getDouble(1), beginPos.getDouble(2)));
        }

        if (compoundTag.contains("end_pos")) {
            ListNBT endPos = compoundTag.getList("end_pos", Constants.NBT.TAG_DOUBLE);
            trailBuilder.endPos(new Vector3d(endPos.getDouble(0), endPos.getDouble(1), endPos.getDouble(2)));
        }

        if (compoundTag.contains("item_skin_hand")) {
            String itemSkinHand = compoundTag.getString("item_skin_hand");
            Hand hand = Hand.valueOf(itemSkinHand.toUpperCase(Locale.ROOT));
            trailBuilder.itemSkinHand(hand);
        }

        return trailBuilder.create();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private Vector3d start;
        private Vector3d end;
        private IParticleData particle;
        private String joint;
        private float startTime = Float.NaN;
        private float endTime = Float.NaN;
        private float fadeTime = Float.NaN;
        private float rCol = -1.0F;
        private float gCol = -1.0F;
        private float bCol = -1.0F;
        private int interpolateCount = -1;
        private int trailLifetime = -1;
        private ResourceLocation texturePath;
        private Hand hand = Hand.MAIN_HAND;

        public TrailInfo.Builder startPos(Vector3d start) {
            this.start = start;
            return this;
        }

        public TrailInfo.Builder endPos(Vector3d end) {
            this.end = end;
            return this;
        }

        public TrailInfo.Builder type(IParticleData particle) {
            this.particle = particle;
            return this;
        }

        public TrailInfo.Builder joint(String joint) {
            this.joint = joint;
            return this;
        }

        public TrailInfo.Builder time(float startTime, float endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
            return this;
        }

        public TrailInfo.Builder fadeTime(float fadeTime) {
            this.fadeTime = fadeTime;
            return this;
        }

        public TrailInfo.Builder r(float rCol) {
            this.rCol = rCol;
            return this;
        }

        public TrailInfo.Builder g(float gCol) {
            this.gCol = gCol;
            return this;
        }

        public TrailInfo.Builder b(float bCol) {
            this.bCol = bCol;
            return this;
        }

        public TrailInfo.Builder interpolations(int interpolateCount) {
            this.interpolateCount = interpolateCount;
            return this;
        }

        public TrailInfo.Builder lifetime(int trailLifetime) {
            this.trailLifetime = trailLifetime;
            return this;
        }

        public TrailInfo.Builder texture(String texturePath) {
            this.texturePath = new ResourceLocation(texturePath);
            return this;
        }

        public TrailInfo.Builder texture(ResourceLocation texturePath) {
            this.texturePath = texturePath;
            return this;
        }

        public TrailInfo.Builder itemSkinHand(Hand itemSkinHand) {
            this.hand = itemSkinHand;
            return this;
        }

        public TrailInfo create() {
            return new TrailInfo(this);
        }
    }
}