package yesman.epicfight.client.particle;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractTrailParticle<T extends EntityPatch<?>> extends SpriteTexturedParticle {
    protected final TrailInfo trailInfo;
    protected final T owner;
    protected final List<TrailEdge> trailEdges;
    protected float startEdgeCorrection = 0.0F;
    protected boolean shouldRemove;

    protected AbstractTrailParticle(ClientWorld level, T entitypatch, TrailInfo trailInfo) {
        super(level, 0, 0, 0);
        this.hasPhysics = false;
        this.owner = entitypatch;
        this.trailEdges = Lists.newLinkedList();
        this.trailInfo = trailInfo;

        Vector3d entityPos = entitypatch.getOriginal().position();
        this.move(entityPos.x, entityPos.y + entitypatch.getOriginal().getEyeHeight(), entityPos.z);

        float size = (float) Math.max(this.trailInfo.start.length(), this.trailInfo.end.length()) * 2.0F;
        this.setSize(size, size);

        this.rCol = Math.max(this.trailInfo.rCol, 0.0F);
        this.gCol = Math.max(this.trailInfo.gCol, 0.0F);
        this.bCol = Math.max(this.trailInfo.bCol, 0.0F);
    }

    protected abstract boolean canContinue();

    protected boolean canCreateNextCurve() {
        return this.age % Math.max(1, this.trailInfo.updateInterval) == 0 && !this.removed;
    }

    protected abstract void createNextCurve();

    @Override
    public void tick() {
        if (this.shouldRemove) {
            if (this.age >= this.lifetime) {
                this.remove();
            }
        } else {
            if (!this.canContinue()) {
                this.shouldRemove = true;
                this.lifetime = this.age + this.trailInfo.trailLifetime;
            }
        }

        this.age++;
        this.trailEdges.removeIf(TrailEdge::isDead);

        if (!this.canCreateNextCurve()) {
            return;
        }

        Vector3d lastPos = this.owner.getOriginal().getPosition(0.0F);
        double xd = Math.pow(this.owner.getOriginal().getX() - lastPos.x, 2);
        double yd = Math.pow(this.owner.getOriginal().getY() - lastPos.y, 2);
        double zd = Math.pow(this.owner.getOriginal().getZ() - lastPos.z, 2);
        float move = (float)Math.sqrt(xd + yd + zd) * 2.0F;

        this.setSize(this.bbWidth + move, this.bbHeight + move);
        this.createNextCurve();
    }

    @Override
    public void render(IVertexBuilder vertexConsumer, ActiveRenderInfo camera, float partialTick) {
        if (this.trailEdges.isEmpty()) {
            return;
        }

        MatrixStack poseStack = new MatrixStack();
        int light = this.getLightColor(partialTick);
        this.setupMatrixStack(poseStack, camera, partialTick);
        Matrix4f matrix4f = poseStack.last().pose();
        int edges = this.trailEdges.size() - 1;
        boolean startFade = this.trailEdges.get(0).lifetime == 1;
        boolean endFade = this.trailEdges.get(edges).lifetime == this.trailInfo.trailLifetime;
        float startEdge = (startFade ? this.trailInfo.interpolateCount * 2 * partialTick : 0.0F) + this.startEdgeCorrection;
        float endEdge = endFade ? Math.min(edges - (this.trailInfo.interpolateCount * 2) * (1.0F - partialTick), edges - 1) : edges - 1;
        float interval = 1.0F / (endEdge - startEdge);
        float fading = 1.0F;

        if (this.shouldRemove) {
            if (TrailInfo.isValidTime(this.trailInfo.fadeTime)) {
                fading = ((float)(this.lifetime - this.age) / (float)this.trailInfo.trailLifetime);
            } else {
                fading = MathHelper.clamp(((this.lifetime - this.age) + (1.0F - partialTick)) / this.trailInfo.trailLifetime, 0.0F, 1.0F);
            }
        }

        float partialStartEdge = interval * (startEdge % 1.0F);
        float from = -partialStartEdge;
        float to = -partialStartEdge + interval;

        for (int i = (int)(startEdge); i < (int)endEdge + 1; i++) {
            TrailEdge e1 = this.trailEdges.get(i);
            TrailEdge e2 = this.trailEdges.get(i + 1);
            Vector4f pos1 = new Vector4f((float)e1.start.x, (float)e1.start.y, (float)e1.start.z, 1.0F);
            Vector4f pos2 = new Vector4f((float)e1.end.x, (float)e1.end.y, (float)e1.end.z, 1.0F);
            Vector4f pos3 = new Vector4f((float)e2.end.x, (float)e2.end.y, (float)e2.end.z, 1.0F);
            Vector4f pos4 = new Vector4f((float)e2.start.x, (float)e2.start.y, (float)e2.start.z, 1.0F);

            pos1.transform(matrix4f);
            pos2.transform(matrix4f);
            pos3.transform(matrix4f);
            pos4.transform(matrix4f);

            float alphaFrom = MathHelper.clamp(from, 0.0F, 1.0F);
            float alphaTo = MathHelper.clamp(to, 0.0F, 1.0F);

            vertexConsumer.vertex(pos1.x(), pos1.y(), pos1.z()).uv(from, 1.0F).color(this.rCol, this.gCol, this.bCol, this.alpha * alphaFrom * fading).uv2(light).endVertex();
            vertexConsumer.vertex(pos2.x(), pos2.y(), pos2.z()).uv(from, 0.0F).color(this.rCol, this.gCol, this.bCol, this.alpha * alphaFrom * fading).uv2(light).endVertex();
            vertexConsumer.vertex(pos3.x(), pos3.y(), pos3.z()).uv(to, 0.0F).color(this.rCol, this.gCol, this.bCol, this.alpha * alphaTo * fading).uv2(light).endVertex();
            vertexConsumer.vertex(pos4.x(), pos4.y(), pos4.z()).uv(to, 1.0F).color(this.rCol, this.gCol, this.bCol, this.alpha * alphaTo * fading).uv2(light).endVertex();

            from += interval;
            to += interval;
        }
    }

    @Override
    public IParticleRenderType getRenderType() {
        return EpicFightParticleRenderTypes.trailEffect(this.trailInfo.texturePath);
    }

    protected void setupMatrixStack(MatrixStack matrixStack, ActiveRenderInfo camera, float partialTicks) {
        Vector3d vec3 = camera.getPosition();
        matrixStack.translate(-vec3.x, -vec3.y, -vec3.z);
    }

    protected void makeTrailEdges(List<Vector3d> startPositions, List<Vector3d> endPositions, List<TrailEdge> dest) {
        for (int i = 0; i < startPositions.size(); i++) {
            dest.add(new TrailEdge(startPositions.get(i), endPositions.get(i), this.trailInfo.trailLifetime));
        }
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    protected int getLightColor(float partialTick) {
        if (this.trailInfo.blockLight > 0 || this.trailInfo.skyLight > 0) {
            return LightTexture.pack(MathHelper.clamp(this.trailInfo.blockLight, 0, 15), MathHelper.clamp(this.trailInfo.skyLight, 0, 15));
        }
        return super.getLightColor(partialTick);
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailEdge {
        public final Vector3d start;
        public final Vector3d end;
        public int lifetime;

        public TrailEdge(Vector3d start, Vector3d end, int lifetime) {
            this.start = start;
            this.end = end;
            this.lifetime = lifetime;
        }

        public boolean isDead() {
            return --this.lifetime <= 0;
        }
    }
}
