package yesman.epicfight.client.gui;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class TargetIndicator extends EntityIndicator {
	@Override
	public boolean shouldDraw(LivingEntity entityIn, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch) {
		if (!EpicFightMod.CLIENT_CONFIGS.showTargetIndicator.getValue()) {
			return false;
		} else {
			if (playerpatch != null && entityIn != playerpatch.getTarget()) {
				return false;
			} else if (entityIn.isInvisible() || !entityIn.isAlive() || entityIn == playerpatch.getOriginal()) {
				return false;
			} else if (entityIn.distanceToSqr(Minecraft.getInstance().getCameraEntity()) >= 400) {
				return false;
			} else if (entityIn instanceof PlayerEntity playerIn) {

				return !playerIn.isSpectator();
			}
		}

		return true;
	}

	@Override
	public void drawIndicator(LivingEntity entityIn, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, MatrixStack matStackIn, IRenderTypeBuffer bufferIn, float partialTicks) {
		Matrix4f mvMatrix = super.getMVMatrix(matStackIn, entityIn, 0.0F, entityIn.getBbHeight() + 0.45F, 0.0F, true, partialTicks);

		if (entitypatch == null) {
			this.drawTexturedModalRect2DPlane(mvMatrix, bufferIn.getBuffer(EpicFightRenderTypes.entityIndicator(BATTLE_ICON)), -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33);
		} else {
			if (entityIn.tickCount % 2 == 0 /*&& !entitypatch.flashTargetIndicator(playerpatch) TODO*/) {
				this.drawTexturedModalRect2DPlane(mvMatrix, bufferIn.getBuffer(EpicFightRenderTypes.entityIndicator(BATTLE_ICON)), -0.1F, -0.1F, 0.1F, 0.1F, 132, 0, 167, 36);
			} else {
				this.drawTexturedModalRect2DPlane(mvMatrix, bufferIn.getBuffer(EpicFightRenderTypes.entityIndicator(BATTLE_ICON)), -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33);
			}
		}
	}
}