
package yesman.epicfight.data.conditions.entity;

import java.util.List;

import io.netty.util.internal.StringUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SkillActivated extends EntityPatchCondition {
    private Skill skill;

    @Override
    public SkillActivated read(CompoundNBT tag) {
        if (!tag.contains("skill") || StringUtil.isNullOrEmpty(tag.getString("skill"))) {
            throw new IllegalArgumentException("Undefined skill");
        }

//        this.skill = SkillManager.getSkill(tag.getString("skill")); //TODO Uncoomment once #getSkill exists in SkillManager
        this.skill = null;

        return this;
    }

    @Override
    public CompoundNBT serializePredicate() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("skill", this.skill.getSkillRegistryName().toString());

        return tag;
    }

    @Override
    public boolean predicate(LivingEntityPatch<?> target) {
        if (target instanceof PlayerPatch<?> playerpatch) {
            SkillContainer skill = playerpatch.getSkill(this.skill);

            if (skill == null) {
                return false;
            } else {
                return skill.isActivated();
            }
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
//        AbstractWidget popupBox = new PopupBox.RegistryPopupBox<>(screen, screen.getMinecraft().font, 0, 0, 0, 0, null, null, Component.literal("skill"), SkillManager.getSkillRegistry(), null);
//
//        return List.of(ParameterEditor.of((skill) -> StringTag.valueOf(skill.toString()), (tag) -> SkillManager.getSkill(ParseUtil.nullOrToString(tag, Tag::getAsString)), popupBox));
        return null;
    }
}
