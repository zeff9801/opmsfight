package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.exception.DatapackException;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;

public class SPDatapackSync {
	protected int count;
	protected int index;
	protected SPDatapackSync.Type type;
	protected CompoundNBT[] tags;

	public SPDatapackSync() {
		this(0, SPDatapackSync.Type.WEAPON);
	}

	public SPDatapackSync(int count, SPDatapackSync.Type type) {
		this.count = count;
		this.index = 0;
		this.type = type;
		this.tags = new CompoundNBT[count];
	}

	public void write(CompoundNBT tag) {
		this.tags[this.index] = tag;
		this.index++;
	}

	public CompoundNBT[] getTags() {
		return this.tags;
	}

	public SPDatapackSync.Type getType() {
		return this.type;
	}

	public static SPDatapackSync fromBytes(PacketBuffer buf) {
		SPDatapackSync msg = new SPDatapackSync(buf.readInt(), SPDatapackSync.Type.values()[buf.readInt()]);

		for (int i = 0; i < msg.count; i++) {
			msg.tags[i] = buf.readNbt();
		}

		try {
			switch (msg.getType()) {
				case MOB -> MobPatchReloadListener.processServerPacket(msg);
				case SKILL_PARAMS -> SkillManager.processServerPacket((SPDatapackSyncSkill)msg);
				case WEAPON -> ItemCapabilityReloadListener.processServerPacket(msg);
				case ARMOR -> ItemCapabilityReloadListener.processServerPacket(msg);
				case WEAPON_TYPE -> WeaponTypeReloadListener.processServerPacket(msg);
			}
		} catch (Exception e) {
			throw new DatapackException(e.getMessage());
		}

		return msg;
	}

	public static void toBytes(SPDatapackSync msg, PacketBuffer buf) {
		buf.writeInt(msg.count);
		buf.writeInt(msg.type.ordinal());

		for (CompoundNBT tag : msg.tags) {
			buf.writeNbt(tag);
		}
	}

	public static void handle(SPDatapackSync msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
		});

		ctx.get().setPacketHandled(true);
	}

	public enum Type {
		ARMOR, WEAPON, MOB, SKILL_PARAMS, WEAPON_TYPE
	}
}