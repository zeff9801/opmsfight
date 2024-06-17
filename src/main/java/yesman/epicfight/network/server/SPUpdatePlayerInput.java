package yesman.epicfight.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SPUpdatePlayerInput {
	private int entityId;
	private float forwardImpulse;
	private float leftImpulse;

	public SPUpdatePlayerInput() {
	}

	public SPUpdatePlayerInput(int entityId, float forwardImpulse, float leftImpulse) {
		this.entityId = entityId;
		this.forwardImpulse = forwardImpulse;
		this.leftImpulse = leftImpulse;
	}

	public static SPUpdatePlayerInput fromBytes(PacketBuffer buf) {
		return new SPUpdatePlayerInput(buf.readInt(), buf.readFloat(), buf.readFloat());
	}

	public static void toBytes(SPUpdatePlayerInput msg, PacketBuffer buf) {
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.forwardImpulse);
		buf.writeFloat(msg.leftImpulse);
	}
	
	public static void handle(SPUpdatePlayerInput msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.player.level.getEntity(msg.entityId);
			
			if (entity instanceof LivingEntity livingentity) {
				livingentity.xxa = msg.leftImpulse;
				livingentity.zza = msg.forwardImpulse;
			}
		});
		ctx.get().setPacketHandled(true);
	}
}