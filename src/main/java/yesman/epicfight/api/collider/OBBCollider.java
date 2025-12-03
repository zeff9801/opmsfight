package yesman.epicfight.api.collider;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class OBBCollider extends Collider {
	protected final Vector3d[] modelVertices;
	protected final Vector3d[] modelNormals;
	protected Vector3d[] rotatedVertices;
	protected Vector3d[] rotatedNormals;
	protected Vec3f scale;

	public OBBCollider(double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
		this(getInitialAABB(vertexX, vertexY, vertexZ, centerX, centerY, centerZ), vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
	}

	protected OBBCollider(AxisAlignedBB outerAABB, double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
		super(new Vector3d(centerX, centerY, centerZ), outerAABB);
		this.modelVertices = new Vector3d[4];
		this.modelNormals = new Vector3d[3];
		this.rotatedVertices = new Vector3d[4];
		this.rotatedNormals = new Vector3d[3];
		this.modelVertices[0] = new Vector3d(vertexX, vertexY, -vertexZ);
		this.modelVertices[1] = new Vector3d(vertexX, vertexY, vertexZ);
		this.modelVertices[2] = new Vector3d(-vertexX, vertexY, vertexZ);
		this.modelVertices[3] = new Vector3d(-vertexX, vertexY, -vertexZ);
		this.modelNormals[0] = new Vector3d(1, 0, 0);
		this.modelNormals[1] = new Vector3d(0, 1, 0);
		this.modelNormals[2] = new Vector3d(0, 0, 1);
		this.rotatedVertices[0] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedVertices[1] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedVertices[2] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedVertices[3] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedNormals[0] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedNormals[1] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedNormals[2] = new Vector3d(0.0D, 0.0D, 0.0D);
	}

	public OBBCollider(AxisAlignedBB entityCallAABB, double pos1_x, double pos1_y, double pos1_z, double pos2_x,
					   double pos2_y, double pos2_z,
					   double norm1_x, double norm1_y, double norm1_z, double norm2_x, double norm2_y, double norm2_z,
					   double center_x, double center_y, double center_z) {
		super(new Vector3d(center_x, center_y, center_z), entityCallAABB);
		this.modelVertices = new Vector3d[2];
		this.modelNormals = new Vector3d[2];
		this.rotatedVertices = new Vector3d[2];
		this.rotatedNormals = new Vector3d[2];
		this.modelVertices[0] = new Vector3d(pos1_x, pos1_y, pos1_z);
		this.modelVertices[1] = new Vector3d(pos2_x, pos2_y, pos2_z);
		this.modelNormals[0] = new Vector3d(norm1_x, norm1_y, norm1_z);
		this.modelNormals[1] = new Vector3d(norm2_x, norm2_y, norm2_z);
		this.rotatedVertices[0] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedVertices[1] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedNormals[0] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedNormals[1] = new Vector3d(0.0D, 0.0D, 0.0D);
	}

	public OBBCollider(AxisAlignedBB aabbCopy) {
		super(null, null);
		this.modelVertices = null;
		this.modelNormals = null;
		double xSize = (aabbCopy.maxX - aabbCopy.minX) / 2;
		double ySize = (aabbCopy.maxY - aabbCopy.minY) / 2;
		double zSize = (aabbCopy.maxZ - aabbCopy.minZ) / 2;
		this.worldCenter = new Vector3d(-((float) aabbCopy.minX + xSize), (float) aabbCopy.minY + ySize, -((float) aabbCopy.minZ + zSize));
		this.rotatedVertices = new Vector3d[] {
				new Vector3d(-xSize, ySize, -zSize),
				new Vector3d(-xSize, ySize, zSize),
				new Vector3d(xSize, ySize, zSize),
				new Vector3d(xSize, ySize, -zSize)
		};
		this.rotatedNormals = new Vector3d[] {
				new Vector3d(1, 0, 0),
				new Vector3d(0, 1, 0),
				new Vector3d(0, 0, 1)
		};
		this.scale = new Vec3f(1.0F, 1.0F, 1.0F);
	}

	static AxisAlignedBB getInitialAABB(double posX, double posY, double posZ, double center_x, double center_y, double center_z) {
		double xLength = Math.abs(posX) + Math.abs(center_x);
		double yLength = Math.abs(posY) + Math.abs(center_y);
		double zLength = Math.abs(posZ) + Math.abs(center_z);
		double maxLength = Math.max(xLength, Math.max(yLength, zLength));
		return new AxisAlignedBB(maxLength, maxLength, maxLength, -maxLength, -maxLength, -maxLength);
	}

	@Override
	public void transform(OpenMatrix4f modelMatrix) {
		OpenMatrix4f noTranslation = modelMatrix.removeTranslation();
		if (this.modelVertices != null) {
			for (int i = 0; i < this.modelVertices.length; i++) {
				this.rotatedVertices[i] = OpenMatrix4f.transform(noTranslation, this.modelVertices[i]);
			}
		}
		if (this.modelNormals != null) {
			for (int i = 0; i < this.modelNormals.length; i++) {
				this.rotatedNormals[i] = OpenMatrix4f.transform(noTranslation, this.modelNormals[i]);
			}
		}
		this.scale = noTranslation.toScaleVector();
		super.transform(modelMatrix);
	}

	@Override
	protected AxisAlignedBB getHitboxAABB() {
		return this.outerAABB.inflate(
				(this.outerAABB.maxX - this.outerAABB.minX) * this.scale.x,
				(this.outerAABB.maxY - this.outerAABB.minY) * this.scale.y,
				(this.outerAABB.maxZ - this.outerAABB.minZ) * this.scale.z
		).move(-this.worldCenter.x, this.worldCenter.y, -this.worldCenter.z);
	}

	public boolean isCollide(OBBCollider opponent) {
		Vector3d toOpponent = opponent.worldCenter.subtract(this.worldCenter);

		for (Vector3d seperateAxis : this.rotatedNormals) {
			if (!checkSeparateAxisOverlap(seperateAxis, toOpponent, this, opponent)) {
				return false;
			}
		}

		for (Vector3d seperateAxis : opponent.rotatedNormals) {
			if (!checkSeparateAxisOverlap(seperateAxis, toOpponent, this, opponent)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isCollide(Entity opponent) {
		OBBCollider obb = new OBBCollider(opponent.getBoundingBox());
		return isCollide(obb);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void drawInternal(MatrixStack poseStack, IVertexBuilder vertexConsumer, Armature armature, Joint joint,
			Pose pose1, Pose pose2, float partialTicks, int color) {
		OpenMatrix4f poseMatrix;
		Pose interpolatedPose = Pose.interpolatePose(pose1, pose2, partialTicks);

		if (armature.rootJoint.equals(joint)) {
			JointTransform jt = interpolatedPose.orElseEmpty("Root");
			jt.rotation().x = 0.0F;
			jt.rotation().y = 0.0F;
			jt.rotation().z = 0.0F;
			jt.rotation().w = 1.0F;
			poseMatrix = jt.getAnimationBoundMatrix(armature.rootJoint, new OpenMatrix4f()).removeTranslation();
		} else {
			int pathIndex = armature.searchPathIndex(joint.getName());
			poseMatrix = armature.getBindedTransformByJointIndex(interpolatedPose, pathIndex);
		}

		poseStack.pushPose();
		MathUtils.mulStack(poseStack, poseMatrix);
		Matrix4f matrix = poseStack.last().pose();
		Vector3d vec = this.modelVertices[1];
		float maxX = (float) (this.modelCenter.x + vec.x);
		float maxY = (float) (this.modelCenter.y + vec.y);
		float maxZ = (float) (this.modelCenter.z + vec.z);
		float minX = (float) (this.modelCenter.x - vec.x);
		float minY = (float) (this.modelCenter.y - vec.y);
		float minZ = (float) (this.modelCenter.z - vec.z);

		vertexConsumer.vertex(matrix, minX, maxY, minZ).color(1.0F, color, color, 1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, maxY, maxZ).color(1.0F, color, color, 1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, maxY, maxZ).color(1.0F, color, color, 1.0F).normal(1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, maxY, maxZ).color(1.0F, color, color, 1.0F).normal(1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, maxY, maxZ).color(1.0F, color, color, 1.0F).normal(0.0F, 0.0F, -1.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, maxY, minZ).color(1.0F, color, color, 1.0F).normal(0.0F, 0.0F, -1.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, maxY, minZ).color(1.0F, color, color, 1.0F).normal(-1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, maxY, minZ).color(1.0F, color, color, 1.0F).normal(-1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, maxY, maxZ).color(1.0F, color, color, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, minY, maxZ).color(1.0F, color, color, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, maxY, maxZ).color(1.0F, color, color, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, minY, maxZ).color(1.0F, color, color, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, maxY, minZ).color(1.0F, color, color, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, minY, minZ).color(1.0F, color, color, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, maxY, minZ).color(1.0F, color, color, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, minY, minZ).color(1.0F, color, color, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, minY, minZ).color(1.0F, color, color, 1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, minY, maxZ).color(1.0F, color, color, 1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, minY, maxZ).color(1.0F, color, color, 1.0F).normal(1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, minY, maxZ).color(1.0F, color, color, 1.0F).normal(1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, minY, maxZ).color(1.0F, color, color, 1.0F).normal(0.0F, 0.0F, -1.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, minY, minZ).color(1.0F, color, color, 1.0F).normal(0.0F, 0.0F, -1.0F).endVertex();
		vertexConsumer.vertex(matrix, maxX, minY, minZ).color(1.0F, color, color, 1.0F).normal(-1.0F, 0.0F, 0.0F).endVertex();
		vertexConsumer.vertex(matrix, minX, minY, minZ).color(1.0F, color, color, 1.0F).normal(-1.0F, 0.0F, 0.0F).endVertex();
		poseStack.popPose();
	}

	public CompoundNBT serialize(CompoundNBT resultTag) {
		if (resultTag == null) {
			resultTag = new CompoundNBT();
		}

		resultTag.putInt("number", 1);

		ListNBT center = new ListNBT();
		center.add(DoubleNBT.valueOf(this.modelCenter.x));
		center.add(DoubleNBT.valueOf(this.modelCenter.y));
		center.add(DoubleNBT.valueOf(this.modelCenter.z));

		resultTag.put("center", center);

		ListNBT size = new ListNBT();
		size.add(DoubleNBT.valueOf(this.modelVertices[1].x));
		size.add(DoubleNBT.valueOf(this.modelVertices[1].y));
		size.add(DoubleNBT.valueOf(this.modelVertices[1].z));

		resultTag.put("size", size);

		return resultTag;
	}

	public static boolean collisionDetection(Vector3d seperateAxis, Vector3d toOpponent, OBBCollider collider,
			OBBCollider opponent) {
		return Math.abs(seperateAxis.dot(toOpponent)) <= Math.abs(
				seperateAxis.dot(collider.rotatedVertices[0]) - seperateAxis.dot(collider.rotatedVertices[2])) / 2.0D
				+ Math.abs(seperateAxis.dot(opponent.rotatedVertices[0]) - seperateAxis.dot(opponent.rotatedVertices[2]))
						/ 2.0D;
	}

	private static boolean checkSeparateAxisOverlap(Vector3d seperateAxis, Vector3d toOpponent, OBBCollider box1, OBBCollider box2) {
		Vector3d maxProj1 = null, maxProj2 = null;
		double maxDot1 = -1, maxDot2 = -1;

		if (seperateAxis.dot(toOpponent) < 0.0F) {
			seperateAxis = seperateAxis.scale(-1.0D);
		}

		for (Vector3d vertexVector : box1.rotatedVertices) {
			Vector3d temp = seperateAxis.dot(vertexVector) > 0.0F ? vertexVector : vertexVector.scale(-1.0D);
			double dot = seperateAxis.dot(temp);

			if (dot > maxDot1) {
				maxDot1 = dot;
				maxProj1 = temp;
			}
		}

		for (Vector3d vertexVector : box2.rotatedVertices) {
			Vector3d temp = seperateAxis.dot(vertexVector) > 0.0F ? vertexVector : vertexVector.scale(-1.0D);
			double dot = seperateAxis.dot(temp);

			if (dot > maxDot2) {
				maxDot2 = dot;
				maxProj2 = temp;
			}
		}

		return MathUtils.projectVector(toOpponent, seperateAxis).length() < MathUtils.projectVector(maxProj1, seperateAxis).length() + MathUtils.projectVector(maxProj2, seperateAxis).length();
	}

	@Override
	public RenderType getRenderType() {
		return RenderType.lines();
	}

	@Override
	public Collider deepCopy() {
		if (this.modelVertices == null) {
			return new OBBCollider(this.outerAABB);
		} else {
			return new OBBCollider(this.outerAABB, this.modelVertices[0].x, this.modelVertices[0].y, this.modelVertices[0].z,
					this.modelCenter.x, this.modelCenter.y, this.modelCenter.z);
		}
	}
}
