package yesman.epicfight.data.conditions.entity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.Condition.EntityCondition;

public class HasCustomTag extends EntityCondition {
    private final Set<String> allowedTags;

    public HasCustomTag(ListNBT allowedTags) {
        this.allowedTags = allowedTags.stream().map(INBT::getAsString).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Condition<Entity> read(CompoundNBT tag) {
        return null;
    }

    @Override
    public CompoundNBT serializePredicate() {
        return null;
    }

    @Override
    public boolean predicate(Entity target) {
        for (String tag : this.allowedTags) {
            if (target.getTags().contains(tag)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
//        ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("tag"), null, null);
//        return List.of(ParameterEditor.of((value) -> StringTag.valueOf(value.toString()), (tag) -> ParseUtil.nullOrToString(tag, Tag::getAsString), (AbstractWidget)editbox));
        return null;
    }
}