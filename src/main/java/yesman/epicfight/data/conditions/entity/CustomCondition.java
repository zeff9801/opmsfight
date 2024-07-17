package yesman.epicfight.data.conditions.entity;

import java.util.List;
import java.util.function.Function;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CustomCondition<T extends LivingEntityPatch<?>> extends Condition<T> {
    private final Function<T, Boolean> predicate;

    public CustomCondition(Function<T, Boolean> predicate) {
        this.predicate = predicate;
    }

    @Override
    public CustomCondition<T> read(CompoundNBT tag) {
        return null;
    }

    @Override
    public CompoundNBT serializePredicate() {
        return null;
    }

    @Override
    public boolean predicate(T target) {
        return predicate.apply(target);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }
}