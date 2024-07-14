package yesman.epicfight.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPPlayAnimationInstant extends SPPlayAnimation {
	public SPPlayAnimationInstant(int animation, int entityId, float convertTimeModifier) {
		super(animation, entityId, convertTimeModifier);
	}

	public SPPlayAnimationInstant(StaticAnimation animation, float convertTimeModifier, LivingEntityPatch<?> entitypatch) {
		this(animation.getId(), entitypatch.getOriginal().getId(), convertTimeModifier);
	}

	public static SPPlayAnimationInstant fromBytes(PacketBuffer buf) {
		return new SPPlayAnimationInstant(buf.readInt(), buf.readInt(), buf.readFloat());
	}

	@Override
	public void onArrive() {
		Minecraft mc = Minecraft.getInstance();
		Entity entity = mc.player.level.getEntity(this.entityId);

		if (entity == null) {
			return;
		}

		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
		entitypatch.getAnimator().playAnimationInstantly(this.animationId);
	}
}