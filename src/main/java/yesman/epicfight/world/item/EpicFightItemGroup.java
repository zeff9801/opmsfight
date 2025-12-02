package yesman.epicfight.world.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightItems;

public class EpicFightItemGroup {
	public static final ItemGroup ITEMS = new ItemGroup(EpicFightMod.MODID + ".items") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(EpicFightItems.SKILLBOOK.get());
        }
    };
}
