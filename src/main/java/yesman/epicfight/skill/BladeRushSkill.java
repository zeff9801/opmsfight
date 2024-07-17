package yesman.epicfight.skill;

import com.mojang.blaze3d.platform.GlStateManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.SkillDataManager.SkillDataKey;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.List;
import java.util.UUID;

public class BladeRushSkill extends SpecialAttackSkill {
	private static final SkillDataKey<Integer> COMBO_COUNT = SkillDataKey.createDataKey(SkillDataManager.ValueType.INTEGER);
	private static final UUID EVENT_UUID = UUID.fromString("444a1a6a-c2f1-11eb-8529-0242ac130003");
	
	public BladeRushSkill(Builder<? extends Skill> builder) {
		super(builder);
	}
	
	@Override
	public PacketBuffer gatherArguments(LocalPlayerPatch executer, ControllEngine controllEngine) {
		PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
		buf.writeBoolean(true);
		return buf;
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		container.getDataManager().registerData(COMBO_COUNT);
		container.getExecuter().getEventListener().addEventListener(EventType.DEALT_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event) -> {
//			int animationId = event.getDamageSource().getAnimationId(); //TODO Outdated damage source
			int animationId = 0;

			if (animationId >= Animations.BLADE_RUSH_FIRST.getId() && animationId <= Animations.BLADE_RUSH_FINISHER.getId() && !event.getTarget().isAlive()) {
				this.setStackSynchronize(event.getPlayerPatch(), container.stack + 1);
			}
		});
		
		container.getExecuter().getEventListener().addEventListener(EventType.BASIC_ATTACK_EVENT, EVENT_UUID, (event) -> {
			if (container.isActivated() && !container.isDisabled()) {
				PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
				buf.writeBoolean(false);
				this.executeOnServer(event.getPlayerPatch(), buf);
				event.setCanceled(true);
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		container.getExecuter().getEventListener().removeListener(EventType.DEALT_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
		container.getExecuter().getEventListener().removeListener(EventType.BASIC_ATTACK_EVENT, EVENT_UUID);
	}
	
	@Override
	public void executeOnServer(ServerPlayerPatch executer, PacketBuffer args) {
		if (executer.getSkill(this.category).isActivated() && args.readBoolean()) {
			this.cancelOnServer(executer, null);
		} else {
			int firstComboId = Animations.BLADE_RUSH_FIRST.getId();
			int animationId = firstComboId + executer.getSkill(this.category).getDataManager().getDataValue(COMBO_COUNT);
			executer.playAnimationSynchronized(Animations.BLADE_RUSH_TRY, 0);
			executer.getSkill(this.category).getDataManager().setDataF(COMBO_COUNT, (v) -> (v + 1) % 4);
			this.setDurationSynchronize(executer, this.maxDuration);
			this.setStackSynchronize(executer, executer.getSkill(this.category).getStack() - 1);
			executer.getSkill(this.category).activate();
		}
	}
	
	@Override
	public void cancelOnClient(LocalPlayerPatch executer, PacketBuffer args) {
		executer.getSkill(this.category).deactivate();
	}
	
	@Override
	public void cancelOnServer(ServerPlayerPatch executer, PacketBuffer args) {
		executer.getSkill(this.category).deactivate();
		executer.getSkill(this.category).getDataManager().setData(COMBO_COUNT, 0);
		EpicFightNetworkManager.sendToPlayer(new SPSkillExecutionFeedback(this.category.universalOrdinal(), false), executer.getOriginal());
	}
	
	@Override
	public List<ITextComponent> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
		List<ITextComponent> list = super.getTooltipOnItem(itemStack, cap, playerCap);
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Each Strike:");
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(1), "Last Strike:");
		return list;
	}
	
	@Override
	public SpecialAttackSkill registerPropertiesToAnimation() {
		((AttackAnimation)Animations.BLADE_RUSH_FIRST).phases[0].addProperties(this.properties.get(0).entrySet());
		((AttackAnimation)Animations.BLADE_RUSH_SECOND).phases[0].addProperties(this.properties.get(0).entrySet());
		((AttackAnimation)Animations.BLADE_RUSH_THIRD).phases[0].addProperties(this.properties.get(0).entrySet());
		((AttackAnimation)Animations.BLADE_RUSH_FINISHER).phases[0].addProperties(this.properties.get(1).entrySet());
		return this;
	}
	
	@SuppressWarnings("deprecation")
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onScreen(LocalPlayerPatch playerpatch, float resolutionX, float resolutionY) {
		if (playerpatch.getSkill(this.category).isActivated()) {
			Minecraft.getInstance().getTextureManager().bind(new ResourceLocation(EpicFightMod.MODID, "textures/gui/overlay/blade_rush.png"));
			GlStateManager._enableBlend();
			GlStateManager._disableDepthTest();
			GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager._color4f(1, 1, 1, 1);
			Tessellator tessellator = Tessellator.getInstance();
		    BufferBuilder bufferbuilder = tessellator.getBuilder();
		    bufferbuilder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
		    bufferbuilder.vertex(0, 0, 1).uv(0, 0).endVertex();
		    bufferbuilder.vertex(0, resolutionY, 1).uv(0, 1).endVertex();
		    bufferbuilder.vertex(resolutionX, resolutionY, 1).uv(1, 1).endVertex();
		    bufferbuilder.vertex(resolutionX, 0, 1).uv(1, 0).endVertex();
		    tessellator.end();
		}
	}
}