
package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SPRemoveSkill {
	private final String skillName;
	private final SkillSlot skillSlot;

	public SPRemoveSkill() {
		this("", SkillSlots.BASIC_ATTACK);
	}

	public SPRemoveSkill(String name, SkillSlot skillSlotId) {
		this.skillName = name;
		this.skillSlot = skillSlotId;
	}

	public static SPRemoveSkill fromBytes(PacketBuffer buf) {
		SPRemoveSkill msg = new SPRemoveSkill(buf.readUtf(), SkillSlot.ENUM_MANAGER.getOrThrow(buf.readInt()));
		return msg;
	}

	public static void toBytes(SPRemoveSkill msg, PacketBuffer buf) {
		buf.writeUtf(msg.skillName);
		buf.writeInt(msg.skillSlot.universalOrdinal());
	}

	public static void handle(SPRemoveSkill msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			PlayerPatch<?> playerpatch = (PlayerPatch<?>)mc.player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

			if (playerpatch != null) {
				Skill skill = SkillManager.getSkill(msg.skillName);
				playerpatch.getSkillCapability().removeLearnedSkill(skill);
				SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot);

				if (skillContainer.getSkill() == skill) {
					skillContainer.setSkill(null);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
