package yesman.epicfight.main;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
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
import net.minecraftforge.registries.DataSerializerEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.model.ItemSkins;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.IngameConfigurationScreen;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.config.ConfigManager;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.data.loot.EpicFightLootModifiers;
import yesman.epicfight.events.*;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.network.EpicFightDataSerializers;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.server.commands.PlayerModeCommand;
import yesman.epicfight.server.commands.PlayerSkillCommand;
import yesman.epicfight.server.commands.arguments.EpicFightCommandArgumentTypes;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillCategory;
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
import yesman.epicfight.world.capabilities.provider.ProviderProjectile;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.effect.EpicFightPotions;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.gamerule.EpicFightGamerules;
import yesman.epicfight.world.item.EpicFightItems;

import java.util.function.Function;

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
		bus.addListener(this::registerResourcepackReloadListnerEvent);
		//bus.addListener(this::buildCreativeTabWithSkillBooks);
		bus.addListener(EpicFightAttributes::registerNewMobs);
		bus.addListener(EpicFightAttributes::modifyExistingMobs);
		//bus.addListener(SkillManager::createSkillRegistry);
		//bus.addListener(SkillManager::registerSkills);
		//bus.addListener(EpicFightEntities::onSpawnPlacementRegister);

		MinecraftForge.EVENT_BUS.addListener(this::command);
		MinecraftForge.EVENT_BUS.addListener(this::registerDatapackReloadListnerEvent);


		LivingMotion.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, LivingMotions.class);
		SkillCategory.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, SkillCategories.class);
		SkillSlot.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, SkillSlots.class);
		Style.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, Styles.class);
		WeaponCategory.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, WeaponCategories.class);

		bus.addGenericListener(DataSerializerEntry.class, EpicFightDataSerializers::register);
		bus.addGenericListener(GlobalLootModifierSerializer.class, EpicFightLootModifiers::register);


		EpicFightMobEffects.EFFECTS.register(bus);
		EpicFightPotions.POTIONS.register(bus);
		EpicFightAttributes.ATTRIBUTES.register(bus);
		//EpicFightCreativeTabs.TABS.register(bus);
		EpicFightItems.ITEMS.register(bus);
		EpicFightParticles.PARTICLES.register(bus);
		EpicFightEntities.ENTITIES.register(bus);
		//EpicFightBlocks.BLOCKS.register(bus);
		//EpicFightBlockEntities.BLOCK_ENTITIES.register(bus);
		//EpicFightLootTables.LOOT_MODIFIERS.register(bus);
		//EpicFightSounds.SOUNDS.register(bus);
		//EpicFightDataSerializers.ENTITY_DATA_SERIALIZER.register(bus);
		EpicFightConditions.CONDITIONS.register(bus);
		//SkillDataKeys.DATA_KEYS.register(bus);
		//EpicFightPaintingVariants.PAINTING_VARIANTS.register(bus);
		//EpicFightCommandArgumentTypes.COMMAND_ARGUMENT_TYPES.register(bus);


		ConfigManager.loadConfig(ConfigManager.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve(MODID + "-client.toml").toString());
		ConfigManager.loadConfig(ConfigManager.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_PATH).toString());
		//ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(IngameConfigurationScreen::new));


		MinecraftForge.EVENT_BUS.register(EntityEvents.class);
		MinecraftForge.EVENT_BUS.register(ModBusEvents.class);
		MinecraftForge.EVENT_BUS.register(CapabilityEvent.class);
		MinecraftForge.EVENT_BUS.register(PlayerEvents.class);

		ConfigManager.loadConfig(ConfigManager.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve(MODID + "-client.toml").toString());
		ConfigManager.loadConfig(ConfigManager.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_PATH).toString());
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> IngameConfigurationScreen::new);

		//ModLoadingContext.get().registerExtensionPoint(EpicFightExtensions.class, () -> new EpicFightExtensions(EpicFightCreativeTabs.ITEMS.get()));
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
		//EpicFightItemProperties.registerItemProperties();
		//SkillBookScreen.registerIconItems();
		EpicFightKeyMappings.registerKeys();
	}

	private void doServerStuff(final FMLDedicatedServerSetupEvent event) {
		this.animatorProvider = ServerAnimator::getAnimator;
	}

	private void doCommonStuff(final FMLCommonSetupEvent event) {
		event.enqueueWork(EpicFightCapabilities::registerCapabilities);
		event.enqueueWork(EpicFightSkills::registerSkills);//old
		event.enqueueWork(ProviderProjectile::registerPatches);//old
		event.enqueueWork(EpicFightEntities::registerSpawnPlacements);//old


		event.enqueueWork(EpicFightCommandArgumentTypes::registerArgumentTypes);
		//event.enqueueWork(EpicFightPotions::addRecipes);
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
		PlayerSkillCommand.register(event.getDispatcher());
	}


	private void registerDatapackReloadListnerEvent(final AddReloadListenerEvent event) {
		if (!isPhysicalClient()) {
			event.addListener(AnimationManager.getInstance());
		}

		event.addListener(new ColliderPreset());
		//event.addListener(new SkillManager());
		event.addListener(new WeaponTypeReloadListener());
		event.addListener(new ItemCapabilityReloadListener());
		event.addListener(new MobPatchReloadListener());
	}

	private void registerResourcepackReloadListnerEvent(final RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(new JointMaskReloadListener());
		event.registerReloadListener(Meshes.INSTANCE);
		event.registerReloadListener(AnimationManager.getInstance());
		event.registerReloadListener(ItemSkins.INSTANCE);
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