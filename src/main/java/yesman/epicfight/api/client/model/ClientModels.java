package yesman.epicfight.api.client.model;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.common.collect.Lists;

import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import yesman.epicfight.gameasset.Models;
import yesman.epicfight.main.EpicFightMod;

public class ClientModels extends Models<ClientModel> implements IFutureReloadListener {
	public static final ClientModels LOGICAL_CLIENT = new ClientModels();
	
	/** Entities **/
	public final ClientModel playerFirstPerson;
	public final ClientModel playerFirstPersonAlex;
	/** Armors **/
	public final ClientModel helmet;
	public final ClientModel chestplate;
	public final ClientModel leggins;
	public final ClientModel boots;
	/** Particles **/
	public final ClientModel forceField;
	//public final ClientModel airBurst;
	public final ClientModel laser;
	
	public ClientModels() {
		this.biped = register(new ResourceLocation(EpicFightMod.MODID, "entity/biped"));
		this.bipedOldTexture = register(new ResourceLocation(EpicFightMod.MODID, "entity/biped_old_texture"));
		this.bipedAlex = register(new ResourceLocation(EpicFightMod.MODID, "entity/biped_slim_arm"));
		this.playerFirstPerson = register(new ResourceLocation(EpicFightMod.MODID, "entity/biped_firstperson"));
		this.playerFirstPersonAlex = register(new ResourceLocation(EpicFightMod.MODID, "entity/biped_firstperson_slim"));
		this.helmet = register(new ResourceLocation(EpicFightMod.MODID, "armor/helmet_default"));
		this.chestplate = register(new ResourceLocation(EpicFightMod.MODID, "armor/chestplate_default"));
		this.leggins = register(new ResourceLocation(EpicFightMod.MODID, "armor/leggings_default"));
		this.boots = register(new ResourceLocation(EpicFightMod.MODID, "armor/boots_default"));

		//this.airBurst = register(new ResourceLocation(EpicFightMod.MODID, "particle/air_burst"));
		this.forceField = register(new ResourceLocation(EpicFightMod.MODID, "particle/force_field"));
		this.laser = register(new ResourceLocation(EpicFightMod.MODID, "particle/laser"));
	}
	
	@Override
	public ClientModel register(ResourceLocation rl) {
		ClientModel model = new ClientModel(rl);
		this.register(rl, model);
		return model;
	}
	
	public void register(ResourceLocation rl, ClientModel model) {
		this.models.put(rl, model);
	}
	
	public void loadModels(IResourceManager resourceManager) {
		List<ResourceLocation> emptyResourceLocations = Lists.newArrayList();
		
		this.models.forEach((key, value) -> {
            if (!value.loadMeshAndProperties(resourceManager)) {
                emptyResourceLocations.add(key);
            }
        });
		
		emptyResourceLocations.forEach(this.models::remove);
	}
	
	@Override
	public Models<?> getModels(boolean isLogicalClient) {
		return isLogicalClient ? LOGICAL_CLIENT : LOGICAL_SERVER;
	}
	
	@Override
	public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
		return CompletableFuture.runAsync(() -> {
			this.loadModels(resourceManager);
			this.loadArmatures(resourceManager);
		}, gameExecutor).thenCompose(stage::wait);
	}
}