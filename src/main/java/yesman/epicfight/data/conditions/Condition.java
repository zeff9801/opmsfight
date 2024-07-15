package yesman.epicfight.data.conditions;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jline.reader.Widget;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

import javax.swing.text.html.parser.Entity;
import java.util.List;
import java.util.function.Function;

public interface Condition<T> {
	public Condition<T> read(CompoundNBT tag);
	public CompoundNBT serializePredicate();
	public boolean predicate(T target);
	
	@OnlyIn(Dist.CLIENT)
	public List<ParameterEditor> getAcceptingParameters(Screen screen);
	
	public static abstract class EntityPatchCondition implements Condition<LivingEntityPatch<?>> {
	}
	
	public static abstract class EntityCondition implements Condition<Entity> {
	}
	
	public static abstract class MobPatchCondition implements Condition<MobPatch<?>> {
	}
	
	public static abstract class ItemStackCondition implements Condition<ItemStack> {
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