package yesman.epicfight.network.client;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPModifyPlayerData;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class CPModifyEntityModelYRot {
    private final float modelYRot;
    private final boolean disable;

    public CPModifyEntityModelYRot() {
        this.modelYRot = 0F;
        this.disable = true;
    }

    public CPModifyEntityModelYRot(float degree) {
        this.modelYRot = degree;
        this.disable = false;
    }

    private CPModifyEntityModelYRot(float degree, boolean disable) {
        this.modelYRot = degree;
        this.disable = disable;
    }

    public static CPModifyEntityModelYRot fromBytes(PacketBuffer buf) {
        return new CPModifyEntityModelYRot(buf.readFloat(), buf.readBoolean());
    }

    public static void toBytes(CPModifyEntityModelYRot msg, PacketBuffer buf) {
        buf.writeFloat(msg.modelYRot);
        buf.writeBoolean(msg.disable);
    }

    public static void handle(CPModifyEntityModelYRot msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(()->{
            ServerPlayerEntity player = ctx.get().getSender();

            if (player != null) {
                PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);

                if (playerpatch != null) {
                    if (msg.disable) {
                        playerpatch.disableModelYRot(false);
                        EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(SPModifyPlayerData.disablePlayerYRot(player.getId()), player);
                    } else {
                        playerpatch.setModelYRot(msg.modelYRot, false);
                        EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(SPModifyPlayerData.setPlayerYRot(player.getId(), msg.modelYRot), player);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}