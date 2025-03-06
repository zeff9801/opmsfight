package yesman.epicfight.network.client;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import yesman.epicfight.api.animation.AnimationManager;

import java.util.function.Supplier;

public class CPCheckAnimationRegistrySync {
	public final int animationCount;
	public final String[] registryNames;
	
	public CPCheckAnimationRegistrySync() {
		this.animationCount = 0;
		this.registryNames = new String[0];
	}
	
	public CPCheckAnimationRegistrySync(int animationCount, String[] registryNames) {
		this.animationCount = animationCount;
		this.registryNames = registryNames;
	}

	public static CPCheckAnimationRegistrySync fromBytes(PacketBuffer buf) {
		int animationCount = buf.readInt();
		String[] registryNames = new String[animationCount];
		
		for (int i = 0; i < animationCount; i++) {
			registryNames[i] = buf.readUtf();
		}
		
		return new CPCheckAnimationRegistrySync(animationCount, registryNames);
	}

	public static void toBytes(CPCheckAnimationRegistrySync msg, PacketBuffer buf) {
		buf.writeInt(msg.animationCount);
		
		for (String registryName : msg.registryNames) {
			buf.writeUtf(registryName);
		}
	}
	
	public static void handle(CPCheckAnimationRegistrySync msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			AnimationManager.getInstance().validateClientAnimationRegistry(msg, ctx.get().getSender().connection);
		});
		ctx.get().setPacketHandled(true);
	}
}