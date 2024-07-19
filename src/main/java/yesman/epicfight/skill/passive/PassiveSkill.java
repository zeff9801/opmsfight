package yesman.epicfight.skill.passive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;

public abstract class PassiveSkill extends Skill {
    public static Skill.Builder<PassiveSkill> createPassiveBuilder() {
        return (new Skill.Builder<PassiveSkill>()).setCategory(SkillCategories.PASSIVE).setResource(Resource.NONE);
    }

    public PassiveSkill(Builder<? extends Skill> builder) {
        super(builder);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, MatrixStack matStackIn, float x, float y, float scale, int width, int height) {
        MatrixStack poseStack = matStackIn;
        poseStack.pushPose();
        poseStack.translate(0, (float)gui.getSlidingProgression(), 0);

        Minecraft mc = Minecraft.getInstance();
        mc.textureManager.bind(this.getSkillTexture());

        gui.drawTexturedModalRectFixCoord(matStackIn.last().pose(), (int)x, (int)y, 0, 0, 255, 255);
        String remainTime = String.format("%.0f", container.getMaxResource() - container.getResource());
        Minecraft.getInstance().font.draw(matStackIn, remainTime, x + 12 - 4 * remainTime.length(), (y+6), 16777215);
        poseStack.popPose();
    }
}