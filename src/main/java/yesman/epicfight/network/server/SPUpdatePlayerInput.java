package yesman.epicfight.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.function.Supplier;

public class SPUpdatePlayerInput {
	private int entityId;
	private float forward;
	private float strafe;

	public SPUpdatePlayerInput() {
	}

	public SPUpdatePlayerInput(int entityId, float forward, float strafe) {
		this.entityId = entityId;
		this.forward = forward;
		this.strafe = strafe;
	}

	public static SPUpdatePlayerInput fromBytes(PacketBuffer buf) {
		return new SPUpdatePlayerInput(buf.readInt(), buf.readFloat(), buf.readFloat());
	}

	public static void toBytes(SPUpdatePlayerInput msg, PacketBuffer buf) {
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.forward);
		buf.writeFloat(msg.strafe);
	}

	public static void handle(SPUpdatePlayerInput msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.player.level.getEntity(msg.entityId);

			entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent((entitypatch) -> {
				if (entitypatch instanceof PlayerPatch<?> plyaerpatch) {
					plyaerpatch.dx = msg.strafe;
					plyaerpatch.dz = msg.forward;
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}