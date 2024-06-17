package yesman.epicfight.client.gui;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.IVertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.effect.VisibleMobEffect;

import java.util.Collection;
import java.util.Iterator;

@OnlyIn(Dist.CLIENT)
public class HealthBarIndicator extends EntityIndicator {
	@Override
	public boolean shouldDraw(LivingEntity entityIn, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch) {
		ClientConfig.HealthBarShowOptions option = ClientConfig.HealthBarShowOptions.TARGET; //TODO Change
		Minecraft mc = Minecraft.getInstance();

		if (option == ClientConfig.HealthBarShowOptions.NONE) {
			return false;
		} else if (!entityIn.canChangeDimensions() || entityIn.isInvisible() || entityIn == playerpatch.getOriginal().getVehicle()) {
			return false;
		} else if (entityIn.distanceToSqr(mc.getCameraEntity()) >= 400) {
			return false;
		} else if (entityIn instanceof PlayerEntity playerIn) {
			if (playerIn == playerpatch.getOriginal() && playerpatch.getMaxStunShield() <= 0.0F) {
				return false;
			} else if (playerIn.isCreative() || playerIn.isSpectator()) {
				return false;
			}
		}

		if (option == ClientConfig.HealthBarShowOptions.TARGET) {
			return playerpatch.getTarget() == entityIn;
		}

		return (!entityIn.getActiveEffects().isEmpty() || !(entityIn.getHealth() >= entityIn.getMaxHealth())) && entityIn.deathTime < 19;
	}

	@Override
	public void drawIndicator(LivingEntity entityIn, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, MatrixStack matStackIn, IRenderTypeBuffer bufferIn, float partialTicks) {
		Matrix4f mvMatrix = super.getMVMatrix(matStackIn, entityIn, 0.0F, entityIn.getBbHeight() + 0.25F, 0.0F, true, partialTicks);
		Collection<EffectInstance> activeEffects = entityIn.getActiveEffects();

		if (!activeEffects.isEmpty() && !entityIn.is(playerpatch.getOriginal())) {
			Iterator<EffectInstance> iter = activeEffects.iterator();
			int acives = activeEffects.size();
			int row = acives > 1 ? 1 : 0;
			int column = ((acives-1) / 2);
			float startX = -0.8F + -0.3F * row;
			float startY = -0.15F + 0.15F * column;

			for (int i = 0; i <= column; i++) {
				for (int j = 0; j <= row; j++) {
					EffectInstance effectInstance = iter.next();
					Effect effect = effectInstance.getEffect();
					ResourceLocation rl;

					if (effect instanceof VisibleMobEffect visibleMobEffect) {
						//rl = visibleMobEffect.getIcon(effectInstance);
						rl = ((VisibleMobEffect)effect).getIcon();
					} else {
						rl = new ResourceLocation(ForgeRegistries.POTIONS.getKey(effect).getNamespace(), "textures/mob_effect/" + ForgeRegistries.POTIONS.getKey(effect).getPath() + ".png");
					}

					Minecraft.getInstance().getTextureManager().bind(rl);
					float x = startX + 0.3F * j;
					float y = startY + -0.3F * i;

					IVertexBuilder vertexBuilder1 = bufferIn.getBuffer(EpicFightRenderTypes.entityIndicator(rl));

					this.drawTexturedModalRect2DPlane(mvMatrix, vertexBuilder1, x, y, x + 0.3F, y + 0.3F, 0, 0, 256, 256);
					if (!iter.hasNext()) {
						break;
					}
				}
			}
		}

		IVertexBuilder vertexBuilder = bufferIn.getBuffer(EpicFightRenderTypes.entityIndicator(BATTLE_ICON));

		float ratio = MathHelper.clamp(entityIn.getHealth() / entityIn.getMaxHealth(), 0.0F, 1.0F);
		float healthRatio = -0.5F + ratio;
		int textureRatio = (int) (62 * ratio);
		this.drawTexturedModalRect2DPlane(mvMatrix, vertexBuilder, -0.5F, -0.05F, healthRatio, 0.05F, 1, 15, textureRatio, 20);
		this.drawTexturedModalRect2DPlane(mvMatrix, vertexBuilder, healthRatio, -0.05F, 0.5F, 0.05F, textureRatio, 10, 62, 15);
		float absorption = entityIn.getAbsorptionAmount();

		if (absorption > 0.0D) {
			float absorptionRatio = MathHelper.clamp(absorption / entityIn.getMaxHealth(), 0.0F, 1.0F);
			int absTexRatio = (int) (62 * absorptionRatio);
			this.drawTexturedModalRect2DPlane(mvMatrix, vertexBuilder, -0.5F, -0.05F, absorptionRatio - 0.5F, 0.05F, 1, 20, absTexRatio, 25);
		}

		if (entitypatch != null) {
			this.renderStunShield(entitypatch, mvMatrix, vertexBuilder);
		}
	}

	private void renderStunShield(LivingEntityPatch<?> entitypatch, Matrix4f mvMatrix, IVertexBuilder vertexBuilder) {
		if (entitypatch.getStunShield() == 0) {
			return;
		}

		float ratio = MathHelper.clamp(entitypatch.getStunShield() / entitypatch.getMaxStunShield(), 0.0F, 1.0F);
		float barRatio = -0.5F + ratio;
		int textureRatio = (int) (62 * ratio);

		this.drawTexturedModalRect2DPlane(mvMatrix, vertexBuilder, -0.5F, -0.1F, barRatio, -0.05F, 1, 5, textureRatio, 10);
		this.drawTexturedModalRect2DPlane(mvMatrix, vertexBuilder, barRatio, -0.1F, 0.5F, -0.05F, textureRatio, 0, 63, 5);
	}
}