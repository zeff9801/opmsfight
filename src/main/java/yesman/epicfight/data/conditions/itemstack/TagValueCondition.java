package yesman.epicfight.data.conditions.itemstack;

import com.google.common.collect.Lists;
import io.netty.util.internal.StringUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import yesman.epicfight.data.conditions.Condition.ItemStackCondition;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagValueCondition extends ItemStackCondition {
	private String key;
	private String value;
	
	@Override
	public TagValueCondition read(CompoundNBT tag) {
		this.key = tag.getString("key");
		this.value = tag.get("value").getAsString();
		
		if (this.key == null) {
			throw new IllegalArgumentException("No key provided!");
		}
		
		if (this.value == null) {
			throw new IllegalArgumentException("No value provided!");
		}
		
		return this;
	}
	
	@Override
	public CompoundNBT serializePredicate() {
		CompoundNBT tag = new CompoundNBT();
		
		tag.putString("key", this.key);
		tag.putString("value", this.value);
		
		return tag;
	}
	
	@Override
	public boolean predicate(ItemStack itemstack) {
		String[] keys = this.key.split("[.]");
		List<INBT> visitTags = List.of(itemstack.getTag());
		
		for (int i = 0; i < keys.length; i++) {
			visitTags = visitTags(keys[i], visitTags);
		}
		
		for (INBT tag : visitTags) {
			if (tag.getAsString().equals(this.value)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		return List.of();
	}

	//@Override
	//@OnlyIn(Dist.CLIENT)
	//public List<ParameterEditor> getAcceptingParameters(Screen screen) {
	//	ResizableEditBox keyEditBox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, .literal("key"), null, null);
	//	ResizableEditBox valueEditBox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("value"), null, null);
	//	Function<Object, INBT> stringParser = (value) -> StringTag.valueOf(value.toString());
	//	Function<INBT, Object> stringGetter = (tag) -> ParseUtil.nullOrToString(tag, INBT::getAsString);
		
	//	return List.of(ParameterEditor.of(stringParser, stringGetter, keyEditBox), ParameterEditor.of(stringParser, stringGetter, valueEditBox));
	//}
	
	private static List<INBT> visitTags(String key, List<INBT> CompoundNBT) {
		Pattern pattern = Pattern.compile("\\[[0-9]*\\]", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(key);
		List<INBT> childs = Lists.newArrayList();
		
		if (matcher.find()) {
			String sIndex = matcher.group().replaceAll("[\\[\\]]", "");
			String arrayKey = matcher.replaceAll("");
			
			if (StringUtil.isNullOrEmpty(sIndex)) {
				for (INBT tag : CompoundNBT) {
					if (tag instanceof CompoundNBT compTag && compTag.contains(arrayKey)) {
						ListNBT listTag = (ListNBT)compTag.get(arrayKey);
						
						for (INBT listTagElement : listTag) {
							childs.add(listTagElement);
						}
					}
				}
			} else {
				int index = Integer.valueOf(sIndex);
				
				for (INBT tag : CompoundNBT) {
					if (tag instanceof CompoundNBT compTag && compTag.contains(arrayKey)) {
						ListNBT listTag = (ListNBT)compTag.get(arrayKey);
						childs.add(listTag.get(index));
					}
				}
			}
		} else {
			for (INBT tag : CompoundNBT) {
				if (tag instanceof CompoundNBT compTag && compTag.contains(key)) {
					childs.add(compTag.get(key));
				}
			}
		}
		
		return childs;
	}
}