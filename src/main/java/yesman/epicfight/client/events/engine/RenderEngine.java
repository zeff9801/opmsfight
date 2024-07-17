package yesman.epicfight.client.events.engine;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.EntityIndicator;
import yesman.epicfight.client.gui.screen.overlay.OverlayManager;
import yesman.epicfight.client.renderer.AimHelperRenderer;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.renderer.patched.entity.PCustomEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.renderer.patched.item.*;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.*;
import yesman.epicfight.world.gamerule.EpicFightGamerules;
import yesman.epicfight.world.item.EpicFightItems;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
@OnlyIn(Dist.CLIENT)
public class RenderEngine {
	private static final Vec3f AIMING_CORRECTION = new Vec3f(-1.5F, 0.0F, 1.25F);

	public AimHelperRenderer aimHelper;
	private final BattleModeGui battleModeUI = new BattleModeGui(Minecraft.getInstance());
	public final Minecraft minecraft;
	private final Map<EntityType<?>, Supplier<PatchedEntityRenderer>> entityRendererProvider;
	private final Map<EntityType<?>, PatchedEntityRenderer> entityRendererCache;
	private final Map<Item, RenderItemBase> itemRendererMapByInstance;
	private final Map<Class<?>, RenderItemBase> itemRendererMapByClass;
	private FirstPersonRenderer firstPersonRenderer;
	private PHumanoidRenderer<?, ?, ?, ?, ?> basicHumanoidRenderer;
	private final OverlayManager overlayManager;
	private boolean zoomingIn;
	private int zoomOutStandbyTicks = 0;
	private int zoomCount;
	private final int maxZoomCount = 20;

	public RenderEngine() {
		Events.renderEngine = this;
		RenderItemBase.renderEngine = this;
		EntityIndicator.init();
		this.minecraft = Minecraft.getInstance();
		this.entityRendererProvider = HashBiMap.create();
		this.entityRendererCache = Maps.newHashMap();
		this.itemRendererMapByInstance = Maps.newHashMap();
		this.itemRendererMapByClass = Maps.newHashMap();
		this.firstPersonRenderer = new FirstPersonRenderer();
		this.overlayManager = new OverlayManager();
//		this.minecraft.renderBuffers().fixedBuffers.put(EpicFightRenderTypes.enchantedAnimatedArmor(), new BufferBuilder(EpicFightRenderTypes.enchantedAnimatedArmor().bufferSize()));
	}

	public void registerRenderer() {
		this.entityRendererProvider.clear();
		this.entityRendererCache.clear();
		this.itemRendererMapByInstance.clear();
		this.itemRendererMapByClass.clear();

		this.firstPersonRenderer = new FirstPersonRenderer();
		this.basicHumanoidRenderer = new PHumanoidRenderer<>(Meshes.BIPED);

//		this.entityRendererProvider.put(EntityType.CREEPER, PCreeperRenderer::new);
//		this.entityRendererProvider.put(EntityType.ENDERMAN, PEndermanRenderer::new);
		this.entityRendererProvider.put(EntityType.ZOMBIE, () -> new PHumanoidRenderer<>(Meshes.BIPED_OLD_TEX));
//		this.entityRendererProvider.put(EntityType.ZOMBIE_VILLAGER, PZombieVillagerRenderer::new);
//		this.entityRendererProvider.put(EntityType.ZOMBIFIED_PIGLIN, () -> new PHumanoidRenderer<>(Meshes.PIGLIN));
		this.entityRendererProvider.put(EntityType.HUSK, () -> new PHumanoidRenderer<>(Meshes.BIPED_OLD_TEX));
		this.entityRendererProvider.put(EntityType.SKELETON, () -> new PHumanoidRenderer<>(Meshes.SKELETON));
		this.entityRendererProvider.put(EntityType.WITHER_SKELETON, () -> new PHumanoidRenderer<>(Meshes.SKELETON));
//		this.entityRendererProvider.put(EntityType.STRAY, PStrayRenderer::new);
		this.entityRendererProvider.put(EntityType.PLAYER, PPlayerRenderer::new);
//		this.entityRendererProvider.put(EntityType.SPIDER, PSpiderRenderer::new);
//		this.entityRendererProvider.put(EntityType.CAVE_SPIDER, PSpiderRenderer::new);
//		this.entityRendererProvider.put(EntityType.IRON_GOLEM, PIronGolemRenderer::new);
//		this.entityRendererProvider.put(EntityType.VINDICATOR, PVindicatorRenderer::new);
//		this.entityRendererProvider.put(EntityType.EVOKER, PIllagerRenderer::new);
//		this.entityRendererProvider.put(EntityType.WITCH, PWitchRenderer::new);
//		this.entityRendererProvider.put(EntityType.DROWNED, PDrownedRenderer::new);
//		this.entityRendererProvider.put(EntityType.PILLAGER, PIllagerRenderer::new);
//		this.entityRendererProvider.put(EntityType.RAVAGER, PRavagerRenderer::new);
//		this.entityRendererProvider.put(EntityType.VEX, PVexRenderer::new);
//		this.entityRendererProvider.put(EntityType.PIGLIN, () -> new PHumanoidRenderer<>(Meshes.PIGLIN));
//		this.entityRendererProvider.put(EntityType.PIGLIN_BRUTE, () -> new PHumanoidRenderer<>(Meshes.PIGLIN));
//		this.entityRendererProvider.put(EntityType.HOGLIN, PHoglinRenderer::new);
//		this.entityRendererProvider.put(EntityType.ZOGLIN, PHoglinRenderer::new);
//		this.entityRendererProvider.put(EntityType.ENDER_DRAGON, PEnderDragonRenderer::new);
//		this.entityRendererProvider.put(EntityType.WITHER, PWitherRenderer::new);
//		this.entityRendererProvider.put(EpicFightEntities.WITHER_SKELETON_MINION.get(), PWitherSkeletonMinionRenderer::new);
//		this.entityRendererProvider.put(EpicFightEntities.WITHER_GHOST_CLONE.get(), WitherGhostCloneRenderer::new);


		RenderItemBase baseRenderer = new RenderItemBase();
		RenderBow bowRenderer = new RenderBow();
		RenderCrossbow crossbowRenderer = new RenderCrossbow();
		RenderTrident tridentRenderer = new RenderTrident();
//		RenderMap mapRenderer = new RenderMap();
		RenderShield shieldRenderer = new RenderShield();

		this.itemRendererMapByInstance.put(Items.AIR, baseRenderer);
		this.itemRendererMapByInstance.put(Items.BOW, bowRenderer);
		this.itemRendererMapByInstance.put(Items.SHIELD, shieldRenderer);
		this.itemRendererMapByInstance.put(Items.CROSSBOW, crossbowRenderer);
		this.itemRendererMapByInstance.put(Items.TRIDENT, tridentRenderer);
//		this.itemRendererMapByInstance.put(Items.FILLED_MAP, mapRenderer);
		this.itemRendererMapByInstance.put(EpicFightItems.KATANA.get(), new RenderKatana());

		//Render by item class
		this.itemRendererMapByClass.put(BowItem.class, bowRenderer);
		this.itemRendererMapByClass.put(CrossbowItem.class, crossbowRenderer);
		this.itemRendererMapByClass.put(TridentItem.class, tridentRenderer);
		this.itemRendererMapByClass.put(ShieldItem.class, shieldRenderer);

		//Render by capability class
		this.itemRendererMapByClass.put(BowCapability.class, bowRenderer);
		this.itemRendererMapByClass.put(CrossbowCapability.class, crossbowRenderer);
		this.itemRendererMapByClass.put(TridentCapability.class, tridentRenderer);
//		this.itemRendererMapByClass.put(MapCapability.class, mapRenderer);
		this.itemRendererMapByClass.put(ShieldCapability.class, shieldRenderer);

		this.aimHelper = new AimHelperRenderer();

		ModLoader.get().postEvent(new PatchedRenderersEvent.Add(this.entityRendererProvider, this.itemRendererMapByInstance));

		for (Map.Entry<EntityType<?>, Supplier<PatchedEntityRenderer>> entry : this.entityRendererProvider.entrySet()) {
			this.entityRendererCache.put(entry.getKey(), entry.getValue().get());
		}

		ModLoader.get().postEvent(new PatchedRenderersEvent.Modify(this.entityRendererCache));
	}

	public void registerCustomEntityRenderer(EntityType<?> entityType, String renderer, CompoundNBT compound) {
		if ("".equals(renderer)) {
			return;
		}

		if ("player".equals(renderer)) {
			this.entityRendererCache.put(entityType, this.basicHumanoidRenderer);
		} else if ("epicfight:custom".equals(renderer)) {
			AnimatedMesh mesh = Meshes.getOrCreateAnimatedMesh(Minecraft.getInstance().getResourceManager(), new ResourceLocation(compound.getString("model")), AnimatedMesh::new);
				this.entityRendererCache.put(entityType, new PCustomEntityRenderer(mesh));
		} else {
			EntityType<?> presetEntityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(renderer));

			if (this.entityRendererProvider.containsKey(presetEntityType)) {
				this.entityRendererCache.put(entityType, this.entityRendererProvider.get(presetEntityType).get());
			} else {
				throw new IllegalArgumentException("Datapack Mob Patch Crash: Invalid Renderer type " + renderer);
			}
		}

	}

	public RenderItemBase getItemRenderer(ItemStack itemstack) {
		RenderItemBase renderItem = this.itemRendererMapByInstance.get(itemstack.getItem());

		if (renderItem == null) {
			renderItem = this.findMatchingRendererByClass(itemstack.getClass());

			if (renderItem == null) {
				CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(itemstack);
				renderItem = this.findMatchingRendererByClass(itemCap.getClass());
			}

			if (renderItem == null) {
				renderItem = this.itemRendererMapByInstance.get(Items.AIR);
			}

			this.itemRendererMapByInstance.put(itemstack.getItem(), renderItem);
		}

		return renderItem;
	}

	private RenderItemBase findMatchingRendererByClass(Class<?> clazz) {
		RenderItemBase renderer = null;

		for (; clazz != null && renderer == null; clazz = clazz.getSuperclass()) {
			renderer = this.itemRendererMapByClass.get(clazz);
		}

		return renderer;
	}

	@SuppressWarnings("unchecked")
	public void renderEntityArmatureModel(LivingEntity livingEntity, LivingEntityPatch<?> entitypatch, LivingRenderer<? extends Entity, ?> renderer, IRenderTypeBuffer buffer, MatrixStack matStack, int packedLightIn, float partialTicks) {
		this.getEntityRenderer(livingEntity).render(livingEntity, entitypatch, renderer, buffer, matStack, packedLightIn, partialTicks);
	}

	public PatchedEntityRenderer getEntityRenderer(Entity entity) {
		return this.entityRendererCache.get(entity.getType());
	}

	public boolean hasRendererFor(Entity entity) {
		return this.entityRendererCache.computeIfAbsent(entity.getType(), (key) -> this.entityRendererProvider.containsKey(key) ? this.entityRendererProvider.get(entity.getType()).get() : null) != null;
	}

	public Set<ResourceLocation> getRendererEntries() {
		Set<ResourceLocation> availableRendererEntities = this.entityRendererProvider.keySet().stream().map((entityType) -> EntityType.getKey(entityType)).collect(Collectors.toSet());
		availableRendererEntities.add(new ResourceLocation(EpicFightMod.MODID, "custom"));

		return availableRendererEntities;
	}

	//Nothing happens if player is already zooming-in
	public void zoomIn() {
		if (!this.zoomingIn) {
			this.zoomingIn = true;
			this.zoomCount = this.zoomCount == 0 ? 1 : this.zoomCount;
		}
	}

	//Nothing happens if player is already zooming-out
	public void zoomOut(int zoomOutTicks) {
		if (this.zoomingIn) {
			this.zoomingIn = false;
			this.zoomOutStandbyTicks = zoomOutTicks;
		}
	}

	private void setRangedWeaponThirdPerson(CameraSetup event, PointOfView pov, double partialTicks) {
		if (ClientEngine.getInstance().getPlayerPatch() == null) {
			return;
		}

		ActiveRenderInfo camera = event.getInfo();
		Entity entity = minecraft.getCameraEntity();
		Vector3d vector = camera.getPosition();
		double totalX = vector.x();
		double totalY = vector.y();
		double totalZ = vector.z();

		if (pov == PointOfView.THIRD_PERSON_BACK) {
			double posX = vector.x();
			double posY = vector.y();
			double posZ = vector.z();
			double entityPosX = entity.xOld + (entity.getX() - entity.xOld) * partialTicks;
			double entityPosY = entity.yOld + (entity.getY() - entity.yOld) * partialTicks + entity.getEyeHeight();
			double entityPosZ = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks;
			float intpol = pov == PointOfView.THIRD_PERSON_BACK ? ((float) zoomCount / (float) maxZoomCount) : 0;
			Vec3f interpolatedCorrection = new Vec3f(AIMING_CORRECTION.x * intpol, AIMING_CORRECTION.y * intpol, AIMING_CORRECTION.z * intpol);
			OpenMatrix4f rotationMatrix = ClientEngine.getInstance().getPlayerPatch().getMatrix((float)partialTicks);
			Vec3f rotateVec = OpenMatrix4f.transform3v(rotationMatrix, interpolatedCorrection, null);
			double d3 = Math.sqrt((rotateVec.x * rotateVec.x) + (rotateVec.y * rotateVec.y) + (rotateVec.z * rotateVec.z));
			double smallest = d3;
			double d00 = posX + rotateVec.x;
			double d11 = posY - rotateVec.y;
			double d22 = posZ + rotateVec.z;

			for (int i = 0; i < 8; ++i) {
				float f = (float) ((i & 1) * 2 - 1);
				float f1 = (float) ((i >> 1 & 1) * 2 - 1);
				float f2 = (float) ((i >> 2 & 1) * 2 - 1);
				f = f * 0.1F;
				f1 = f1 * 0.1F;
				f2 = f2 * 0.1F;
				RayTraceResult raytraceresult = minecraft.level.clip(new RayTraceContext(new Vector3d(entityPosX + f, entityPosY + f1, entityPosZ + f2), new Vector3d(d00 + f + f2, d11 + f1, d22 + f2), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));

				if (raytraceresult != null) {
					double d7 = raytraceresult.getLocation().distanceTo(new Vector3d(entityPosX, entityPosY, entityPosZ));
					if (d7 < smallest) {
						smallest = d7;
					}
				}
			}

			float dist = d3 == 0 ? 0 : (float) (smallest / d3);
			totalX += rotateVec.x * dist;
			totalY -= rotateVec.y * dist;
			totalZ += rotateVec.z * dist;
		}

		camera.setPosition(totalX, totalY, totalZ);
	}

	public void correctCamera(CameraSetup event, float partialTicks) {
		LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
		ActiveRenderInfo camera = event.getInfo();
		PointOfView cameraType = this.minecraft.options.getCameraType();

		if (localPlayerPatch != null) {
			if (localPlayerPatch.getTarget() != null && localPlayerPatch.isTargetLockedOn()) {
				float xRot = localPlayerPatch.getLerpedLockOnX(event.getRenderPartialTicks());
				float yRot = localPlayerPatch.getLerpedLockOnY(event.getRenderPartialTicks());

				if (cameraType.isMirrored()) {
					yRot += 180.0F;
					xRot *= -1.0F;
				}

				camera.setRotation(yRot, xRot);
				event.setPitch(xRot);
				event.setYaw(yRot);

				if (!cameraType.isFirstPerson()) {
					Entity cameraEntity = this.minecraft.cameraEntity;

					camera.setPosition(MathHelper.lerp(partialTicks, cameraEntity.xo, cameraEntity.getX()),
							MathHelper.lerp(partialTicks, cameraEntity.yo, cameraEntity.getY())
									+ MathHelper.lerp(partialTicks, camera.eyeHeightOld, camera.eyeHeight),
							MathHelper.lerp(partialTicks, cameraEntity.zo, cameraEntity.getZ()));

					camera.move(-camera.getMaxZoom(4.0D), 0.0D, 0.0D);
				}
			}
		}
	}

	public OverlayManager getOverlayManager() {
		return this.overlayManager;
	}

	public FirstPersonRenderer getFirstPersonRenderer() {
		return firstPersonRenderer;
	}

	public void upSlideSkillUI() {
		this.battleModeUI.slideUp();
	}

	public void downSlideSkillUI() {
		this.battleModeUI.slideDown();
	}

	@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
	public static class Events {
		static RenderEngine renderEngine;
		private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");

		@SubscribeEvent
		public static void renderLivingEvent(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
			LivingEntity livingentity = event.getEntity();

			if (livingentity.level == null) {
				return;
			}

			if (renderEngine.hasRendererFor(livingentity)) {
				LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);
				LocalPlayerPatch playerpatch = null;
				float originalYRot = 0.0F;

				if ((event.getPartialRenderTick() == 0.0F || event.getPartialRenderTick() == 1.0F) && entitypatch instanceof LocalPlayerPatch localPlayerPatch) {
					playerpatch = localPlayerPatch;
					originalYRot = playerpatch.getCameraYRot();
					playerpatch.setModelYRotInGui(livingentity.yRot);
					event.getMatrixStack().translate(0, 0.1D, 0);
				}

				if (entitypatch != null && entitypatch.overrideRender()) {
					event.setCanceled(true);
					renderEngine.renderEntityArmatureModel(livingentity, entitypatch, event.getRenderer(), event.getBuffers(), event.getMatrixStack(), event.getLight(), event.getPartialRenderTick());
				}

				if (playerpatch != null) {
					playerpatch.disableModelYRotInGui(originalYRot);
				}
			}

			if (ClientEngine.getInstance().getPlayerPatch() != null && !renderEngine.minecraft.options.hideGui && !livingentity.level.getGameRules().getBoolean(EpicFightGamerules.DISABLE_ENTITY_UI)) {
				LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);

				for (EntityIndicator entityIndicator : EntityIndicator.ENTITY_INDICATOR_RENDERERS) {
					if (entityIndicator.shouldDraw(livingentity, entitypatch, ClientEngine.getInstance().getPlayerPatch())) {
						entityIndicator.drawIndicator(livingentity, entitypatch, ClientEngine.getInstance().getPlayerPatch(), event.getMatrixStack(), event.getBuffers(), event.getPartialRenderTick());
					}
				}
			}
		}

		@SubscribeEvent
		public static void itemTooltip(ItemTooltipEvent event) {
//			if (event.getPlayer() != null) {
//				CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(event.getItemStack());
//				LocalPlayerPatch playerpatch = (LocalPlayerPatch) event.getPlayer().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
//
//				if (cap != null && playerpatch != null) {
//					if (ClientEngine.getInstance().controllEngine.isKeyDown(EpicFightKeyMappings.SPECIAL_SKILL_TOOLTIP)) {
//						if (cap.getInnateSkill(playerpatch) != null) {
//							event.getToolTip().clear();
//							List<ITextComponent> skilltooltip = cap.getInnateSkill(playerpatch).getTooltipOnItem(event.getItemStack(), cap, playerpatch);
//
//							for (ITextComponent s : skilltooltip) {
//								event.getToolTip().add(s);
//							}
//						}
//					} else {
//						List<ITextComponent> tooltip = event.getToolTip();
//						cap.modifyItemTooltip(event.getItemStack(), event.getToolTip(), playerpatch);
//
//						for (int i = 0; i < tooltip.size(); i++) {
//							ITextComponent textComp = tooltip.get(i);
//
//							if (!textComp.getSiblings().isEmpty()) {
//								ITextComponent sibling = textComp.getSiblings().get(0);
//
//								if (sibling instanceof TranslationTextComponent) {
//									TranslationTextComponent translationComponent = (TranslationTextComponent)sibling;
//
//									if (translationComponent.getArgs().length > 1 && translationComponent.getArgs()[1] instanceof TranslationTextComponent) {
//										CapabilityItem itemCapability = EpicFightCapabilities.getItemStackCapability(event.getItemStack());
//
//										if (((TranslationTextComponent)translationComponent.getArgs()[1]).getKey().equals(Attributes.ATTACK_SPEED.getDescriptionId())) {
//											float weaponSpeed = (float)playerpatch.getOriginal().getAttribute(Attributes.ATTACK_SPEED).getBaseValue();
//
//											for (AttributeModifier modifier : event.getItemStack().getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_SPEED)) {
//												weaponSpeed += (float) modifier.getAmount();
//											}
//
//											if (itemCapability != null) {
//												for (AttributeModifier modifier : itemCapability.getAttributeModifiers(EquipmentSlotType.MAINHAND, playerpatch).get(Attributes.ATTACK_SPEED)) {
//													weaponSpeed += (float) modifier.getAmount();
//												}
//											}
//
//											tooltip.remove(i);
//											tooltip.add(i, new StringTextComponent(String.format(" %.2f ", playerpatch.getAttackSpeed(cap, weaponSpeed))).append(new TranslationTextComponent(Attributes.ATTACK_SPEED.getDescriptionId())));
//										} else if (((TranslationTextComponent)translationComponent.getArgs()[1]).getKey().equals(Attributes.ATTACK_DAMAGE.getDescriptionId())) {
//											float weaponDamage = (float)playerpatch.getOriginal().getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
//											weaponDamage += EnchantmentHelper.getDamageBonus(event.getItemStack(), CreatureAttribute.UNDEFINED);
//
//											for (AttributeModifier modifier : event.getItemStack().getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
//												weaponDamage += (float) modifier.getAmount();
//											}
//
//											if (itemCapability != null) {
//
//												for (AttributeModifier modifier : itemCapability.getAttributeModifiers(EquipmentSlotType.MAINHAND, playerpatch).get(Attributes.ATTACK_DAMAGE)) {
//													weaponDamage += (float) modifier.getAmount();
//												}
//											}
//
//											tooltip.remove(i);
//											tooltip.add(i, new StringTextComponent(String.format(" %.0f ", playerpatch.getDamageToEntity(null, null, weaponDamage))).append(new TranslationTextComponent(Attributes.ATTACK_DAMAGE.getDescriptionId())).withStyle(TextFormatting.DARK_GREEN));
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
		}


		@SubscribeEvent
		public static void cameraSetupEvent(CameraSetup event) {
			boolean aimCorrection = true;
//			aimCorrection = EpicFightMod.CLIENT_CONFIGS.aimingCorrection.getValue();
			if (renderEngine.zoomCount > 0 && aimCorrection) {
			renderEngine.setRangedWeaponThirdPerson(event, renderEngine.minecraft.options.getCameraType(), event.getRenderPartialTicks());

				if (renderEngine.zoomOutStandbyTicks > 0) {
					renderEngine.zoomOutStandbyTicks--;
				} else {
					renderEngine.zoomCount = renderEngine.zoomingIn ? renderEngine.zoomCount + 1 : renderEngine.zoomCount - 1;
				}

				renderEngine.zoomCount = Math.min(renderEngine.maxZoomCount, renderEngine.zoomCount);
			}

			renderEngine.correctCamera(event, (float)event.getRenderPartialTicks());
		}

		@SubscribeEvent
		public static void fogEvent(RenderFogEvent event) {
		}

		@SuppressWarnings("deprecation")
		@SubscribeEvent
		public static void renderGameOverlayPost(RenderGameOverlayEvent.BossInfo event) {
			if (event.getBossInfo().getName().getString().equals("Ender Dragon")) {
			}
		}

		@SuppressWarnings("unchecked")
		@SubscribeEvent
		public static void renderHand(RenderHandEvent event) {
			LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();

			if (playerpatch != null) {
				boolean isBattleMode = playerpatch.isBattleMode();

				if (isBattleMode || !EpicFightMod.CLIENT_CONFIGS.filterAnimation.getValue()) {
					if (event.getHand() == Hand.MAIN_HAND) {
						renderEngine.firstPersonRenderer.render(playerpatch.getOriginal(), playerpatch, (LivingRenderer)renderEngine.minecraft.getEntityRenderDispatcher().getRenderer(playerpatch.getOriginal()),
								event.getBuffers(), event.getMatrixStack(), event.getLight(), event.getPartialTicks());
					}

					event.setCanceled(true);
				}
			}
		}

		@SubscribeEvent
		public static void renderWorldLast(RenderWorldLastEvent event) {
			boolean aimCorrection = true;
//			aimCorrection = EpicFightMod.CLIENT_CONFIGS.aimingCorrection.getValue()
			//			if (aimCorrection && renderEngine.zoomCount > 0 && renderEngine.minecraft.options.getCameraType() == PointOfView.THIRD_PERSON_BACK && event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			if (aimCorrection && renderEngine.zoomCount > 0 && renderEngine.minecraft.options.getCameraType() == PointOfView.THIRD_PERSON_BACK) {
				renderEngine.aimHelper.doRender(event.getMatrixStack(), event.getPartialTicks());
			}

			/**
			 if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
			 renderEngine.betaWarningMessage.drawMessage(event.getPoseStack());
			 }**/
		}

//		@SuppressWarnings("unchecked")
//		@SubscribeEvent
//		public static void renderEnderDragonEvent(RenderEnderDragonEvent event) {
//			EnderDragon livingentity = event.getEntity();
//
//			if (renderEngine.hasRendererFor(livingentity)) {
//				EnderDragonPatch entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, EnderDragonPatch.class);
//
//				if (entitypatch != null) {
//					event.setCanceled(true);
//					renderEngine.getEntityRenderer(livingentity).render(livingentity, entitypatch, event.getRenderer(), event.getBuffers(), event.getPoseStack(), event.getLight(), event.getPartialRenderTick());
//				}
//			}
//		}

		@SubscribeEvent
		public static void renderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
			if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
				MainWindow window = Minecraft.getInstance().getWindow();
				LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();

				if (playerpatch != null) {
					for (SkillContainer skillContainer : playerpatch.getSkillCapability().skillContainers) {
						if (skillContainer.getSkill() != null) {
							skillContainer.getSkill().onScreen(playerpatch, window.getGuiScaledWidth(), window.getGuiScaledHeight());
						}
					}

					renderEngine.overlayManager.renderTick(window.getGuiScaledWidth(), window.getGuiScaledHeight());

//					if (Minecraft.renderNames() && !(Minecraft.getInstance().screen instanceof UISetupScreen)) {
//						renderEngine.battleModeUI.renderGui(playerpatch, event.getPartialTicks());
//					}
				}
			}
			}

	}
}