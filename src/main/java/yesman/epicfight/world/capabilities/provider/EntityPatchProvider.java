package yesman.epicfight.world.capabilities.provider;

import com.google.common.collect.Maps;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.forgeevent.EntityPatchRegistryEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityPatchProvider implements ICapabilityProvider, NonNullSupplier<EntityPatch<?>> {
	private static final Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> CAPABILITIES = Maps.newHashMap();
	private static final Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> CUSTOM_CAPABILITIES = Maps.newHashMap();
	
	public static void registerEntityPatches() {
		Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> registry = Maps.newHashMap();
		registry.put(EntityType.PLAYER, (entityIn) -> ServerPlayerPatch::new);

		EntityPatchRegistryEvent entitypatchRegistryEvent = new EntityPatchRegistryEvent(registry);
		ModLoader.get().postEvent(entitypatchRegistryEvent);

        CAPABILITIES.putAll(registry);
	}
	
	public static void registerEntityPatchesClient() {
		CAPABILITIES.put(EntityType.PLAYER, (entityIn) -> {
			if (entityIn instanceof ClientPlayerEntity) {
				return LocalPlayerPatch::new;
			} else if (entityIn instanceof RemoteClientPlayerEntity) {
				return AbstractClientPlayerPatch<RemoteClientPlayerEntity>::new;
			} else if (entityIn instanceof ServerPlayerEntity) {
				return ServerPlayerPatch::new;
			} else {
				return () -> null;
			}
		});
	}
	
	public static void clear() {
		CUSTOM_CAPABILITIES.clear();
	}
	
	public static void putCustomEntityPatch(EntityType<?> entityType, Function<Entity, Supplier<EntityPatch<?>>> entitypatchProvider) {
		CUSTOM_CAPABILITIES.put(entityType, entitypatchProvider);
	}
	
	public static Function<Entity, Supplier<EntityPatch<?>>> get(String registryName) {
		ResourceLocation rl = new ResourceLocation(registryName);
		EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(rl);
		return CAPABILITIES.get(entityType);
	}
	
	private EntityPatch<?> capability;
	private LazyOptional<EntityPatch<?>> optional = LazyOptional.of(this);
	
	public EntityPatchProvider(Entity entity) {
		Function<Entity, Supplier<EntityPatch<?>>> provider = CUSTOM_CAPABILITIES.getOrDefault(entity.getType(), CAPABILITIES.get(entity.getType()));
		
		if (provider != null) {
			this.capability = provider.apply(entity).get();
		}
	}
	
	public boolean hasCapability() {
		return capability != null;
	}
	
	@Override
	public EntityPatch<?> get() {
		return this.capability;
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == EpicFightCapabilities.CAPABILITY_ENTITY ? this.optional.cast() :  LazyOptional.empty();
	}
}