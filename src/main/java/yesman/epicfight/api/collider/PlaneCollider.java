package yesman.epicfight.api.collider;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PlaneCollider extends Collider {
	private Vector3d[] modelPos;
	private Vector3d[] worldPos;

	public PlaneCollider(Vector3d center, AxisAlignedBB entityCallAABB) {
		super(center, entityCallAABB);
	}

	public PlaneCollider(AxisAlignedBB entityCallAABB, double centerX, double centerY, double centerZ, double pos1X, double pos1Y, double pos1Z, double pos2X, double pos2Y, double pos2Z) {
		super(new Vector3d(centerX, centerY, centerZ), entityCallAABB);

		this.modelPos = new Vector3d[2];
		this.worldPos = new Vector3d[2];
		this.modelPos[0] = new Vector3d(pos1X, pos1Y, pos1Z);
		this.modelPos[1] = new Vector3d(pos2X, pos2Y, pos2Z);
		this.worldPos[0] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.worldPos[1] = new Vector3d(0.0D, 0.0D, 0.0D);
	}



	@Override
	public PlaneCollider deepCopy() {
		Vector3d aVec = this.modelPos[0];
		Vector3d bVec = this.modelPos[1];

		return new PlaneCollider(this.outerAABB, this.modelCenter.x, this.modelCenter.y, this.modelCenter.z, aVec.x, aVec.y, aVec.z, bVec.x, bVec.y, bVec.z);
	}
	@Override
	public boolean isCollide(Entity entity) {
		AxisAlignedBB opponent = entity.getBoundingBox();
		Vector3d planeNorm = this.worldPos[0].cross(this.worldPos[1]);
		Vector3d pos = new Vector3d(planeNorm.x >= 0 ? opponent.maxX : opponent.minX, planeNorm.y >= 0 ? opponent.maxY : opponent.minY, planeNorm.z >= 0 ? opponent.maxZ : opponent.minZ);
		Vector3d neg = new Vector3d(planeNorm.x >= 0 ? opponent.minX : opponent.maxX, planeNorm.y >= 0 ? opponent.minY : opponent.maxY, planeNorm.z >= 0 ? opponent.minZ : opponent.maxZ);
		double planeD = planeNorm.dot(this.worldCenter);
		double dot1 = planeNorm.dot(pos) - planeD;

		if (dot1 < 0.0D) {
			return false;
		}

		double dot2 = planeNorm.dot(neg) - planeD;

		if (dot2 > 0.0D) {
			return false;
		}

		return true;
	}

	@Override
	public void transform(OpenMatrix4f mat) {
		for (int i = 0; i < 2; i ++) {
			this.worldPos[i] = OpenMatrix4f.transform(mat.removeTranslation(), this.modelPos[i]);
		}

		super.transform(mat);
	}

	@Override
	public void drawInternal(MatrixStack poseStack, IVertexBuilder vertexConsumer, Armature armature, Joint joint, Pose pose1, Pose pose2, float partialTicks, int color) {
	}

	@Override
	public void draw(MatrixStack matrixStackIn, IRenderTypeBuffer buffer, LivingEntityPatch<?> entitypatch, AttackAnimation animation, Joint joint, float prevElapsedTime, float elapsedTime, float partialTicks, float attackSpeed) {
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public RenderType getRenderType() {
		return null;
	}
}