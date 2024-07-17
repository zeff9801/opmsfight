package yesman.epicfight.network.server;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SPModifyPlayerData {
    public static SPModifyPlayerData setPlayerYRot(int entityId, float yaw) {
        return new SPModifyPlayerData(PacketType.SET_MODEL_YROT, entityId).addData("yaw", yaw);
    }

    public static SPModifyPlayerData disablePlayerYRot(int entityId) {
        return new SPModifyPlayerData(PacketType.YROT_TURN_OFF, entityId);
    }

    public static SPModifyPlayerData setLastAttackResult(int entityId, boolean lastAttackSuccess) {
        return new SPModifyPlayerData(PacketType.LAST_ATTACK_RESULT, entityId).addData("lastAttackSuccess", lastAttackSuccess);
    }

    public static SPModifyPlayerData setPlayerMode(int entityId, PlayerPatch.PlayerMode mode) {
        return new SPModifyPlayerData(PacketType.MODE, entityId).addData("mode", mode);
    }

    public static SPModifyPlayerData setGrapplingTarget(int entityId, Entity grapplingTarget) {
        return new SPModifyPlayerData(PacketType.SET_GRAPPLE_TARGET, entityId).addData("grapplingTarget", grapplingTarget == null ? -1 : grapplingTarget.getId());
    }

    PacketType packetType;
    private final int entityId;
    private final Map<String, Object> data;

    public SPModifyPlayerData() {
        this.packetType = null;
        this.entityId = 0;
        this.data = Maps.newHashMap();
    }

    private SPModifyPlayerData(PacketType packetType, int entityId) {
        this.packetType = packetType;
        this.entityId = entityId;
        this.data = Maps.newHashMap();
    }

    public SPModifyPlayerData addData(String key, Object val) {
        this.data.put(key, val);
        return this;
    }

    public static SPModifyPlayerData fromBytes(PacketBuffer  buf) {
        PacketType packetType = PacketType.values()[buf.readInt()];
        SPModifyPlayerData packet = new SPModifyPlayerData(packetType, buf.readInt());
        packetType.decoder.accept(packet, buf);

        return packet;
    }

    public static void toBytes(SPModifyPlayerData msg, PacketBuffer buf) {
        buf.writeInt(msg.packetType.ordinal());
        buf.writeInt(msg.entityId);
        msg.packetType.encoder.accept(msg, buf);
    }

    public static void handle(SPModifyPlayerData msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.player.level.getEntity(msg.entityId);

            if (entity != null) {
                if (entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null) instanceof PlayerPatch<?> playerpatch) {
                    switch (msg.packetType) {
                        case SET_MODEL_YROT:
                            playerpatch.setModelYRot((float)msg.data.get("yaw"), false);
                            break;
                        case YROT_TURN_OFF:
                            playerpatch.disableModelYRot(false);
                        case MODE:
                            playerpatch.toMode((PlayerPatch.PlayerMode)msg.data.get("mode"), false);
                            break;
                        case LAST_ATTACK_RESULT:
                            playerpatch.setLastAttackSuccess((boolean)msg.data.get("lastAttackSuccess"));
                            break;
                        case SET_GRAPPLE_TARGET:
                            Entity grapplingTarget = mc.player.level.getEntity((int)msg.data.get("grapplingTarget"));

                            if (grapplingTarget instanceof LivingEntity) {
                                playerpatch.setGrapplingTarget((LivingEntity)grapplingTarget);
                            } else {
                                playerpatch.setGrapplingTarget(null);
                            }

                            break;
                    }
                }
            }
        });

        ctx.get().setPacketHandled(true);
    }

    public static enum State {
        ENABLE(false), DISABLE(true);

        boolean setter;

        State(boolean setter) {
            this.setter = setter;
        }
    }

    public enum PacketType {
        SET_MODEL_YROT((packet, buffer) -> {
            buffer.writeFloat((float)packet.data.get("yaw"));
        }, (packet, buffer) -> {
            packet.addData("yaw", buffer.readFloat());
        }),
        YROT_TURN_OFF((packet, buffer) -> {
        }, (packet, buffer) -> {
        }),
        MODE((packet, buffer) -> {
            buffer.writeInt(((PlayerPatch.PlayerMode)packet.data.get("mode")).ordinal());
        }, (packet, buffer) -> {
            packet.addData("mode", PlayerPatch.PlayerMode.values()[buffer.readInt()]);
        }),
        LAST_ATTACK_RESULT((packet, buffer) -> {
            buffer.writeBoolean((boolean)packet.data.get("lastAttackSuccess"));
        }, (packet, buffer) -> {
            packet.addData("lastAttackSuccess", buffer.readBoolean());
        }),
        SET_GRAPPLE_TARGET((packet, buffer) -> {
            buffer.writeInt((int)packet.data.get("grapplingTarget"));
        }, (packet, buffer) -> {
            packet.addData("grapplingTarget", buffer.readInt());
        })
        ;

        BiConsumer<SPModifyPlayerData, PacketBuffer > encoder;
        BiConsumer<SPModifyPlayerData, PacketBuffer > decoder;

        PacketType(BiConsumer<SPModifyPlayerData, PacketBuffer> encoder, BiConsumer<SPModifyPlayerData, PacketBuffer> decoder) {
            this.encoder = encoder;
            this.decoder = decoder;
        }
    }
}
