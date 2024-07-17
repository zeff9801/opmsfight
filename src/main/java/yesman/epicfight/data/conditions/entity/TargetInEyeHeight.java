package yesman.epicfight.data.conditions.entity;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

import java.util.List;

public class TargetInEyeHeight extends Condition.MobPatchCondition {

    @Override
    public TargetInEyeHeight read(CompoundNBT tag) {
        return this;
    }

    @Override
    public CompoundNBT serializePredicate() {
        return new CompoundNBT();
    }

    @Override
    public boolean predicate(MobPatch<?> target) {
        double veticalDistance = Math.abs(target.getOriginal().getY() - target.getTarget().getY());
        return veticalDistance < target.getOriginal().getEyeHeight();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return List.of();
    }
}