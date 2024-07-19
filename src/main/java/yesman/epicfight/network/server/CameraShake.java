package yesman.epicfight.network.server;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import yesman.epicfight.api.client.CameraEngine;

import java.util.function.Supplier;

public class CameraShake {
    private final int time;
    private final float strength;
    private final float frequency;

    public CameraShake(int time, float strength, float frequency) {
        this.time = time;
        this.strength = strength;
        this.frequency = frequency;
    }

    public CameraShake(PacketBuffer buf) {
        this.time = buf.readInt();
        this.strength = buf.readFloat();
        this.frequency = buf.readFloat();
    }

    public void encode(PacketBuffer buf) {
        buf.writeInt(this.time);
        buf.writeFloat(this.strength);
        buf.writeFloat(this.frequency);
    }

    public void handle(Supplier<Context> context) {
        context.get().enqueueWork(() -> CameraEngine.getInstance().shakeCamera(this.time, this.strength, this.frequency));
        context.get().setPacketHandled(true);
    }
}
