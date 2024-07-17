package yesman.epicfight.data.conditions.entity;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition.MobPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class HealthPoint extends MobPatchCondition {
    private float health;
    private Comparator comparator;

    public HealthPoint() {
        this.health = 0.0F;
    }

    public HealthPoint(float health, Comparator comparator) {
        this.health = health;
        this.comparator = comparator;
    }

    @Override
    public HealthPoint read(CompoundNBT tag) {
        if (!tag.contains("comparator")) {
            throw new IllegalArgumentException("HealthPoint condition error: comparator not specified!");
        }

        if (!tag.contains("health")) {
            throw new IllegalArgumentException("HealthPoint condition error: health not specified!");
        }

        String sComparator = tag.getString("comparator").toUpperCase(Locale.ROOT);

        try {
            this.comparator = Comparator.valueOf(sComparator);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("HealthPoint condition error: invalid comparator " + sComparator);
        }

        this.health = tag.getFloat("health");

        return this;
    }

    @Override
    public CompoundNBT serializePredicate() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("comparator", this.comparator.toString().toLowerCase(Locale.ROOT));
        tag.putFloat("health", this.health);

        return tag;
    }

    @Override
    public boolean predicate(MobPatch<?> target) {
        switch (this.comparator) {
            case LESS_ABSOLUTE:
                return this.health > target.getOriginal().getHealth();
            case GREATER_ABSOLUTE:
                return this.health < target.getOriginal().getHealth();
            case LESS_RATIO:
                return this.health > target.getOriginal().getHealth() / target.getOriginal().getMaxHealth();
            case GREATER_RATIO:
                return this.health < target.getOriginal().getHealth() / target.getOriginal().getMaxHealth();
        }

        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
//        ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("health"), null, null);
//        AbstractWidget comboBox = new ComboBox<>(screen, screen.getMinecraft().font, 0, 0, 0, 0, null, null, 4, Component.literal("comparator"), List.of(Comparator.values()), ParseUtil::snakeToSpacedCamel, null);
//
//        editbox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));
//
//        return List.of(
//                ParameterEditor.of((value) -> FloatTag.valueOf(Float.parseFloat(value.toString())), (tag) -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, Tag::getAsString)), editbox),
//                ParameterEditor.of((value) -> StringTag.valueOf(value.toString().toLowerCase(Locale.ROOT)), (tag) -> ParseUtil.enumValueOfOrNull(Comparator.class, ParseUtil.nullOrToString(tag, Tag::getAsString)), comboBox)
//        );
        return null;
    }

    public enum Comparator {
        GREATER_ABSOLUTE, LESS_ABSOLUTE, GREATER_RATIO, LESS_RATIO
    }
}