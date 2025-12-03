package yesman.epicfight.client.particle;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.physics.bezier.CubicBezierCurve;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.projectile.ProjectilePatch;

@OnlyIn(Dist.CLIENT)
public class ProjectileTrailParticle extends AbstractTrailParticle<ProjectilePatch<AbstractArrowEntity>> {
	protected float lastXRot;
	protected float lastYRot;

	protected ProjectileTrailParticle(ClientWorld level, ProjectilePatch<AbstractArrowEntity> entitypatch, TrailInfo trailInfo) {
		super(level, entitypatch, trailInfo);

		this.rCol = trailInfo.rCol;
		this.gCol = trailInfo.gCol;
		this.bCol = trailInfo.bCol;
	}

	@Override
	protected boolean canContinue() {
		if (this.owner.hit()) {
			return false;
		}

		return this.owner.getOriginal().isAlive();
	}

	@Override
	protected void createNextCurve() {
		if (this.shouldRemove) {
			return;
		}

		if (this.owner.getOriginal() instanceof ArrowEntity) {
			int color = ((ArrowEntity)this.owner.getOriginal()).getColor();
			float r = ((color & 0x00FF0000) >> 16) / 255.0F;
			float g = ((color & 0x0000FF00) >> 8) / 255.0F;
			float b = ((color & 0x000000FF)) / 255.0F;
			this.rCol = r;
			this.gCol = g;
			this.bCol = b;
		}

		boolean isFirstTrail = this.trailEdges.isEmpty();

		if (isFirstTrail) {
			this.lastXRot = this.owner.getOriginal().xRot;
			this.lastYRot = 180.0F + this.owner.getOriginal().yRot;
		}

		TrailInfo trailInfo = this.trailInfo;
		Vector3d posOld = this.owner.getOriginal().getPosition(0.0F);
		Vector3d posCur = this.owner.getOriginal().getPosition(1.0F);
		Vector3d posMid = MathUtils.lerpVector(posOld, posCur, 0.5F);

		float xRotO = this.lastXRot;
		float xRot = this.owner.getOriginal().xRot;
		float xRotMod = MathHelper.rotLerp(0.5F, xRotO, xRot);
		float yRotO =  this.lastYRot;
		float yRot =  180.0F + this.owner.getOriginal().yRot;
		float yRotMod = MathHelper.rotLerp(0.5F, yRotO, yRot);

		OpenMatrix4f prevTransform
			= OpenMatrix4f
				.createTranslation((float)posOld.x, (float)posOld.y, (float)posOld.z)
				.mulBack(OpenMatrix4f.createRotatorDeg(yRotO, Vec3f.Y_AXIS)
						.mulBack(OpenMatrix4f.createRotatorDeg(xRotO, Vec3f.X_AXIS)));
		OpenMatrix4f modTransform
			= OpenMatrix4f
				.createTranslation((float)posMid.x, (float)posMid.y, (float)posMid.z)
				.mulBack(OpenMatrix4f.createRotatorDeg(yRotMod, Vec3f.Y_AXIS)
						.mulBack(OpenMatrix4f.createRotatorDeg(xRotMod, Vec3f.X_AXIS)));
		OpenMatrix4f curTransform
			= OpenMatrix4f
				.createTranslation((float)posCur.x, (float)posCur.y, (float)posCur.z)
				.mulBack(OpenMatrix4f.createRotatorDeg(yRot, Vec3f.Y_AXIS)
						.mulBack(OpenMatrix4f.createRotatorDeg(xRot, Vec3f.X_AXIS)));

		Vector3d prevStartPos = OpenMatrix4f.transform(prevTransform, trailInfo.start);
		Vector3d prevEndPos = OpenMatrix4f.transform(prevTransform, trailInfo.end);
		Vector3d middleStartPos = OpenMatrix4f.transform(modTransform, trailInfo.start);
		Vector3d middleEndPos = OpenMatrix4f.transform(modTransform, trailInfo.end);
		Vector3d currentStartPos = OpenMatrix4f.transform(curTransform, trailInfo.start);
		Vector3d currentEndPos = OpenMatrix4f.transform(curTransform, trailInfo.end);
		List<Vector3d> finalStartPositions;
		List<Vector3d> finalEndPositions;
		List<Vector3d> startPosList = Lists.newArrayList();
		List<Vector3d> endPosList = Lists.newArrayList();
		TrailEdge edge1;
		TrailEdge edge2;

		if (isFirstTrail) {
			edge1 = new TrailEdge(prevStartPos, prevEndPos, -1);
			edge2 = new TrailEdge(middleStartPos, middleEndPos, -1);
		} else {
			edge1 = this.trailEdges.get(this.trailEdges.size() - (this.trailInfo.interpolateCount / 2 + 1));
			edge2 = this.trailEdges.get(this.trailEdges.size() - 1);
			edge2.lifetime++;
		}

		startPosList.add(edge1.start);
		endPosList.add(edge1.end);
		startPosList.add(edge2.start);
		endPosList.add(edge2.end);
		startPosList.add(middleStartPos);
		endPosList.add(middleEndPos);
		startPosList.add(currentStartPos);
		endPosList.add(currentEndPos);

		finalStartPositions = CubicBezierCurve.getBezierInterpolatedPoints(startPosList, 1, 3, this.trailInfo.interpolateCount);
		finalEndPositions = CubicBezierCurve.getBezierInterpolatedPoints(endPosList, 1, 3, this.trailInfo.interpolateCount);

		if (!isFirstTrail) {
			finalStartPositions.remove(0);
			finalEndPositions.remove(0);
		}

		this.makeTrailEdges(finalStartPositions, finalEndPositions, this.trailEdges);

		this.lastXRot = xRot;
		this.lastYRot = yRot;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Provider implements IParticleFactory<BasicParticleType> {
		public static final TrailInfo ARROW_TRAIL_DEFAULT
			= TrailInfo
				.builder()
				.type(EpicFightParticles.PROJECTILE_TRAIL.get())
				.startPos(new Vector3d(-0.1D, 0.0D, 0.7D))
				.endPos(new Vector3d(0.1D, 0.0D, 0.7D))
				.interpolations(4)
				.lifetime(9)
				.updateInterval(1)
				.texture(new ResourceLocation(EpicFightMod.MODID, "textures/particle/projectile_trail.png"))
				.build();

		public static final TrailInfo SPECTRAL_ARROW_TRAIL_DEFAULT
			= TrailInfo
				.builder()
				.type(EpicFightParticles.PROJECTILE_TRAIL.get())
				.startPos(new Vector3d(-0.1D, 0.0D, 0.7D))
				.endPos(new Vector3d(0.1D, 0.0D, 0.7D))
				.interpolations(4)
				.lifetime(9)
				.updateInterval(1)
				.r(252.0F / 255.0F)
				.g(252.0F / 255.0F)
				.b(118.0F / 255.0F)
				.texture(new ResourceLocation(EpicFightMod.MODID, "textures/particle/projectile_trail.png"))
				.build();

		public static final TrailInfo TRIDENT_TRAIL_DEFAULT
			= TrailInfo
				.builder()
				.type(EpicFightParticles.PROJECTILE_TRAIL.get())
				.startPos(new Vector3d(-0.1D, 0.0D, 1.8D))
				.endPos(new Vector3d(0.1D, 0.0D, 1.8D))
				.interpolations(4)
				.lifetime(9)
				.updateInterval(1)
				.r(0.0F / 255.0F)
				.g(232.0F / 255.0F)
				.b(245.0F / 255.0F)
				.texture(new ResourceLocation(EpicFightMod.MODID, "textures/particle/projectile_trail.png"))
				.build();

		@SuppressWarnings("unchecked")
		@Override
		public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			int eid = (int)Double.doubleToRawLongBits(x);
			Entity entity = level.getEntity(eid);

			if (entity == null) {
				return null;
			}

			if (!(entity instanceof AbstractArrowEntity)) {
				return null;
			}

			ProjectilePatch<AbstractArrowEntity> entitypatch = EpicFightCapabilities.getEntityPatch(entity, ProjectilePatch.class);

			if (entitypatch != null) {
				TrailInfo trailInfo;

				if (entitypatch.getOriginal() instanceof ArrowEntity) {
					trailInfo = ARROW_TRAIL_DEFAULT;
				} else if (entitypatch.getOriginal() instanceof SpectralArrowEntity) {
					trailInfo = SPECTRAL_ARROW_TRAIL_DEFAULT;
				} else if (entitypatch.getOriginal() instanceof TridentEntity) {
					trailInfo = TRIDENT_TRAIL_DEFAULT;
				} else {
					return null;
				}

				return new ProjectileTrailParticle(level, entitypatch, trailInfo);
			}

			return null;
		}
	}
}
