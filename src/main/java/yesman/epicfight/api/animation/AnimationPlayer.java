package yesman.epicfight.api.animation;

import com.ibm.icu.impl.Pair;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import yesman.epicfight.api.animation.property.AnimationProperty.PlaybackSpeedModifier;
import yesman.epicfight.api.animation.property.AnimationProperty.PlaybackTimeModifier;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.text.MessageFormat;

public class AnimationPlayer {
	protected float elapsedTime;
	protected float prevElapsedTime;
	protected boolean isEnd;
	protected boolean doNotResetNext;
	protected boolean reversed;
	protected DynamicAnimation play;

	public AnimationPlayer() {
		this.setPlayAnimation(Animations.DUMMY_ANIMATION);
	}

	public void tick(LivingEntityPatch<?> entitypatch) {
		if (this.play == Animations.DUMMY_ANIMATION) {
			return;
		}
		if (!entitypatch.isLogicalClient() && !isVisibleToAnyPlayer(entitypatch, 256.0)) {
			return;
		}
		this.prevElapsedTime = this.elapsedTime;

		float playbackSpeed = this.getAnimation().getPlaySpeed(entitypatch, this.getAnimation());

		PlaybackSpeedModifier playSpeedModifier = this.getAnimation().getRealAnimation().getProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER).orElse(null);

		if (playSpeedModifier != null) {
			playbackSpeed = playSpeedModifier.modify(this.getAnimation(), entitypatch, playbackSpeed, this.prevElapsedTime, this.elapsedTime);
		}

		this.elapsedTime += EpicFightOptions.A_TICK * playbackSpeed * (this.isReversed() && this.getAnimation().canBePlayedReverse() ? -1.0F : 1.0F);
		PlaybackTimeModifier playTimeModifier = this.getAnimation().getRealAnimation().getProperty(StaticAnimationProperty.ELAPSED_TIME_MODIFIER).orElse(null);

		if (playTimeModifier != null) {
			Pair<Float, Float> time = playTimeModifier.modify(this.getAnimation(), entitypatch, playbackSpeed, this.prevElapsedTime, this.elapsedTime);
			this.prevElapsedTime = time.first;
			this.elapsedTime = time.second;
		}

		if (this.elapsedTime > this.play.getTotalTime()) {
			if (this.play.isRepeat()) {
				this.prevElapsedTime = this.prevElapsedTime - this.play.getTotalTime();
				this.elapsedTime %= this.play.getTotalTime();
			} else {
				this.elapsedTime = this.play.getTotalTime();
				this.isEnd = true;
			}
		} else if (this.elapsedTime < 0) {
			if (this.play.isRepeat()) {
				this.prevElapsedTime = this.play.getTotalTime() - this.elapsedTime;
				this.elapsedTime = this.play.getTotalTime() + this.elapsedTime;
			} else {
				this.elapsedTime = 0.0F;
				this.isEnd = true;
			}
		}
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

	//public AssetAccessor<? extends StaticAnimation> getRealAnimation() {
	//	return this.play.get().getRealAnimation();
	//}

	public void markToDoNotReset() {
		this.doNotResetNext = true;
	}

	public boolean isEnd() {
		return this.isEnd;
	}
	public void terminate() {
		this.isEnd = true;
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
		return MessageFormat.format("{0} {1} {2}", this.getAnimation(), this.prevElapsedTime, this.elapsedTime);
	}

	/**
	 * Returns true if the entity is within 'distance' blocks of any player on the server.
	 */
	private boolean isVisibleToAnyPlayer(LivingEntityPatch<?> entitypatch, double distance) {
		if (!(entitypatch.getOriginal().level instanceof ServerWorld)) return true;
		ServerWorld world = (ServerWorld) entitypatch.getOriginal().level;
		for (ServerPlayerEntity player : world.players()) {
			if (player.distanceToSqr(entitypatch.getOriginal()) <= distance * distance) {
				return true;
			}
		}
		return false;
	}
}