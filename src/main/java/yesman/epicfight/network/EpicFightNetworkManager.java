package yesman.epicfight.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.client.CPChangePlayerMode;
import yesman.epicfight.network.client.CPChangeSkill;
import yesman.epicfight.network.client.CPExecuteSkill;
import yesman.epicfight.network.client.CPPlayAnimation;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.network.server.*;

public class EpicFightNetworkManager {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(EpicFightMod.MODID, "network_manager"),
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public static <MSG> void sendToServer(MSG message) {
		INSTANCE.sendToServer(message);
	}
	
	public static <MSG> void sendToClient(MSG message, PacketTarget packetTarget) {
		INSTANCE.send(packetTarget, message);
	}
	
	public static <MSG> void sendToAll(MSG message) {
		sendToClient(message, PacketDistributor.ALL.noArg());
	}

	public static <MSG> void sendToAllPlayerTrackingThisEntity(MSG message, Entity entity) {
		sendToClient(message, PacketDistributor.TRACKING_ENTITY.with(() -> entity));
	}

	public static <MSG> void sendToPlayer(MSG message, ServerPlayerEntity player) {
		sendToClient(message, PacketDistributor.PLAYER.with(() -> player));
	}

	public static <MSG> void sendToAllPlayerTrackingThisEntityWithSelf(MSG message, ServerPlayerEntity entity) {
		sendToClient(message, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity));
	}

	public static <MSG> void sendToAllPlayerTrackingThisChunkWithSelf(MSG message, Chunk chunk) {
		sendToClient(message, PacketDistributor.TRACKING_CHUNK.with(() -> chunk));
	}


	public static void registerPackets() {
		int id = 0;
		INSTANCE.registerMessage(id++, CPExecuteSkill.class, CPExecuteSkill::toBytes, CPExecuteSkill::fromBytes, CPExecuteSkill::handle);
		INSTANCE.registerMessage(id++, CPPlayAnimation.class, CPPlayAnimation::toBytes, CPPlayAnimation::fromBytes, CPPlayAnimation::handle);
		INSTANCE.registerMessage(id++, CPChangePlayerMode.class, CPChangePlayerMode::toBytes, CPChangePlayerMode::fromBytes, CPChangePlayerMode::handle);
		INSTANCE.registerMessage(id++, CPSetPlayerTarget.class, CPSetPlayerTarget::toBytes, CPSetPlayerTarget::fromBytes, CPSetPlayerTarget::handle);
		INSTANCE.registerMessage(id++, CPChangeSkill.class, CPChangeSkill::toBytes, CPChangeSkill::fromBytes, CPChangeSkill::handle);
		
		INSTANCE.registerMessage(id++, SPChangeSkill.class, SPChangeSkill::toBytes, SPChangeSkill::fromBytes, SPChangeSkill::handle);
		INSTANCE.registerMessage(id++, SPSkillExecutionFeedback.class, SPSkillExecutionFeedback::toBytes, SPSkillExecutionFeedback::fromBytes, SPSkillExecutionFeedback::handle);
		INSTANCE.registerMessage(id++, SPSpawnData.class, SPSpawnData::toBytes, SPSpawnData::fromBytes, SPSpawnData::handle);
		INSTANCE.registerMessage(id++, SPChangeLivingMotion.class, SPChangeLivingMotion::toBytes, SPChangeLivingMotion::fromBytes, SPChangeLivingMotion::handle);
		INSTANCE.registerMessage(id++, SPSetSkillValue.class, SPSetSkillValue::toBytes, SPSetSkillValue::fromBytes, SPSetSkillValue::handle);
		INSTANCE.registerMessage(id++, SPPlayAnimation.class, SPPlayAnimation::toBytes, SPPlayAnimation::fromBytes, SPPlayAnimation::handle);
		INSTANCE.registerMessage(id++, SPPlayAnimationInstant.class, SPPlayAnimation::toBytes, SPPlayAnimationInstant::fromBytes, SPPlayAnimation::handle);
		INSTANCE.registerMessage(id++, SPPlayAnimationAndSetTarget.class, SPPlayAnimationAndSetTarget::toBytes, SPPlayAnimationAndSetTarget::fromBytes, SPPlayAnimationAndSetTarget::handle);
		INSTANCE.registerMessage(id++, SPPlayAnimationAndSyncTransform.class, SPPlayAnimationAndSyncTransform::toBytes, SPPlayAnimationAndSyncTransform::fromBytes, SPPlayAnimationAndSyncTransform::handle);
		INSTANCE.registerMessage(id++, SPPotion.class, SPPotion::toBytes, SPPotion::fromBytes, SPPotion::handle);
		INSTANCE.registerMessage(id++, SPModifySkillData.class, SPModifySkillData::toBytes, SPModifySkillData::fromBytes, SPModifySkillData::handle);
		INSTANCE.registerMessage(id++, SPChangeGamerule.class, SPChangeGamerule::toBytes, SPChangeGamerule::fromBytes, SPChangeGamerule::handle);
		INSTANCE.registerMessage(id++, SPChangePlayerMode.class, SPChangePlayerMode::toBytes, SPChangePlayerMode::fromBytes, SPChangePlayerMode::handle);
		INSTANCE.registerMessage(id++, SPAddSkill.class, SPAddSkill::toBytes, SPAddSkill::fromBytes, SPAddSkill::handle);
		INSTANCE.registerMessage(id++, SPDatapackSync.class, SPDatapackSync::toBytes, SPDatapackSync::fromBytes, SPDatapackSync::handle);
		INSTANCE.registerMessage(id++, SPSetAttackTarget.class, SPSetAttackTarget::toBytes, SPSetAttackTarget::fromBytes, SPSetAttackTarget::handle);
		INSTANCE.registerMessage(id++, SPClearSkills.class, SPClearSkills::toBytes, SPClearSkills::fromBytes, SPClearSkills::handle);
		INSTANCE.registerMessage(id++, SPUpdatePlayerInput.class, SPUpdatePlayerInput::toBytes, SPUpdatePlayerInput::fromBytes, SPUpdatePlayerInput::handle);
		INSTANCE.registerMessage(id++, SPRemoveSkill.class, SPRemoveSkill::toBytes, SPRemoveSkill::fromBytes, SPRemoveSkill::handle);
	}
}