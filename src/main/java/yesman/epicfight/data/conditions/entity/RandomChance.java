package yesman.epicfight.data.conditions.entity;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition.MobPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class RandomChance extends MobPatchCondition {
    private float chance;

    public RandomChance() {
        this.chance = 0.0F;
    }

    public RandomChance(float chance) {
        this.chance = chance;
    }

    @Override
    public RandomChance read(CompoundNBT tag) {
        this.chance = tag.getFloat("chance");

        if (!tag.contains("chance")) {
            throw new IllegalArgumentException("Random condition error: chancec not specified!");
        }

        return this;
    }

    @Override
    public CompoundNBT serializePredicate() {
        CompoundNBT tag = new CompoundNBT();
        tag.putFloat("chance", this.chance);

        return tag;
    }

    @Override
    public boolean predicate(MobPatch<?> target) {
        return target.getOriginal().getRandom().nextFloat() < this.chance;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
//        ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("chance"), null, null);
//        editbox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
//
//        return List.of(ParameterEditor.of((value) -> FloatTag.valueOf(Float.valueOf(value.toString())), (tag) -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, Tag::getAsString)), editbox));
    return null;
    }
}