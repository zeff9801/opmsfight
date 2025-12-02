package yesman.epicfight.api.animation;

import com.joml.Quaternionf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import yesman.epicfight.api.utils.VectorUtils;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransformSheet {
	public static final TransformSheet EMPTY_SHEET = new TransformSheet(List.of(new Keyframe(0.0F, JointTransform.empty()), new Keyframe(Float.MAX_VALUE, JointTransform.empty())));
	public static final Function<Vector3d, TransformSheet> EMPTY_SHEET_PROVIDER = translation -> {
		return new TransformSheet(List.of(new Keyframe(0.0F, JointTransform.translation(new Vec3f(translation))), new Keyframe(Float.MAX_VALUE, JointTransform.empty())));
	};

	private Keyframe[] keyframes;

	public TransformSheet() {
		this(new Keyframe[0]);
	}

	public TransformSheet(int size) {
		this(new Keyframe[size]);
	}

	public TransformSheet(List<Keyframe> keyframeList) {
		this(keyframeList.toArray(new Keyframe[0]));
	}

	public TransformSheet(Keyframe[] keyframes) {
		this.keyframes = keyframes;
	}

	public JointTransform getStartTransform() {
		return this.keyframes[0].transform();
	}

	public Keyframe[] getKeyframes() {
		return this.keyframes;
	}

	public TransformSheet copyAll() {
		return this.copy(0, this.keyframes.length);
	}

	public TransformSheet copy(int start, int end) {
		int len = end - start;
		Keyframe[] newKeyframes = new Keyframe[len];

		for (int i = 0; i < len; i++) {
			Keyframe kf = this.keyframes[i + start];
			newKeyframes[i] = new Keyframe(kf);
		}

		return new TransformSheet(newKeyframes);
	}

	public TransformSheet readFrom(TransformSheet opponent) {
		if (opponent.keyframes.length != this.keyframes.length) {
			this.keyframes = new Keyframe[opponent.keyframes.length];

			for (int i = 0; i < this.keyframes.length; i++) {
				this.keyframes[i] = Keyframe.empty();
			}
		}

		for (int i = 0; i < this.keyframes.length; i++) {
			this.keyframes[i].copyFrom(opponent.keyframes[i]);
		}

		return this;
	}

	public TransformSheet createInterpolated(float[] timestamp) {
		TransformSheet interpolationCreated = new TransformSheet(timestamp.length);

		for (int i = 0; i < timestamp.length; i++) {
			interpolationCreated.keyframes[i] = new Keyframe(timestamp[i], this.getInterpolatedTransform(timestamp[i]));
		}

		return interpolationCreated;
	}

	/**
	 * Transform each joint
	 */
	public void forEach(BiConsumer<Integer, Keyframe> task) {
		this.forEach(task, 0, this.keyframes.length);
	}

	public void forEach(BiConsumer<Integer, Keyframe> task, int start, int end) {
		end = Math.min(end, this.keyframes.length);

		for (int i = start; i < end; i++) {
			task.accept(i, this.keyframes[i]);
		}
	}

	public void transform(Consumer<JointTransform> transformFunc) {
		this.transform(transformFunc, 0, this.keyframes.length);
	}

	public void transform(Consumer<JointTransform> transformFunc, int start, int end) {
		this.forEach((idx, keyframe) -> transformFunc.accept(keyframe.transform()), start, end);
	}

	public Vec3f getInterpolatedTranslation(float currentTime) {
		return this.getInterpolatedTranslation(currentTime, null);
	}

	public Vec3f getInterpolatedTranslation(float currentTime, Vec3f dest) {
		InterpolationInfo interpolInfo = this.getInterpolationInfo(currentTime);

		if (interpolInfo == InterpolationInfo.INVALID) {
			return dest == null ? new Vec3f() : dest.set(Vec3f.ZERO);
		}

		return MathUtils.lerpVector(this.keyframes[interpolInfo.prev].transform().translation(), this.keyframes[interpolInfo.next].transform().translation(), interpolInfo.delta, dest);
	}

	public Quaternionf getInterpolatedRotation(float currentTime) {
		InterpolationInfo interpolInfo = this.getInterpolationInfo(currentTime);

		if (interpolInfo == InterpolationInfo.INVALID) {
			return new Quaternionf();
		}

		Quaternionf quat = MathUtils.lerpQuaternion(this.keyframes[interpolInfo.prev].transform().rotation(), this.keyframes[interpolInfo.next].transform().rotation(), interpolInfo.delta);
		return quat;
	}

	public JointTransform getInterpolatedTransform(float currentTime) {
		return this.getInterpolatedTransform(this.getInterpolationInfo(currentTime));
	}

	public JointTransform getInterpolatedTransform(InterpolationInfo interpolationInfo) {
		if (interpolationInfo == InterpolationInfo.INVALID) {
			return JointTransform.empty();
		}

		JointTransform trasnform = JointTransform.interpolate(this.keyframes[interpolationInfo.prev].transform(), this.keyframes[interpolationInfo.next].transform(), interpolationInfo.delta);
		return trasnform;
	}

	public TransformSheet extend(TransformSheet target) {
		int newKeyLength = this.keyframes.length + target.keyframes.length;
		Keyframe[] newKeyfrmaes = new Keyframe[newKeyLength];

		for (int i = 0; i < this.keyframes.length; i++) {
			newKeyfrmaes[i] = this.keyframes[i];
		}

		for (int i = this.keyframes.length; i < newKeyLength; i++) {
			newKeyfrmaes[i] = new Keyframe(target.keyframes[i - this.keyframes.length]);
		}

		this.keyframes = newKeyfrmaes;

		return this;
	}

	public TransformSheet getFirstFrame() {
		TransformSheet part = this.copy(0, 2);
		Keyframe[] keyframes = part.getKeyframes();
		keyframes[1].transform().copyFrom(keyframes[0].transform());

		return part;
	}

	public void correctAnimationByNewPosition(Vec3f startpos, Vec3f startToEnd, Vec3f modifiedStart, Vec3f modifiedStartToEnd) {
		Keyframe[] keyframes = this.getKeyframes();
		Keyframe startKeyframe = keyframes[0];
		Keyframe endKeyframe = keyframes[keyframes.length - 1];
		float pitchDeg = (float) Math.toDegrees(MathHelper.atan2(modifiedStartToEnd.y - startToEnd.y, modifiedStartToEnd.length()));
		float yawDeg = (float) MathUtils.getAngleBetween(modifiedStartToEnd.copy().multiply(1.0F, 0.0F, 1.0F), startToEnd.copy().multiply(1.0F, 0.0F, 1.0F));

		for (Keyframe kf : keyframes) {
			float lerp = (kf.time() - startKeyframe.time()) / (endKeyframe.time() - startKeyframe.time());
			Vec3f line = MathUtils.lerpVector(new Vec3f(0F, 0F, 0F), startToEnd, lerp);
			Vec3f modifiedLine = MathUtils.lerpVector(new Vec3f(0F, 0F, 0F), modifiedStartToEnd, lerp);
			Vec3f keyTransform = kf.transform().translation();
			Vec3f startToKeyTransform = keyTransform.copy().sub(startpos).multiply(-1.0F, 1.0F, -1.0F);
			Vec3f animOnLine = startToKeyTransform.copy().sub(line);
			OpenMatrix4f rotator = OpenMatrix4f.createRotatorDeg(pitchDeg, Vec3f.X_AXIS).mulFront(OpenMatrix4f.createRotatorDeg(yawDeg, Vec3f.Y_AXIS));
			Vec3f toNewKeyTransform = modifiedLine.add(OpenMatrix4f.transform3v(rotator, animOnLine, null));
			keyTransform.set(modifiedStart.copy().add((toNewKeyTransform)));
		}
	}

	public TransformSheet getCorrectedModelCoord(LivingEntityPatch<?> entitypatch, Vector3d start, Vector3d dest, int startFrame, int endFrame) {
		TransformSheet transform = this.copyAll();
		//float horizontalDistance = (float) dest.subtract(start).horizontalDistance();
		float horizontalDistance = (float) VectorUtils.horizontalDistance(dest.subtract(start));
		float verticalDistance = (float) Math.abs(dest.y - start.y);
		JointTransform startJt = transform.getKeyframes()[startFrame].transform();
		JointTransform endJt = transform.getKeyframes()[endFrame].transform();
		Vec3f jointCoord = new Vec3f(startJt.translation().x, verticalDistance, horizontalDistance);

		startJt.translation().set(jointCoord);

		for (int i = startFrame + 1; i < endFrame; i++) {
			JointTransform middleJt = transform.getKeyframes()[i].transform();
			middleJt.translation().set(MathUtils.lerpVector(startJt.translation(), endJt.translation(), transform.getKeyframes()[i].time() / transform.getKeyframes()[endFrame].time()));
		}

		return transform;
	}

	public TransformSheet extendsZCoord(float multiplier, int startFrame, int endFrame) {
		TransformSheet transform = this.copyAll();
		float extend = 0.0F;

		for (int i = 0; i < endFrame + 1; i++) {
			Keyframe kf = transform.getKeyframes()[i];
			float prevZ = kf.transform().translation().z;
			kf.transform().translation().multiply(1.0F, 1.0F, multiplier);
			float extendedZ = kf.transform().translation().z;
			extend = extendedZ - prevZ;
		}

		for (int i = endFrame + 1; i < transform.getKeyframes().length; i++) {
			Keyframe kf = transform.getKeyframes()[i];
			kf.transform().translation().add(0.0F, 0.0F, extend);
		}

		return transform;
	}

	/**
	 * Transform the animation coord system to world coord system regarding origin point as @param worldDest
	 *
	 * @param entitypatch
	 * @param worldStart
	 * @param worldDest
	 * @param xRot
	 * @param entityYRot
	 * @param startFrame
	 * @param endFrame
	 * @return
	 */
	public TransformSheet transformToWorldCoordOriginAsDest(LivingEntityPatch<?> entitypatch, Vector3d startInWorld, Vector3d destInWorld, float entityYRot, float destYRot, int startFrmae, int destFrame) {
		if (this.keyframes.length == 0) {
			return new TransformSheet();
		}

		final int destFrameIndex = MathHelper.clamp(destFrame, 0, this.keyframes.length - 1);
		final int startFrameIndex = MathHelper.clamp(startFrmae, 0, destFrameIndex);

		if (destFrameIndex == 0) {
			return this.copyAll();
		}

		TransformSheet byStart = this.copy(0, destFrameIndex + 1);
		TransformSheet byDest = this.copy(0, destFrameIndex + 1);
		TransformSheet result = new TransformSheet(destFrameIndex + 1);
		Vector3d toTargetInWorld = destInWorld.subtract(startInWorld);
		double worldMagnitude = VectorUtils.horizontalDistance(toTargetInWorld);
		Vec3f animVector = this.keyframes[destFrameIndex].transform().translation().copy().sub(this.keyframes[0].transform().translation());
		double animMagnitude = animVector.horizontalDistance();
		float scale = animMagnitude <= 1.0E-5D ? 1.0F : (float)(worldMagnitude / animMagnitude);

		byStart.forEach((idx, keyframe) -> {
			keyframe.transform().translation().sub(this.keyframes[0].transform().translation());
			keyframe.transform().translation().multiply(1.0F, 1.0F, scale);
			keyframe.transform().translation().rotate(-entityYRot, Vec3f.Y_AXIS);
			keyframe.transform().translation().multiply(-1.0F, 1.0F, -1.0F);
			keyframe.transform().translation().add(startInWorld);
		});

		byDest.forEach((idx, keyframe) -> {
			keyframe.transform().translation().multiply(1.0F, 1.0F, MathHelper.lerp((idx / (float)destFrameIndex), scale, 1.0F));
			keyframe.transform().translation().rotate(-destYRot, Vec3f.Y_AXIS);
			keyframe.transform().translation().multiply(-1.0F, 1.0F, -1.0F);
			keyframe.transform().translation().add(destInWorld);
		});

		for (int i = 0; i < destFrameIndex + 1; i++) {
			if (i <= startFrameIndex) {
				result.getKeyframes()[i] = new Keyframe(this.keyframes[i].time(), JointTransform.translation(byStart.getKeyframes()[i].transform().translation()));
			} else {
				float lerp = this.keyframes[i].time() == 0.0F ? 0.0F : this.keyframes[i].time() / this.keyframes[destFrameIndex].time();
				Vec3f lerpTranslation = Vec3f.interpolate(byStart.getKeyframes()[i].transform().translation(), byDest.getKeyframes()[i].transform().translation(), lerp, null);
				result.getKeyframes()[i] = new Keyframe(this.keyframes[i].time(), JointTransform.translation(lerpTranslation));
			}
		}

		if (this.keyframes.length > destFrameIndex) {
			TransformSheet behindDestination = this.copy(destFrameIndex + 1, this.keyframes.length);

			behindDestination.forEach((idx, keyframe) -> {
				keyframe.transform().translation().sub(this.keyframes[destFrameIndex].transform().translation());
				keyframe.transform().translation().rotate(entityYRot, Vec3f.Y_AXIS);
				keyframe.transform().translation().multiply(-1.0F, 1.0F, -1.0F);
				keyframe.transform().translation().add(result.getKeyframes()[destFrameIndex].transform().translation());
			});

			result.extend(behindDestination);
		}

		return result;
	}

	public TransformSheet getCorrectedWorldCoord(LivingEntityPatch<?> entitypatch, Vector3d start, Vector3d dest, float xRot, float yRot, int startFrame, int endFrame) {
		TransformSheet newTransformSheet = this.copyAll();

		if (newTransformSheet.keyframes.length == 0) {
			return newTransformSheet;
		}

		Vec3f firstPos = newTransformSheet.keyframes[0].transform().translation().copy();
		newTransformSheet.transform((jt) -> jt.translation().sub(firstPos));

		int clampedStart = MathHelper.clamp(startFrame, 0, Math.max(0, newTransformSheet.keyframes.length - 1));
		int clampedEnd = MathHelper.clamp(endFrame, clampedStart + 1, newTransformSheet.keyframes.length);
		Vec3f fromCoord = newTransformSheet.keyframes[clampedStart].transform().translation().copy();
		Vec3f toCoord = newTransformSheet.keyframes[clampedEnd - 1].transform().translation().copy();
		float originalDistance = (float)Math.sqrt(fromCoord.distanceSqr(toCoord));
		float worldDistance = (float)Math.sqrt(dest.distanceToSqr(start));
		float ratio = originalDistance <= 1.0E-5F ? 1.0F : worldDistance / originalDistance;

		Vec3f fromCoordFinal = fromCoord.copy();
		newTransformSheet.transform((jt) -> {
			Vec3f kfTranslation = jt.translation();
			kfTranslation.set(-kfTranslation.x, kfTranslation.y, kfTranslation.z > 0.0F ? kfTranslation.z : kfTranslation.z * ratio);
			Vec3f relativeCoord = Vec3f.rotate(xRot, Vec3f.X_AXIS, Vec3f.sub(kfTranslation, fromCoordFinal, null), null);
			kfTranslation.set(Vec3f.add(fromCoordFinal, relativeCoord, null));
		}, clampedStart, clampedEnd);

		newTransformSheet.transform((jt) -> {
			jt.translation().rotate(yRot, Vec3f.Y_AXIS);
			jt.translation().multiply(1.0F, 1.0F, -1.0F);
			jt.translation().add((float)start.x, (float)start.y, (float)start.z);
		});

		return newTransformSheet;
	}

	public InterpolationInfo getInterpolationInfo(float currentTime) {
		if (this.keyframes.length == 0) {
			return InterpolationInfo.INVALID;
		}

		if (currentTime < 0.0F) {
			currentTime = this.keyframes[this.keyframes.length - 1].time() + currentTime;
		}

		// Binary search
		int begin = 0, end = this.keyframes.length - 1;

		while (end - begin > 1) {
			int i = begin + (end - begin) / 2;

			if (this.keyframes[i].time() <= currentTime && this.keyframes[i+1].time() > currentTime) {
				begin = i;
				end = i+1;
				break;
			} else {
				if (this.keyframes[i].time() > currentTime) {
					end = i;
				} else if (this.keyframes[i+1].time() <= currentTime) {
					begin = i;
				}
			}
		}

		float progression = MathHelper.clamp((currentTime - this.keyframes[begin].time()) / (this.keyframes[end].time() - this.keyframes[begin].time()), 0.0F, 1.0F);
		return new InterpolationInfo(begin, end, Float.isNaN(progression) ? 1.0F : progression);
	}

	public float maxFrameTime() {
		float maxFrameTime = -1.0F;

		for (Keyframe kf : this.keyframes) {
			if (kf.time() > maxFrameTime) {
				maxFrameTime = kf.time();
			}
		}

		return maxFrameTime;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int idx = 0;

		for (Keyframe kf : this.keyframes) {
			sb.append(kf);

			if (++idx < this.keyframes.length) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static record InterpolationInfo(int prev, int next, float delta) {
		public static final InterpolationInfo INVALID = new InterpolationInfo(-1, -1, -1.0F);
	}
}
