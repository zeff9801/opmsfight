package yesman.epicfight.api.collider;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
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
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;

public class OBBCollider extends Collider {
	protected final Vector3d[] modelVertex;
	protected final Vector3d[] modelNormal;
	protected Vector3d[] rotatedVertex;
	protected Vector3d[] rotatedNormal;
	protected Vec3f scale;

	/**
	 * make 3d obb
	 * @param pos1 left_back
	 * @param pos2 left_front
	 * @param pos3 right_front
	 * @param pos4 right_back
	 * @param modelCenter central position
	 */
	public OBBCollider(double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
		this(getInitialAABB(vertexX, vertexY, vertexZ, centerX, centerY, centerZ), vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
	}

	protected OBBCollider(AxisAlignedBB outerAABB, double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
		super(new Vector3d(centerX, centerY, centerZ), outerAABB);
		this.modelVertex = new Vector3d[4];
		this.modelNormal = new Vector3d[3];
		this.rotatedVertex = new Vector3d[4];
		this.rotatedNormal = new Vector3d[3];
		this.modelVertex[0] = new Vector3d(vertexX, vertexY, -vertexZ);
		this.modelVertex[1] = new Vector3d(vertexX, vertexY, vertexZ);
		this.modelVertex[2] = new Vector3d(-vertexX, vertexY, vertexZ);
		this.modelVertex[3] = new Vector3d(-vertexX, vertexY, -vertexZ);
		this.modelNormal[0] = new Vector3d(1, 0, 0);
		this.modelNormal[1] = new Vector3d(0, 1, 0);
		this.modelNormal[2] = new Vector3d(0, 0, -1);
		this.rotatedVertex[0] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedVertex[1] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedVertex[2] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedVertex[3] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedNormal[0] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedNormal[1] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedNormal[2] = new Vector3d(0.0D, 0.0D, 0.0D);
	}

	static AxisAlignedBB getInitialAABB(double posX, double posY, double posZ, double center_x, double center_y, double center_z) {
		double xLength = Math.abs(posX) + Math.abs(center_x);
		double yLength = Math.abs(posY) + Math.abs(center_y);
		double zLength = Math.abs(posZ) + Math.abs(center_z);
		double maxLength = Math.max(xLength, Math.max(yLength, zLength));
		return new AxisAlignedBB(maxLength, maxLength, maxLength, -maxLength, -maxLength, -maxLength);
	}

	/**
	 * make 2d obb
	 * @param pos1 left
	 * @param pos2 right
	 * @param modelCenter central position
	 */
	public OBBCollider(AxisAlignedBB entityCallAABB, double pos1_x, double pos1_y, double pos1_z, double pos2_x, double pos2_y, double pos2_z,
					   double norm1_x, double norm1_y, double norm1_z, double norm2_x, double norm2_y, double norm2_z, double center_x, double center_y, double center_z) {
		super(new Vector3d(center_x, center_y, center_z), entityCallAABB);
		this.modelVertex = new Vector3d[2];
		this.modelNormal = new Vector3d[2];
		this.rotatedVertex = new Vector3d[2];
		this.rotatedNormal = new Vector3d[2];
		this.modelVertex[0] = new Vector3d(pos1_x, pos1_y, pos1_z);
		this.modelVertex[1] = new Vector3d(pos2_x, pos2_y, pos2_z);
		this.modelNormal[0] = new Vector3d(norm1_x,norm1_y,norm1_z);
		this.modelNormal[1] = new Vector3d(norm2_x,norm2_y,norm2_z);
		this.rotatedVertex[0] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedVertex[1] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedNormal[0] = new Vector3d(0.0D, 0.0D, 0.0D);
		this.rotatedNormal[1] = new Vector3d(0.0D, 0.0D, 0.0D);
	}

	/**
	 * make obb from aabb
	 * @param aabbCopy
	 */
	public OBBCollider(AxisAlignedBB aabbCopy) {
		super(null, null);
		this.modelVertex = null;
		this.modelNormal = null;
		double xSize = (aabbCopy.maxX - aabbCopy.minX) / 2;
		double ySize = (aabbCopy.maxY - aabbCopy.minY) / 2;
		double zSize = (aabbCopy.maxZ - aabbCopy.minZ) / 2;
		this.worldCenter = new Vector3d(-((float)aabbCopy.minX + xSize), (float)aabbCopy.minY + ySize, -((float)aabbCopy.minZ + zSize));
		this.rotatedVertex = new Vector3d[4];
		this.rotatedNormal = new Vector3d[3];
		this.rotatedVertex[0] = new Vector3d(-xSize, ySize, -zSize);
		this.rotatedVertex[1] = new Vector3d(-xSize, ySize, zSize);
		this.rotatedVertex[2] = new Vector3d(xSize, ySize, zSize);
		this.rotatedVertex[3] = new Vector3d(xSize, ySize, -zSize);
		this.rotatedNormal[0] = new Vector3d(1, 0, 0);
		this.rotatedNormal[1] = new Vector3d(0, 1, 0);
		this.rotatedNormal[2] = new Vector3d(0, 0, 1);
	}

	/**
	 * Transform every elements of this Bounding Box
	 **/
	@Override
	public void transform(OpenMatrix4f modelMatrix) {
		OpenMatrix4f noTranslation = modelMatrix.removeTranslation();

		for (int i = 0; i < this.modelVertex.length; i++) {
			this.rotatedVertex[i] = OpenMatrix4f.transform(noTranslation, this.modelVertex[i]);
		}

		for (int i = 0; i < this.modelNormal.length; i++) {
			this.rotatedNormal[i] = OpenMatrix4f.transform(noTranslation, this.modelNormal[i]);
		}

		this.scale = noTranslation.toScaleVector();

		super.transform(modelMatrix);
	}

	@Override
	protected AxisAlignedBB getHitboxAABB() {
		return this.outerAABB.inflate((this.outerAABB.maxX - this.outerAABB.minX) * this.scale.x,
				(this.outerAABB.maxY - this.outerAABB.minY) * this.scale.y,
				(this.outerAABB.maxZ - this.outerAABB.minZ) * this.scale.z).move(-this.worldCenter.x, this.worldCenter.y, -this.worldCenter.z);
	}

	public boolean isCollide(OBBCollider opponent) {
		Vector3d toOpponent = opponent.worldCenter.subtract(this.worldCenter);

		for (Vector3d seperateAxis : this.rotatedNormal) {
			if (!collisionDetection(seperateAxis, toOpponent, this, opponent)) {
				return false;
			}
		}

		for (Vector3d seperateAxis : opponent.rotatedNormal) {
			if (!collisionDetection(seperateAxis, toOpponent, this, opponent)) {
				return false;
			}
		}

		/** Below codes detect if the line of each obb collides but it has disabled for better performance
		 for(Vector3f norm1 : this.rotatedNormal)
		 {
		 for(Vector3f norm2 : opponent.rotatedNormal)
		 {
		 Vector3f seperateAxis = Vector3f.cross(norm1, norm2, null);

		 if(seperateAxis.x + seperateAxis.y + seperateAxis.z == 0)
		 {
		 continue;
		 }

		 if(!collisionLogic(seperateAxis, toOpponent, this, opponent))
		 {
		 return false;
		 }
		 }
		 }**/

		return true;
	}

	@Override
	public boolean isCollide(Entity entity) {
		OBBCollider obb = new OBBCollider(entity.getBoundingBox());
		return isCollide(obb);
	}

	@Override
	public OBBCollider deepCopy() {
		Vector3d xyzVec = this.modelVertex[1];
		return new OBBCollider(xyzVec.x, xyzVec.y, xyzVec.z, this.modelCenter.x, this.modelCenter.y, this.modelCenter.z);
	}

	private static boolean collisionDetection(Vector3d seperateAxis, Vector3d toOpponent, OBBCollider box1, OBBCollider box2) {
		Vector3d maxProj1 = null, maxProj2 = null, distance;
		double maxDot1 = -1, maxDot2 = -1;
		distance = seperateAxis.dot(toOpponent) > 0.0F ? toOpponent : toOpponent.scale(-1.0D);

		for (Vector3d vertexVector : box1.rotatedVertex) {
			Vector3d temp = seperateAxis.dot(vertexVector) > 0.0F ? vertexVector : vertexVector.scale(-1.0D);
			double dot = seperateAxis.dot(temp);

			if (dot > maxDot1) {
				maxDot1 = dot;
				maxProj1 = temp;
			}
		}

		for (Vector3d vertexVector : box2.rotatedVertex) {
			Vector3d temp = seperateAxis.dot(vertexVector) > 0.0F ? vertexVector : vertexVector.scale(-1.0D);
			double dot = seperateAxis.dot(temp);

			if (dot > maxDot2) {
				maxDot2 = dot;
				maxProj2 = temp;
			}
		}

		return !(MathUtils.projectVector(distance, seperateAxis).length() > MathUtils.projectVector(maxProj1, seperateAxis).length() + MathUtils.projectVector(maxProj2, seperateAxis).length());
	}

	@Override
	public String toString() {
		return super.toString() + " worldCenter : " + this.modelCenter + " direction : " + this.modelVertex[0];
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public RenderType getRenderType() {
		return RenderType.lines();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void drawInternal(MatrixStack poseStack, IVertexBuilder vertexConsumer, Armature armature, Joint joint, Pose pose1, Pose pose2, float partialTicks, int color) {
		int pathIndex = armature.searchPathIndex(joint.getName());
		OpenMatrix4f poseMatrix;
		Pose interpolatedPose = Pose.interpolatePose(pose1, pose2, partialTicks);

		if (pathIndex == -1) {
			JointTransform jt = interpolatedPose.getOrDefaultTransform("Root");
			jt.rotation().x = 0.0F;
			jt.rotation().y = 0.0F;
			jt.rotation().z = 0.0F;
			jt.rotation().w = 1.0F;

			poseMatrix = jt.getAnimationBindedMatrix(armature.rootJoint, new OpenMatrix4f()).removeTranslation();
		} else {
			poseMatrix = armature.getBindedTransformByJointIndex(interpolatedPose, pathIndex);
		}

		OpenMatrix4f transpose = new OpenMatrix4f();
		OpenMatrix4f.transpose(poseMatrix, transpose);
		poseStack.pushPose();
		MathUtils.translateStack(poseStack, poseMatrix);
		MathUtils.rotateStack(poseStack, transpose);
		Matrix4f matrix = poseStack.last().pose();
		Vector3d vec = this.modelVertex[1];
		float maxX = (float)(this.modelCenter.x + vec.x);
		float maxY = (float)(this.modelCenter.y + vec.y);
		float maxZ = (float)(this.modelCenter.z + vec.z);
		float minX = (float)(this.modelCenter.x - vec.x);
		float minY = (float)(this.modelCenter.y - vec.y);
		float minZ = (float)(this.modelCenter.z - vec.z);

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
		size.add(DoubleNBT.valueOf(this.modelVertex[1].x));
		size.add(DoubleNBT.valueOf(this.modelVertex[1].y));
		size.add(DoubleNBT.valueOf(this.modelVertex[1].z));

		resultTag.put("size", size);

		return resultTag;
	}
}