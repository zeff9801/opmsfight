package yesman.epicfight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.IndexedMessageCodec;
import net.minecraftforge.network.simple.SimpleChannel;
import yesman.epicfight.network.EpicFightNetworkManager;

@Mixin(value = SimpleChannel.class)
public abstract class MixinNetwork {
	@Shadow private IndexedMessageCodec indexedCodec;
	
	@Inject(at = @At(value = "HEAD"), method = "networkEventListener(Lnet/minecraftforge/network/NetworkEvent;)V", cancellable = true, remap = false)
	private void epicfight_networkEventListener(NetworkEvent networkEvent, CallbackInfo info) {
		if (networkEvent.getPayload() == null) {
			return;
		}
		
		if ((Object)this == EpicFightNetworkManager.INSTANCE) {
			EpicFightNetworkManager.testDec(networkEvent, networkEvent.getPayload(), networkEvent.getLoginIndex(), this.indexedCodec);
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "encodeMessage(Ljava/lang/Object;Lnet/minecraft/network/FriendlyByteBuf;)I", cancellable = true, remap = false)
	public void epicfight_encodeMessage(Object message, FriendlyByteBuf target, CallbackInfoReturnable<Integer> info) {
		if ((Object)this == EpicFightNetworkManager.INSTANCE) {
			EpicFightNetworkManager.testEncode(message, target, this.indexedCodec);
		}
    }
	
	/**
	@Inject(at = @At(value = "HEAD"), method = "consume(Lnet/minecraft/network/FriendlyByteBuf;ILjava/util/function/Supplier;)V", cancellable = true)
	private void epicfight_consume(FriendlyByteBuf payload, int payloadIndex, Supplier<NetworkEvent.Context> context, CallbackInfo info) {
		short usgnbt = payload.readUnsignedByte();
		payload.resetReaderIndex();
		System.out.println("2 " + usgnbt);
		
		if (200 <= usgnbt) {
			Object object = this.indicies.get(usgnbt);
			EpicFightNetworkManager.test(payload, payloadIndex, context, object);
		}
	}
	**/
}