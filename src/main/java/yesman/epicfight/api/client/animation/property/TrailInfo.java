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
import yesman.epicfight.registry.entries.EpicFightParticles;

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
            .build();

    public static final TrailInfo ANIMATION_DEFAULT_TRAIL = TrailInfo.builder()
            .time(0.1F, 0.2F)
            .joint("Tool_R")
            .itemSkinHand(Hand.MAIN_HAND)
            .build();

    public Vector3d start;
    public Vector3d end;
    public IParticleData particle;
    public String joint;
    public float startTime;
    public float endTime;
    public float fadeTime;
    public float rCol;
    public float gCol;
    public float bCol;
    public int interpolateCount;
    public int trailLifetime;
    public int updateInterval;
    public int blockLight;
    public int skyLight;
    public ResourceLocation texturePath;
    public Hand hand;

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
        this.updateInterval = builder.updateInterval;
        this.blockLight = builder.blockLight;
        this.skyLight = builder.skyLight;
        this.texturePath = builder.texturePath;
        this.hand = builder.hand;
    }

    public TrailInfo.Builder copy() {
        return new TrailInfo.Builder(this);
    }

    public static boolean isValidTime(float time) {
        return !Float.isNaN(time) && time >= 0.0F;
    }

    public boolean playable() {
        boolean baseValid = this.start != null && this.end != null && this.particle != null && this.interpolateCount > 0 && this.trailLifetime > 0 && this.texturePath != null && this.updateInterval > 0;
        if (!baseValid) {
            return false;
        }

        // SWING_TRAIL needs joint/time metadata; other particles just need positions
        if (this.particle == EpicFightParticles.SWING_TRAIL.get()) {
            return !StringUtil.isNullOrEmpty(this.joint) && isValidTime(this.startTime) && isValidTime(this.endTime);
        }

        return true;
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

        if (trailObj.has("update_interval")) {
            trailBuilder.updateInterval(GsonHelper.getAsInt(trailObj, "update_interval"));
        }

        if (trailObj.has("block_light")) {
            trailBuilder.blockLight(GsonHelper.getAsInt(trailObj, "block_light"));
        }

        if (trailObj.has("sky_light")) {
            trailBuilder.skyLight(GsonHelper.getAsInt(trailObj, "sky_light"));
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

        return trailBuilder.build();
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

        if (compoundTag.contains("update_interval")) {
            trailBuilder.updateInterval(compoundTag.getInt("update_interval"));
        }

        if (compoundTag.contains("block_light")) {
            trailBuilder.blockLight(compoundTag.getInt("block_light"));
        }

        if (compoundTag.contains("sky_light")) {
            trailBuilder.skyLight(compoundTag.getInt("sky_light"));
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

        return trailBuilder.build();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private Vector3d start = new Vector3d(0.0D, 0.0D, 0.0D);
        private Vector3d end = new Vector3d(0.0D, 0.0D, -1.0D);
        private IParticleData particle = EpicFightParticles.SWING_TRAIL.get();
        private String joint;
        private float startTime = Float.NaN;
        private float endTime = Float.NaN;
        private float fadeTime = Float.NaN;
        private float rCol = 0.75F;
        private float gCol = 0.75F;
        private float bCol = 0.75F;
        private int interpolateCount = 4;
        private int trailLifetime = 4;
        private int updateInterval = 1;
        private int blockLight = 0;
        private int skyLight = 0;
        private ResourceLocation texturePath = new ResourceLocation(EpicFightMod.MODID, "textures/particle/swing_trail.png");
        private Hand hand = Hand.MAIN_HAND;

        public Builder() {}
        
        public Builder(TrailInfo trailInfo) {
        	this.start = trailInfo.start;
            this.end = trailInfo.end;
            this.joint = trailInfo.joint;
            this.particle = trailInfo.particle;
            this.startTime = trailInfo.startTime;
            this.endTime = trailInfo.endTime;
            this.fadeTime = trailInfo.fadeTime;
            this.rCol = trailInfo.rCol;
            this.gCol = trailInfo.gCol;
            this.bCol = trailInfo.bCol;
            this.interpolateCount = trailInfo.interpolateCount;
            this.trailLifetime = trailInfo.trailLifetime;
            this.texturePath = trailInfo.texturePath;
            this.hand = trailInfo.hand;
            this.updateInterval = trailInfo.updateInterval;
            this.blockLight = trailInfo.blockLight;
            this.skyLight = trailInfo.skyLight;
        }

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

        public TrailInfo.Builder updateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }

        public TrailInfo.Builder blockLight(int blockLight) {
            this.blockLight = blockLight;
            return this;
        }

        public TrailInfo.Builder skyLight(int skyLight) {
            this.skyLight = skyLight;
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

        public TrailInfo build() {
            return new TrailInfo(this);
        }
        
        public void build(TrailInfo trailInfo) {
            boolean validTime = isValidTime(this.startTime) && isValidTime(this.endTime);
            boolean validColor = this.rCol >= 0.0F && this.gCol >= 0.0F && this.bCol >= 0.0F;

            trailInfo.start = (this.start == null) ? trailInfo.start : this.start;
            trailInfo.end = (this.end == null) ? trailInfo.end : this.end;
            trailInfo.joint = (this.joint == null) ? trailInfo.joint : this.joint;
            trailInfo.particle = (this.particle == null) ? trailInfo.particle : this.particle;
            trailInfo.startTime = (!validTime) ? trailInfo.startTime : this.startTime;
            trailInfo.endTime = (!validTime) ? trailInfo.endTime : this.endTime;
            trailInfo.fadeTime = (!isValidTime(this.fadeTime)) ? trailInfo.fadeTime : this.fadeTime;
            trailInfo.rCol = !(validColor) ? trailInfo.rCol : this.rCol;
            trailInfo.gCol = !(validColor) ? trailInfo.gCol : this.gCol;
            trailInfo.bCol = !(validColor) ? trailInfo.bCol : this.bCol;
            trailInfo.interpolateCount = (this.interpolateCount < 0) ? trailInfo.interpolateCount : this.interpolateCount;
            trailInfo.trailLifetime = (this.trailLifetime < 0) ? trailInfo.trailLifetime : this.trailLifetime;
            trailInfo.updateInterval = this.updateInterval <= 0 ? trailInfo.updateInterval : this.updateInterval;
            trailInfo.blockLight = this.blockLight;
            trailInfo.skyLight = this.skyLight;
            trailInfo.texturePath = (this.texturePath == null) ? trailInfo.texturePath : this.texturePath;
            trailInfo.hand = (this.hand == null) ? trailInfo.hand : this.hand;
        }
    }
}
