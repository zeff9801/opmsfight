package yesman.epicfight.skill;

import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class BerserkerSkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("fdc09ee8-fcfc-11eb-9a03-0242ac130003");
	
	public BerserkerSkill(Builder<? extends Skill> builder) {
		super(builder);
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		PlayerEventListener listener = container.getExecuter().getEventListener();
		listener.addEventListener(EventType.MODIFY_ATTACK_SPEED_EVENT, EVENT_UUID, (event) -> {
			PlayerEntity player = event.getPlayerPatch().getOriginal();
			float health = player.getHealth();
			float maxHealth = player.getMaxHealth();
			float lostHealthPercentage = (maxHealth - health) / maxHealth;
			lostHealthPercentage = (float)Math.floor(lostHealthPercentage * 100.0F) * 0.005F;
			float attackSpeed = event.getAttackSpeed();
			event.setAttackSpeed(Math.min(5.0F, attackSpeed * (1.0F + lostHealthPercentage)));
		});
		
		listener.addEventListener(EventType.MODIFY_DAMAGE_EVENT, EVENT_UUID, (event) -> {
			PlayerEntity player = event.getPlayerPatch().getOriginal();
			float health = player.getHealth();
			float maxHealth = player.getMaxHealth();
			float lostHealthPercentage = (maxHealth - health) / maxHealth;
			lostHealthPercentage = (float)Math.floor(lostHealthPercentage * 100.0F) * 0.003F;
			float attackDamage = event.getDamage();
			event.setDamage(attackDamage * (1.0F + lostHealthPercentage));
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		container.getExecuter().getEventListener().removeListener(EventType.MODIFY_ATTACK_SPEED_EVENT, EVENT_UUID);
		container.getExecuter().getEventListener().removeListener(EventType.MODIFY_DAMAGE_EVENT, EVENT_UUID);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldDraw(SkillContainer container) {
		PlayerEntity player = container.getExecuter().getOriginal();
		float health = player.getHealth();
		float maxHealth = player.getMaxHealth();
		return (maxHealth - health) > 0.0F;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawOnGui(BattleModeGui gui, SkillContainer container, MatrixStack matStackIn, float x, float y, float scale, int width, int height) {
		matStackIn.pushPose();
		matStackIn.scale(scale, scale, 1.0F);
		matStackIn.translate(0, (float) gui.getSlidingProgression() / scale, 0);
		
		Minecraft mc = Minecraft.getInstance();
		mc.textureManager.bind(this.getSkillTexture());
		
		float scaleMultiply = 1.0F / scale;
		gui.drawTexturedModalRectFixCoord(matStackIn.last().pose(), (width - x) * scaleMultiply, (height - y) * scaleMultiply, 0, 0, 255, 255);
		matStackIn.scale(scaleMultiply, scaleMultiply, 1.0F);
		
		PlayerEntity player = container.getExecuter().getOriginal();
		float health = player.getHealth();
		float maxHealth = player.getMaxHealth();
		float lostHealthPercentage = (maxHealth - health) / maxHealth;
		lostHealthPercentage = (float)Math.floor(lostHealthPercentage * 100.0F);
		gui.font.drawShadow(matStackIn, String.format("%.0f%%", lostHealthPercentage), ((float)width - x+4), ((float)height - y+6), 16777215);
		matStackIn.popPose();
	}
}