package yesman.epicfight.api.utils;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public class RegistryUtils {
    public static <T extends IForgeRegistryEntry<T>> void createRegistrry(ResourceLocation registryId, Class<T> type) {
        IForgeRegistry newRegistry =  new RegistryBuilder().setName(registryId).setType(type).setMaxID(Byte.MAX_VALUE).create();
    }

    public static <T extends IForgeRegistryEntry<T>> void createRegistrry(ResourceLocation registryId, Class<T> type, Object registryCallback) {
        IForgeRegistry newRegistry =  new RegistryBuilder().setName(registryId).setType(type).setMaxID(Byte.MAX_VALUE).addCallback(registryCallback).create();
    }

}
