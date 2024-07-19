
package yesman.epicfight.network.client;

import java.util.function.Supplier;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CPExecuteSkill {
	private final int skillSlot;
	private final WorkType workType;
	private final PacketBuffer buffer;

	public CPExecuteSkill() {
		this(0);
	}

	public CPExecuteSkill(int slotIndex) {
		this(slotIndex, WorkType.ACTIVATE);
	}

	public CPExecuteSkill(int slotIndex, WorkType active) {
		this.skillSlot = slotIndex;
		this.workType = active;
		this.buffer = new PacketBuffer(Unpooled.buffer());
	}

	public CPExecuteSkill(int slotIndex, WorkType active, PacketBuffer pb) {
		this.skillSlot = slotIndex;
		this.workType = active;
		this.buffer = new PacketBuffer(Unpooled.buffer());

		if (pb != null) {
			this.buffer.writeBytes(pb);
		}
	}

	public PacketBuffer getBuffer() {
		return buffer;
	}

	public static CPExecuteSkill fromBytes(PacketBuffer buf) {
		CPExecuteSkill msg = new CPExecuteSkill(buf.readInt(), WorkType.values()[buf.readInt()]);

		while (buf.isReadable()) {
			msg.buffer.writeByte(buf.readByte());
		}

		return msg;
	}

	public static void toBytes(CPExecuteSkill msg, PacketBuffer buf) {
		buf.writeInt(msg.skillSlot);
		buf.writeInt(msg.workType.ordinal());

		while (msg.buffer.isReadable()) {
			buf.writeByte(msg.buffer.readByte());
		}
	}

	public static void handle(CPExecuteSkill msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity serverPlayer = ctx.get().getSender();
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(serverPlayer, ServerPlayerPatch.class);
			SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot);

			switch (msg.workType) {
				case ACTIVATE:
					skillContainer.requestExecute(playerpatch, msg.getBuffer());
					break;
				case CANCEL:
					skillContainer.requestCancel(playerpatch, msg.getBuffer());
					break;
				case CHARGING_START:
					skillContainer.requestCharging(playerpatch, msg.getBuffer());
					break;
			}
		});

		ctx.get().setPacketHandled(true);
	}

	public enum WorkType {
		ACTIVATE, CANCEL, CHARGING_START
	}
}
