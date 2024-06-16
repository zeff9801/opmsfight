package yesman.epicfight.api.animation;

import yesman.epicfight.api.animation.property.AnimationProperty.MoveCoordSetter;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AnimationPlayer {
	private float elapsedTime;
	private float prevElapsedTime;
	private boolean isEnd;
	private boolean doNotResetNext;
	private boolean reversed;
	private DynamicAnimation play;
	private TransformSheet actionAnimationCoord = new TransformSheet();

	public AnimationPlayer() {
		this.setPlayAnimation(Animations.DUMMY_ANIMATION);
	}

	public void tick(LivingEntityPatch<?> entitypatch) {
		this.prevElapsedTime = this.elapsedTime;
		this.elapsedTime += EpicFightOptions.A_TICK * this.getAnimation().getPlaySpeed(entitypatch) *
				(this.isReversed() && this.getAnimation().canBePlayedReverse() ? -1.0F : 1.0F);

		if (this.elapsedTime >= this.play.getTotalTime()) {
			if (this.play.isRepeat()) {
				this.prevElapsedTime = 0;
				this.elapsedTime %= this.play.getTotalTime();
			} else {
				this.elapsedTime = this.play.getTotalTime();
				this.isEnd = true;
			}
		} else if (this.elapsedTime < 0) {
			if (this.play.isRepeat()) {
				this.prevElapsedTime = this.play.getTotalTime();
				this.elapsedTime = this.play.getTotalTime() + this.elapsedTime;
			} else {
				System.out.println("?? " + this.getAnimation());
				this.elapsedTime = 0.0F;
				this.isEnd = true;
			}
		}
	}
	public void setActionAnimationCoord(DynamicAnimation animation, LivingEntityPatch<?> entitypatch, MoveCoordSetter moveCoordSetter) {
		moveCoordSetter.set(animation, entitypatch, this.actionAnimationCoord);
	}

	public TransformSheet getActionAnimationCoord() {
		return this.actionAnimationCoord;
	}
	public void reset() {
		this.elapsedTime = 0;
		this.prevElapsedTime = 0;
		this.isEnd = false;
	}

	public void setPlayAnimation(DynamicAnimation animation) {
		if (this.doNotResetNext) {
			this.doNotResetNext = false;
		} else {
			this.reset();
		}

		this.play = animation;
	}

	public Pose getCurrentPose(LivingEntityPatch<?> entitypatch, float partialTicks) {
		return this.play.getPoseByTime(entitypatch, this.prevElapsedTime + (this.elapsedTime - this.prevElapsedTime) * partialTicks, partialTicks);
	}

	public float getElapsedTime() {
		return this.elapsedTime;
	}

	public float getPrevElapsedTime() {
		return this.prevElapsedTime;
	}

	public void setElapsedTimeCurrent(float elapsedTime) {
		this.elapsedTime = elapsedTime;
		this.isEnd = false;
	}

	public void setElapsedTime(float elapsedTime) {
		this.elapsedTime = elapsedTime;
		this.prevElapsedTime = elapsedTime;
		this.isEnd = false;
	}

	public void setElapsedTime(float prevElapsedTime, float elapsedTime) {
		this.elapsedTime = elapsedTime;
		this.prevElapsedTime = prevElapsedTime;
		this.isEnd = false;
	}

	public void begin(DynamicAnimation animation, LivingEntityPatch<?> entitypatch) {
		animation.tick(entitypatch);
	}

	public DynamicAnimation getAnimation() {
		return this.play;
	}

	public void markToDoNotReset() {
		this.doNotResetNext = true;
	}

	public boolean isEnd() {
		return this.isEnd;
	}

	public boolean isReversed() {
		return this.reversed;
	}

	public void setReversed(boolean reversed) {
		this.reversed = reversed;
	}

	public boolean isEmpty() {
		return this.play == Animations.DUMMY_ANIMATION;
	}

	@Override
	public String toString() {
		return this.getAnimation() + " " + this.prevElapsedTime + " " + this.elapsedTime;
	}
}