package yesman.epicfight.data.conditions;

import java.util.List;
import java.util.function.Function;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class Condition<T> extends ForgeRegistryEntry<Condition<?>> {

	public Condition<T> read(CompoundNBT tag) {
        return null;
    }

	public CompoundNBT serializePredicate() {
		return null;
	}

	public boolean predicate(T target) {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		return null;
	}

	public static abstract class EntityPatchCondition extends Condition<LivingEntityPatch<?>> {
	}

	public static abstract class EntityCondition extends Condition<Entity> {
	}

	public static abstract class MobPatchCondition extends Condition<MobPatch<?>> {
	}

	public static abstract class ItemStackCondition extends Condition<ItemStack> {
	}

	@OnlyIn(Dist.CLIENT)
	public static class ParameterEditor {
		public static ParameterEditor of(Function<Object, INBT> toTag, Function<INBT, Object> fromTag, Widget editWidget) {
			return new ParameterEditor(toTag, fromTag, editWidget);
		}

		public final Function<Object, INBT> toTag;
		public final Function<INBT, Object> fromTag;
		public final Widget editWidget;

		private ParameterEditor(Function<Object, INBT> toTag, Function<INBT, Object> fromTag, Widget editWidget) {
			this.toTag = toTag;
			this.fromTag = fromTag;
			this.editWidget = editWidget;
		}
	}
}