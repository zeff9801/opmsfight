package yesman.epicfight.main;

import java.nio.file.Path;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.model.ItemSkins;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.gui.screen.config.IngameConfigurationScreen;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.compat.AzureLibArmorCompat;
import yesman.epicfight.compat.AzureLibCompat;
import yesman.epicfight.compat.GeckolibCompat;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.config.ConfigManager;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EpicFightDataSerializers;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.server.commands.PlayerModeCommand;
import yesman.epicfight.server.commands.PlayerSkillCommand;
import yesman.epicfight.server.commands.arguments.EpicFightCommandArgumentTypes;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.effect.EpicFightPotions;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.decoration.EpicFightPaintingVariants;
import yesman.epicfight.world.gamerule.EpicFightGamerules;
import yesman.epicfight.world.item.EpicFightCreativeTabs;
import yesman.epicfight.world.item.EpicFightItems;
import yesman.epicfight.world.item.SkillBookItem;
import yesman.epicfight.world.level.block.EpicFightBlocks;
import yesman.epicfight.world.level.block.entity.EpicFightBlockEntities;

/**
 *  Changes from 20.7.4 -> 20.8.1
 *  
 *  1. Datapack Edit Screen added
 *  
 *  2. Skillbook screen revamped
 *  
 *  3. Skill Consume event changed
 *  
 *  4. Armor Negation calculation changed
 *  
 *  5. Weight base value calculation changed to (entity dimension width * height * {@link LivingEntityPatch#WEIGHT_CORRECTION})
 *  
 *  6. Animations now won't be interpolated from between the previous and current pose but from animation clip resulting in increased animation accuracy
 *  
 *  7. Skill registration changed
 *  
 *  8. Added {@link EpicFightExtensions}. Now you can decide a creative tab that you want to display your skills of your mod. (see the usage below)
 *  
 *  9. Fixed crash when item broken in player's offhand by attacking any entities
 *  
 *  10. Added a selectable built-in resource pack with legacy weapon textures
 *  
 *  11. Fixed Freecam mod disables Epic Fight battle mode
 *  --- 20.8.1.1
 *  12. Fixed Ender dragon breath hits a player in creative mode
 *  
 *  13. Fixed Player head go through the body when using demolition leap
 *  
 *  14. Fixed Demolition leap unable to jump forward when you cast it with a wall behind
 *  
 *  15. Fixed the skills are not removed even tho keepSkills gamerule is set to false
 *  
 *  16. Added translations for sound subtitles.
 *  
 *  TO DO
 *  
 *  1. Trail texture bug
 *  
 *  2. Crash because {@link PlayerPatch#STAMINA} is unregistered at SynchedEntityData (Most likely a mod compatibility issue)
 *  
 *  3. Add reach property to attack animation. (idea)
 *  
 *  @author yesman
 */
@Mod("epicfight")
public class EpicFightMod {
	public static final String MODID = "epicfight";
	public static final String CONFIG_FILE_PATH = EpicFightMod.MODID + ".toml";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static EpicFightOptions CLIENT_CONFIGS;
	private static EpicFightMod instance;
	
	public static EpicFightMod getInstance() {
		return instance;
	}
	
	private Function<LivingEntityPatch<?>, Animator> animatorProvider;
	
    public EpicFightMod() {
    	instance = this;
    	
    	ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigManager.CLIENT_CONFIG);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		
		bus.addListener(this::constructMod);
    	bus.addListener(this::doClientStuff);
    	bus.addListener(this::doCommonStuff);
    	bus.addListener(this::doServerStuff);
    	bus.addListener(this::addPackFindersEvent);
    	bus.addListener(this::registerResourcepackReloadListnerEvent);
    	bus.addListener(this::buildCreativeTabWithSkillBooks);
    	bus.addListener(EpicFightAttributes::registerNewMobs);
    	bus.addListener(EpicFightAttributes::modifyExistingMobs);
    	bus.addListener(SkillManager::createSkillRegistry);
    	bus.addListener(SkillManager::registerSkills);
    	bus.addListener(EpicFightCapabilities::registerCapabilities);
    	bus.addListener(EpicFightEntities::onSpawnPlacementRegister);
    	
    	MinecraftForge.EVENT_BUS.addListener(this::command);
        MinecraftForge.EVENT_BUS.addListener(this::registerDatapackReloadListnerEvent);
    	
    	LivingMotion.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, LivingMotions.class);
    	SkillCategory.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, SkillCategories.class);
    	SkillSlot.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, SkillSlots.class);
    	Style.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, Styles.class);
    	WeaponCategory.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, WeaponCategories.class);
    	
    	EpicFightMobEffects.EFFECTS.register(bus);
    	EpicFightPotions.POTIONS.register(bus);
        EpicFightAttributes.ATTRIBUTES.register(bus);
        EpicFightCreativeTabs.TABS.register(bus);
        EpicFightItems.ITEMS.register(bus);
        EpicFightParticles.PARTICLES.register(bus);
        EpicFightEntities.ENTITIES.register(bus);
        EpicFightBlocks.BLOCKS.register(bus);
        EpicFightBlockEntities.BLOCK_ENTITIES.register(bus);
		EpicFightLootTables.LOOT_MODIFIERS.register(bus);
		EpicFightSounds.SOUNDS.register(bus);
		EpicFightDataSerializers.ENTITY_DATA_SERIALIZER.register(bus);
		EpicFightConditions.CONDITIONS.register(bus);
		SkillDataKeys.DATA_KEYS.register(bus);
		EpicFightPaintingVariants.PAINTING_VARIANTS.register(bus);
		EpicFightCommandArgumentTypes.COMMAND_ARGUMENT_TYPES.register(bus);
        
        ConfigManager.loadConfig(ConfigManager.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve(MODID + "-client.toml").toString());
        ConfigManager.loadConfig(ConfigManager.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_PATH).toString());
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(IngameConfigurationScreen::new));
        
        //
        ModLoadingContext.get().registerExtensionPoint(EpicFightExtensions.class, () -> new EpicFightExtensions(EpicFightCreativeTabs.ITEMS.get()));
        
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
        	if (ModList.get().isLoaded("geckolib")) {
    			ICompatModule.loadCompatModule(GeckolibCompat.class);
    		}
    		
    		if (ModList.get().isLoaded("azurelib")) {
    			ICompatModule.loadCompatModule(AzureLibCompat.class);
    		}
    		
    		if (ModList.get().isLoaded("azurelibarmor")) {
    			ICompatModule.loadCompatModule(AzureLibArmorCompat.class);
    		}
		});
	}
    
    /**
     * FML Lifecycle Events
     */
    private void constructMod(final FMLConstructModEvent event) {
    	LivingMotion.ENUM_MANAGER.loadEnum();
    	SkillCategory.ENUM_MANAGER.loadEnum();
    	SkillSlot.ENUM_MANAGER.loadEnum();
    	Style.ENUM_MANAGER.loadEnum();
    	WeaponCategory.ENUM_MANAGER.loadEnum();
    }
    
	private void doClientStuff(final FMLClientSetupEvent event) {
		CLIENT_CONFIGS = new EpicFightOptions();
    	new ClientEngine();
    	
        this.animatorProvider = ClientAnimator::getAnimator;
		EntityPatchProvider.registerEntityPatchesClient();
		EpicFightItemProperties.registerItemProperties();
		SkillBookScreen.registerIconItems();
    }
	
	private void doServerStuff(final FMLDedicatedServerSetupEvent event) {
		this.animatorProvider = ServerAnimator::getAnimator;
	}
	
	private void doCommonStuff(final FMLCommonSetupEvent event) {
		event.enqueueWork(EpicFightCommandArgumentTypes::registerArgumentTypes);
		event.enqueueWork(EpicFightPotions::addRecipes);
		event.enqueueWork(EpicFightNetworkManager::registerPackets);
		event.enqueueWork(ItemCapabilityProvider::registerWeaponTypesByClass);
		event.enqueueWork(EntityPatchProvider::registerEntityPatches);
		event.enqueueWork(EpicFightGamerules::registerRules);
		event.enqueueWork(WeaponTypeReloadListener::registerDefaultWeaponTypes);
		event.enqueueWork(EpicFightMobEffects::addOffhandModifier);
    }
	
	/**
	 * Register Etc
	 */
	private void command(final RegisterCommandsEvent event) {
		PlayerModeCommand.register(event.getDispatcher());
		PlayerSkillCommand.register(event.getDispatcher(), event.getBuildContext());
    }
	
	public void addPackFindersEvent(AddPackFindersEvent event) {
		if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            Path resourcePath = ModList.get().getModFileById(EpicFightMod.MODID).getFile().findResource("packs/epicfight_legacy");
            PathPackResources pack = new PathPackResources(ModList.get().getModFileById(EpicFightMod.MODID).getFile().getFileName() + ":" + resourcePath, resourcePath, false);
            Pack.ResourcesSupplier resourcesSupplier = (string) -> pack;
            Pack.Info info = Pack.readPackInfo("epicfight_legacy", resourcesSupplier);
            
            if (info != null) {
                event.addRepositorySource((source) ->
    			source.accept(Pack.create("epicfight_legacy", Component.translatable("pack.epicfight_legacy.title"), false, resourcesSupplier, info, PackType.CLIENT_RESOURCES, Pack.Position.TOP, false, PackSource.BUILT_IN)));
            }
        }
    }
	
	private void registerResourcepackReloadListnerEvent(final RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(new JointMaskReloadListener());
		event.registerReloadListener(Meshes.INSTANCE);
		event.registerReloadListener(AnimationManager.getInstance());
		event.registerReloadListener(ItemSkins.INSTANCE);
	}
	
	private void registerDatapackReloadListnerEvent(final AddReloadListenerEvent event) {
		if (!isPhysicalClient()) {
			event.addListener(AnimationManager.getInstance());
		}
		
		event.addListener(new ColliderPreset());
		event.addListener(new SkillManager());
		event.addListener(new WeaponTypeReloadListener());
		event.addListener(new ItemCapabilityReloadListener());
		event.addListener(new MobPatchReloadListener());
	}
	
	private void buildCreativeTabWithSkillBooks(final BuildCreativeModeTabContentsEvent event) {
		/**
		 * Accept learnable skills for each mod by {@link EpicFightExtensions#skillBookCreativeTab}.
		 * If the extension doesn't exist, add them to {@link EpicFightCreativeTabs.ITEMS} tab.
		 */
		SkillManager.getNamespaces().forEach((modid) -> {
			ModList.get().getModContainerById(modid).flatMap((mc) -> mc.getCustomExtension(EpicFightExtensions.class)).ifPresentOrElse((extension) -> {
				if (extension.skillBookCreativeTab() == event.getTab()) {
					SkillManager.getSkillNames((skill) -> skill.getCategory().learnable() && skill.getRegistryName().getNamespace() == modid).forEach((rl) -> {
						ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
						SkillBookItem.setContainingSkill(rl.toString(), stack);
						event.accept(stack);
					});
				}
			}, () -> {
				if (event.getTab() == EpicFightCreativeTabs.ITEMS.get()) {
					SkillManager.getSkillNames((skill) -> skill.getCategory().learnable() && skill.getRegistryName().getNamespace() == modid).forEach((rl) -> {
						ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
						SkillBookItem.setContainingSkill(rl.toString(), stack);
						event.accept(stack);
					});
				}
			});
		});
	}
	
	/**
	 * Epic Fight utils
	 */
	public static Animator getAnimator(LivingEntityPatch<?> entitypatch) {
		return EpicFightMod.getInstance().animatorProvider.apply(entitypatch);
	}
	
	public static boolean isPhysicalClient() {
    	return FMLEnvironment.dist == Dist.CLIENT;
    }
}