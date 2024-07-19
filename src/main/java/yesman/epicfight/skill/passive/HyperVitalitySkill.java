package yesman.epicfight.skill.passive;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.UUID;

public class HyperVitalitySkill extends PassiveSkill {
    private static final UUID EVENT_UUID = UUID.fromString("06fb3f66-b900-11ed-afa1-0242ac120002");

    public HyperVitalitySkill(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        container.getExecuter().getEventListener().addEventListener(EventType.SKILL_CONSUME_EVENT, EVENT_UUID, (event) -> {
            if (!container.getExecuter().getSkill(event.getSkill()).isDisabled() && event.getSkill().getCategory() == SkillCategories.WEAPON_INNATE) {
                PlayerPatch<?> playerpatch = event.getPlayerPatch();

                if (playerpatch.getSkill(SkillSlots.WEAPON_INNATE).getStack() < 1) {
                    if (container.getStack() > 0 && !playerpatch.getOriginal().isCreative()) {
                        float consumption = event.getSkill().getConsumption();

                        if (playerpatch.consumeForSkill(this, Skill.Resource.STAMINA, consumption * 0.1F)) {
                            event.setResourceType(Skill.Resource.NONE);
                            container.setMaxResource(consumption * 0.2F);

                            if (!container.getExecuter().isLogicalClient()) {
                                container.setMaxDuration(event.getSkill().getMaxDuration());
                                container.activate();
                                EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.executed(container.getSlotId()), (ServerPlayerEntity) playerpatch.getOriginal());
                            }
                        }
                    }
                }
            }
        }, 1);

        container.getExecuter().getEventListener().addEventListener(EventType.SKILL_CANCEL_EVENT, EVENT_UUID, (event) -> {
            if (!container.getExecuter().isLogicalClient() && !container.getExecuter().getOriginal().isCreative() && event.getSkillContainer().getSkill().getCategory() == SkillCategories.WEAPON_INNATE && container.isActivated()) {
                container.setResource(0.0F);
                container.deactivate();
                ServerPlayerPatch serverPlayerPatch = (ServerPlayerPatch)container.getExecuter();
                this.setStackSynchronize(serverPlayerPatch, container.getStack() - 1);
                EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.executed(container.getSlotId()), serverPlayerPatch.getOriginal());
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);

        container.getExecuter().getEventListener().removeListener(EventType.SKILL_CONSUME_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(EventType.SKILL_CANCEL_EVENT, EVENT_UUID);
    }

    @Override
    public void executeOnClient(LocalPlayerPatch executer, PacketBuffer args) {
        super.executeOnClient(executer, args);
        executer.getSkill(this).activate();
    }

    @Override
    public void cancelOnClient(LocalPlayerPatch executer, PacketBuffer args) {
        super.cancelOnClient(executer, args);
        executer.getSkill(this).deactivate();
    }

    @Override
    public boolean shouldDraw(SkillContainer container) {
        return container.isActivated() || container.getStack() == 0;
    }

    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, MatrixStack matStackIn, float x, float y, float scale, int width, int height) {
        MatrixStack poseStack = new MatrixStack();
        poseStack.pushPose();
        poseStack.translate(0, (float) gui.getSlidingProgression(), 0);

        Minecraft mc = Minecraft.getInstance();
        mc.textureManager.bind(this.getSkillTexture());

        gui.drawTexturedModalRectFixCoord(poseStack.last().pose(), (int)x, (int)y, 0, 0, 255, 255);

        if (!container.isActivated()) {
            String remainTime = String.format("%.0f", container.getMaxResource() - container.getResource());
            Minecraft.getInstance().font.draw(poseStack, remainTime, (x + 12 - 4 * (remainTime.length())), y + 6, 16777215);
        }

        poseStack.popPose();
    }
}