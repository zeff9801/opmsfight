package yesman.epicfight.data.conditions.entity;

import java.util.List;

import io.netty.util.internal.StringUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class OffhandItemCategory extends EntityPatchCondition {
    private WeaponCategory category;

    @Override
    public OffhandItemCategory read(CompoundNBT tag) {
        if (!tag.contains("category") || StringUtil.isNullOrEmpty(tag.getString("category"))) {
            throw new IllegalArgumentException("Undefined weapon category");
        }

        this.category = WeaponCategory.ENUM_MANAGER.getOrThrow(tag.getString("category"));

        return this;
    }

    @Override
    public CompoundNBT serializePredicate() {
        CompoundNBT tag = new CompoundNBT();

        tag.putString("category", this.category.toString());

        return tag;
    }

    @Override
    public boolean predicate(LivingEntityPatch<?> target) {
        return target.getHoldingItemCapability(Hand.OFF_HAND).getWeaponCategory() == this.category;
    }

    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
//        AbstractWidget comboBox = new ComboBox<>(screen, screen.getMinecraft().font, 0, 0, 0, 0, null, null, 4, Component.literal("category"), List.copyOf(WeaponCategory.ENUM_MANAGER.universalValues()), ParseUtil::snakeToSpacedCamel, null);
//
//        return List.of(ParameterEditor.of((value) -> StringTag.valueOf(value.toString().toLowerCase(Locale.ROOT)), (tag) -> WeaponCategory.ENUM_MANAGER.get(ParseUtil.nullOrToString(tag, Tag::getAsString)), comboBox));

        return null;
    }
}