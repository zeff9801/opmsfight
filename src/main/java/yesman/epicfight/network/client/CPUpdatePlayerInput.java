package yesman.epicfight.network.client;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPUpdatePlayerInput;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.function.Supplier;

public class CPUpdatePlayerInput {
	private int entityId;
	private float forward;
	private float strafe;

	public CPUpdatePlayerInput() {
	}

	public CPUpdatePlayerInput(int entityId, float forward, float strafe) {
		this.entityId = entityId;
		this.forward = forward;
		this.strafe = strafe;
	}

	public static CPUpdatePlayerInput fromBytes(PacketBuffer buf) {
		return new CPUpdatePlayerInput(buf.readInt(), buf.readFloat(), buf.readFloat());
	}

	public static void toBytes(CPUpdatePlayerInput msg, PacketBuffer buf) {
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.forward);
		buf.writeFloat(msg.strafe);
	}
	
	public static void handle(CPUpdatePlayerInput msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity player = ctx.get().getSender();
			
			player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent((entitypatch) -> {
				if (entitypatch instanceof PlayerPatch<?> plyaerpatch) {
					plyaerpatch.dx = msg.strafe;
					plyaerpatch.dz = msg.forward;
					EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPUpdatePlayerInput(msg.entityId, msg.forward, msg.strafe), player);
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}