package yesman.epicfight.skill.dodge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.mixin_interfaces.IExtendedMovementInput;
import yesman.epicfight.network.client.CPExecuteSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class KnockdownWakeupSkill extends DodgeSkill {
    public KnockdownWakeupSkill(Builder builder) {
        super(builder);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Object getExecutionPacket(LocalPlayerPatch executer, PacketBuffer args) {
        MovementInput input = executer.getOriginal().input;
//        float pulse = MathHelper.clamp(0.3F + EnchantmentHelper.getSneakingSpeedBonus(executer.getOriginal()), 0.0F, 1.0F);
        float pulse = MathHelper.clamp(0.3F + 0.0f, 0.0F, 1.0F);
        ((IExtendedMovementInput)input).tick(false, pulse);

        int left = input.left ? 1 : 0;
        int right = input.right ? -1 : 0;
        int horizon = left + right;
        float yRot = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();

        CPExecuteSkill packet = new CPExecuteSkill(executer.getSkill(this).getSlotId());
        packet.getBuffer().writeInt(horizon >= 0 ? 0 : 1);
        packet.getBuffer().writeFloat(yRot);

        return packet;
    }

    @Override
    public boolean isExecutableState(PlayerPatch<?> executer) {
        EntityState playerState = executer.getEntityState();
        float elapsedTime = executer.getAnimator().getPlayerFor(null).getElapsedTime();
        return !(executer.footsOnGround() || (playerState.hurt() && !playerState.knockDown())) && !executer.getOriginal().isInWater() && !executer.getOriginal().onClimbable() && elapsedTime > 0.7F;
    }
}