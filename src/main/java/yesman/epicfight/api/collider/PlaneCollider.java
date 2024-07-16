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

@Deprecated
public class PlaneCollider extends Collider {
	static AxisAlignedBB getInitialAABB(double center_x, double center_y, double center_z, double aX, double aY, double aZ, double bX, double bY, double bZ) {
		double xLength = Math.max(Math.abs(aX), Math.abs(bX)) + Math.abs(center_x);
		double yLength = Math.max(Math.abs(aY), Math.abs(bY)) + Math.abs(center_y);
		double zLength = Math.max(Math.abs(aZ), Math.abs(bZ)) + Math.abs(center_z);
		double maxLength = Math.max(xLength, Math.max(yLength, zLength));
		return new AxisAlignedBB(maxLength, maxLength, maxLength, -maxLength, -maxLength, -maxLength);
	}

	private final Vector3d[] modelPos;
	private final Vector3d[] worldPos;

	public PlaneCollider(double x, double y, double z, double aX, double aY, double aZ, double bX, double bY, double bZ) {
		this(getInitialAABB(x, y, z, aX, aY, aZ, bX, bY, bZ), x, y, z, aX, aY, aZ, bX, bY, bZ);
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

		return !(dot2 > 0.0D);
	}

	@Override
	public void transform(OpenMatrix4f mat) {
		for (int i = 0; i < 2; i ++) {
			this.worldPos[i] = OpenMatrix4f.transform(mat.removeTranslation(), this.modelPos[i]);
		}

		super.transform(mat);
	}

	@Override
	public PlaneCollider deepCopy() {
		Vector3d aVec = this.modelPos[0];
		Vector3d bVec = this.modelPos[1];

		return new PlaneCollider(this.modelCenter.x, this.modelCenter.y, this.modelCenter.z, aVec.x, aVec.y, aVec.z, bVec.x, bVec.y, bVec.z);
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