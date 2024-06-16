package yesman.epicfight.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.AreaEffectCloudRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.particle.*;
import yesman.epicfight.client.renderer.entity.DroppedNetherStarRenderer;
import yesman.epicfight.client.renderer.entity.WitherGhostRenderer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.entity.EpicFightEntities;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid=EpicFightMod.MODID, value=Dist.CLIENT, bus=EventBusSubscriber.Bus.MOD)
public class ClientModBusEvent {
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onParticleRegistry(final ParticleFactoryRegisterEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ParticleManager particleEngine = mc.particleEngine;
    	particleEngine.register(EpicFightParticles.ENDERMAN_DEATH_EMIT.get(), EnderParticle.EndermanDeathEmitProvider::new);
    	particleEngine.register(EpicFightParticles.HIT_BLUNT.get(), HitBluntParticle.Provider::new);
    	particleEngine.register(EpicFightParticles.HIT_BLADE.get(), new HitCutParticle.Provider());
    	particleEngine.register(EpicFightParticles.CUT.get(), CutParticle.Provider::new);
    	particleEngine.register(EpicFightParticles.NORMAL_DUST.get(), DustParticle.NormalDustProvider::new);
    	particleEngine.register(EpicFightParticles.DUST_EXPANSIVE.get(), DustParticle.ExpansiveDustProvider::new);
    	particleEngine.register(EpicFightParticles.DUST_CONTRACTIVE.get(), DustParticle.ContractiveDustProvider::new);
    	particleEngine.register(EpicFightParticles.EVISCERATE.get(), new EviscerateParticle.Provider());
    	particleEngine.register(EpicFightParticles.BLOOD.get(), BloodParticle.Provider::new);
    	particleEngine.register(EpicFightParticles.BLADE_RUSH_SKILL.get(), BladeRushParticle.Provider::new);
    	particleEngine.register(EpicFightParticles.GROUND_SLAM.get(), new GroundSlamParticle.Provider());
    	particleEngine.register(EpicFightParticles.BREATH_FLAME.get(), EnderParticle.BreathFlameProvider::new);
    	particleEngine.register(EpicFightParticles.FORCE_FIELD.get(), new ForceFieldParticle.Provider());
    	particleEngine.register(EpicFightParticles.FORCE_FIELD_END.get(), new ForceFieldEndParticle.Provider());
    	particleEngine.register(EpicFightParticles.ENTITY_AFTER_IMAGE.get(), new EntityAfterImageParticle.Provider());
    	particleEngine.register(EpicFightParticles.LASER.get(), new LaserParticle.Provider());
    	particleEngine.register(EpicFightParticles.NEUTRALIZE.get(), new DustParticle.ExpansiveMetaParticle.Provider());
    	particleEngine.register(EpicFightParticles.BOSS_CASTING.get(), new DustParticle.ContractiveMetaParticle.Provider());

		particleEngine.register(EpicFightParticles.TSUNAMI_SPLASH.get(), TsunamiSplashParticle.Provider::new);
		particleEngine.register(EpicFightParticles.SWING_TRAIL.get(), TrailParticle.Provider::new);
		particleEngine.register(EpicFightParticles.FEATHER.get(), FeatherParticle.Provider::new);
		particleEngine.register(EpicFightParticles.AIR_BURST.get(), new AirBurstParticle.Provider());

    	EntityRendererManager entityRenderManager = mc.getEntityRenderDispatcher();
    	ItemRenderer itemRenderer = mc.getItemRenderer();
    	
    	entityRenderManager.register(EpicFightEntities.AREA_EFFECT_BREATH.get(), new AreaEffectCloudRenderer(entityRenderManager));
    	entityRenderManager.register(EpicFightEntities.DROPPED_NETHER_STAR.get(), new DroppedNetherStarRenderer(entityRenderManager, itemRenderer));
    	entityRenderManager.register(EpicFightEntities.WITHER_SKELETON_MINION.get(), new WitherSkeletonRenderer(entityRenderManager));
    	entityRenderManager.register(EpicFightEntities.WITHER_GHOST_CLONE.get(), new WitherGhostRenderer(entityRenderManager));
    }
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onParticleRegistry(final ModelBakeEvent event) {
		ClientEngine.instance.renderEngine.registerRenderer();
		WearableItemLayer.clear();
	}
}