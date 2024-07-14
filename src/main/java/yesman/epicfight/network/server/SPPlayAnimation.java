package yesman.epicfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Supplier;

public class SPPlayAnimation {
	protected int animationId;
	protected int entityId;
	protected float convertTimeModifier;

	public SPPlayAnimation() {
		this.animationId = 0;
		this.entityId = 0;
		this.convertTimeModifier = 0;
	}

	public SPPlayAnimation(StaticAnimation animation, float convertTimeModifier, LivingEntityPatch<?> entitypatch) {
		this(animation.getId(), entitypatch.getOriginal().getId(), convertTimeModifier);
	}

	public SPPlayAnimation(StaticAnimation animation, int entityId, float convertTimeModifier) {
		this(animation.getId(), entityId, convertTimeModifier);
	}

	public SPPlayAnimation(int animationId, int entityId, float convertTimeModifier) {
		this.animationId = animationId;
		this.entityId = entityId;
		this.convertTimeModifier = convertTimeModifier;
	}


	public <T extends SPPlayAnimation> void onArrive() {
		Minecraft mc = Minecraft.getInstance();
		Entity entity = mc.player.level.getEntity(this.entityId);

		if (entity == null) {
			return;
		}

		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

		if (entitypatch != null) {
			entitypatch.getAnimator().playAnimation(this.animationId, this.convertTimeModifier);
		}
	}

	public static SPPlayAnimation fromBytes(PacketBuffer buf) {
		return new SPPlayAnimation(buf.readInt(), buf.readInt(), buf.readFloat());
	}

	public static void toBytes(SPPlayAnimation msg, ByteBuf buf) {
		buf.writeInt(msg.animationId);
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.convertTimeModifier);
	}

	public static void handle(SPPlayAnimation msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			msg.onArrive();
		});

		ctx.get().setPacketHandled(true);
	}
}