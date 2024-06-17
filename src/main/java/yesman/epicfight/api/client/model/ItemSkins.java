package yesman.epicfight.api.client.model;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.Item;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public class ItemSkins extends JsonReloadListener {
	public static final ItemSkins INSTANCE = new ItemSkins();
	private static final Map<Item, ItemSkin> ITEM_SKIN_MAP = Maps.newHashMap();
	
	public static ItemSkin getItemSkin(Item item) {
		return ITEM_SKIN_MAP.get(item);
	}
	
	public ItemSkins() {
		super((new GsonBuilder()).create(), "item_skins");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, IResourceManager resourceManager, IProfiler profileFiller) {
		for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()) {
			ResourceLocation rl = entry.getKey();
			String pathString = rl.getPath();
			ResourceLocation registryName = new ResourceLocation(rl.getNamespace(), pathString);
			
			if (!ForgeRegistries.ITEMS.containsKey(registryName)) {
                EpicFightMod.LOGGER.warn("[Item Skins] Item named {} does not exist", registryName);
				continue;
			}
			
			Item item = ForgeRegistries.ITEMS.getValue(registryName);
			ItemSkin itemSkin = ItemSkin.deserialize(entry.getValue());
			
			ITEM_SKIN_MAP.put(item, itemSkin);
		}
	}
}