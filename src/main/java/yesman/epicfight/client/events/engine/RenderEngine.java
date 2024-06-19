package yesman.epicfight.client.events.engine;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.EntityIndicator;
import yesman.epicfight.client.gui.screen.overlay.OverlayManager;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.renderer.AimHelperRenderer;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.renderer.patched.item.RenderBow;
import yesman.epicfight.client.renderer.patched.item.RenderCrossbow;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.client.renderer.patched.item.RenderKatana;
import yesman.epicfight.client.renderer.patched.item.RenderShield;
import yesman.epicfight.client.renderer.patched.item.RenderTrident;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.gamerule.EpicFightGamerules;
import yesman.epicfight.world.item.EpicFightItems;

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
	private final Map<Class<? extends Item>, RenderItemBase> itemRendererMapByClass;
	private FirstPersonRenderer firstPersonRenderer;
	private final OverlayManager overlayManager;
	private boolean aiming;
	private int zoomOutTimer = 0;
	private int zoomCount;
	private final int zoomMaxCount = 20;
	private float cameraXRot;
	private float cameraYRot;
	private boolean isPlayerRotationLocked;

	public RenderEngine() {
		Events.renderEngine = this;
		RenderItemBase.renderEngine = this;
		EntityIndicator.init();
		this.minecraft = Minecraft.getInstance();
		this.entityRendererProvider = Maps.newHashMap();
		this.entityRendererCache = Maps.newHashMap();
		this.itemRendererMapByInstance = Maps.newHashMap();
		this.itemRendererMapByClass = Maps.newHashMap();
		this.firstPersonRenderer = new FirstPersonRenderer();
		this.overlayManager = new OverlayManager();
		this.minecraft.renderBuffers().fixedBuffers.put(EpicFightRenderTypes.enchantedAnimatedArmor(), new BufferBuilder(EpicFightRenderTypes.enchantedAnimatedArmor().bufferSize()));
	}

	public void registerRenderer() {
		this.entityRendererProvider.put(EntityType.PLAYER, PPlayerRenderer::new);


		RenderBow bowRenderer = new RenderBow();
		RenderCrossbow crossbowRenderer = new RenderCrossbow();
		RenderShield shieldRenderer = new RenderShield();
		RenderTrident tridentRenderer = new RenderTrident();

		this.itemRendererMapByInstance.clear();
		this.itemRendererMapByInstance.put(Items.AIR, new RenderItemBase());
		this.itemRendererMapByInstance.put(Items.BOW, bowRenderer);
		this.itemRendererMapByInstance.put(Items.SHIELD, shieldRenderer);
		this.itemRendererMapByInstance.put(Items.CROSSBOW, crossbowRenderer);
		this.itemRendererMapByInstance.put(Items.TRIDENT, tridentRenderer);
		this.itemRendererMapByInstance.put(EpicFightItems.KATANA.get(), new RenderKatana());
		this.itemRendererMapByClass.put(BowItem.class, bowRenderer);
		this.itemRendererMapByClass.put(CrossbowItem.class, crossbowRenderer);
		this.itemRendererMapByClass.put(ShieldItem.class, shieldRenderer);
		this.itemRendererMapByClass.put(TridentItem.class, tridentRenderer);
		this.aimHelper = new AimHelperRenderer();

		ModLoader.get().postEvent(new PatchedRenderersEvent.Add(this.entityRendererProvider, this.itemRendererMapByInstance));

		for (Map.Entry<EntityType<?>, Supplier<PatchedEntityRenderer>> entry : this.entityRendererProvider.entrySet()) {
			this.entityRendererCache.put(entry.getKey(), entry.getValue().get());
		}

		ModLoader.get().postEvent(new PatchedRenderersEvent.Modify(this.entityRendererCache));
	}

	public void registerCustomEntityRenderer(EntityType<?> entityType, String renderer) {
		if ("".equals(renderer)) {
			return;
		}

		EntityType<?> presetEntityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(renderer));

		if (this.entityRendererProvider.containsKey(presetEntityType)) {
			this.entityRendererCache.put(entityType, this.entityRendererProvider.get(presetEntityType).get());
			return;
		}

		throw new IllegalArgumentException("Datapack Mob Patch Crash: Invalid Renderer type " + renderer);
	}

	public RenderItemBase getItemRenderer(Item item) {
		RenderItemBase renderItem = this.itemRendererMapByInstance.get(item);

		if (renderItem == null) {
			renderItem = this.findMatchingRendererByClass(item.getClass());

			if (renderItem == null) {
				renderItem = this.itemRendererMapByInstance.get(Items.AIR);
			}

			this.itemRendererMapByInstance.put(item, renderItem);
		}

		return renderItem;
	}

	private RenderItemBase findMatchingRendererByClass(Class<?> clazz) {
		RenderItemBase renderer = null;

		for (; clazz != null && renderer == null; clazz = clazz.getSuperclass()) {
			renderer = this.itemRendererMapByClass.getOrDefault(clazz, null);
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

	public void clearCustomEntityRenerer() {
		this.entityRendererCache.clear();
	}

	public void zoomIn() {
		this.aiming = true;
		this.zoomCount = this.zoomCount == 0 ? 1 : this.zoomCount;
		this.zoomOutTimer = 0;
	}

	public void zoomOut(int timer) {
		this.aiming = false;
		this.zoomOutTimer = timer;
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

		if (pov == PointOfView.THIRD_PERSON_BACK && zoomCount > 0) {
			double posX = vector.x();
			double posY = vector.y();
			double posZ = vector.z();
			double entityPosX = entity.xOld + (entity.getX() - entity.xOld) * partialTicks;
			double entityPosY = entity.yOld + (entity.getY() - entity.yOld) * partialTicks + entity.getEyeHeight();
			double entityPosZ = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks;
			float intpol = pov == PointOfView.THIRD_PERSON_BACK ? ((float) zoomCount / (float) zoomMaxCount) : 0;
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


	public void setCameraRotation(float x, float y) {
		float f = (float)x * 0.15F;
		float f1 = (float)y * 0.15F;

		if (!this.isPlayerRotationLocked) {
			this.cameraXRot = this.minecraft.player.xRot;
			this.cameraYRot = this.minecraft.player.yRot;
			this.cameraXRot = this.minecraft.player.xRotO;
			this.cameraYRot = this.minecraft.player.yRotO;
		}

		this.cameraXRot += f;
		this.cameraYRot += f1;
		this.cameraXRot = MathHelper.clamp(this.cameraXRot, -90.0F, 90.0F);

		this.cameraXRot += f;
		this.cameraYRot += f1;
		this.cameraXRot = MathHelper.clamp(this.cameraXRot, -90.0F, 90.0F);

		this.isPlayerRotationLocked = true;
	}

	public boolean isPlayerRotationLocked() {
		return this.isPlayerRotationLocked;
	}

	public float getCorrectedXRot() {
		return this.cameraXRot;
	}

	public float getCorrectedYRot() {
		return this.cameraYRot;
	}

	public void unlockRotation(Entity cameraEntity) {
		if (this.isPlayerRotationLocked) {
			cameraEntity.xRot = (this.cameraXRot);
			cameraEntity.yRot = (this.cameraYRot);
		}

		this.isPlayerRotationLocked = false;
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
					//playerpatch.setModelYRotInGui(livingentity.yRot); TODO
					event.getMatrixStack().translate(0, 0.1D, 0);
				}

				if (entitypatch != null && entitypatch.overrideRender()) {
					event.setCanceled(true);
					renderEngine.renderEntityArmatureModel(livingentity, entitypatch, event.getRenderer(), event.getBuffers(), event.getMatrixStack(), event.getLight(), event.getPartialRenderTick());
				}

				if (playerpatch != null) {
					//playerpatch.disableModelYRotInGui(originalYRot); TODO
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
			if (event.getPlayer() != null) {
				CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(event.getItemStack());
				LocalPlayerPatch playerpatch = (LocalPlayerPatch) event.getPlayer().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);

				if (cap != null && playerpatch != null) {
					if (ClientEngine.getInstance().controllEngine.isKeyDown(EpicFightKeyMappings.SPECIAL_SKILL_TOOLTIP)) {
						if (cap.getInnateSkill(playerpatch) != null) {
							event.getToolTip().clear();
							List<ITextComponent> skilltooltip = cap.getInnateSkill(playerpatch).getTooltipOnItem(event.getItemStack(), cap, playerpatch);

							for (ITextComponent s : skilltooltip) {
								event.getToolTip().add(s);
							}
						}
					} else {
						List<ITextComponent> tooltip = event.getToolTip();
						cap.modifyItemTooltip(event.getItemStack(), event.getToolTip(), playerpatch);

						for (int i = 0; i < tooltip.size(); i++) {
							ITextComponent textComp = tooltip.get(i);

							if (!textComp.getSiblings().isEmpty()) {
								ITextComponent sibling = textComp.getSiblings().get(0);

								if (sibling instanceof TranslationTextComponent) {
									TranslationTextComponent translationComponent = (TranslationTextComponent)sibling;

									if (translationComponent.getArgs().length > 1 && translationComponent.getArgs()[1] instanceof TranslationTextComponent) {
										CapabilityItem itemCapability = EpicFightCapabilities.getItemStackCapability(event.getItemStack());

										if (((TranslationTextComponent)translationComponent.getArgs()[1]).getKey().equals(Attributes.ATTACK_SPEED.getDescriptionId())) {
											float weaponSpeed = (float)playerpatch.getOriginal().getAttribute(Attributes.ATTACK_SPEED).getBaseValue();

											for (AttributeModifier modifier : event.getItemStack().getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_SPEED)) {
												weaponSpeed += (float) modifier.getAmount();
											}

											if (itemCapability != null) {
												for (AttributeModifier modifier : itemCapability.getAttributeModifiers(EquipmentSlotType.MAINHAND, playerpatch).get(Attributes.ATTACK_SPEED)) {
													weaponSpeed += (float) modifier.getAmount();
												}
											}

											tooltip.remove(i);
											tooltip.add(i, new StringTextComponent(String.format(" %.2f ", playerpatch.getAttackSpeed(cap, weaponSpeed))).append(new TranslationTextComponent(Attributes.ATTACK_SPEED.getDescriptionId())));
										} else if (((TranslationTextComponent)translationComponent.getArgs()[1]).getKey().equals(Attributes.ATTACK_DAMAGE.getDescriptionId())) {
											float weaponDamage = (float)playerpatch.getOriginal().getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
											weaponDamage += EnchantmentHelper.getDamageBonus(event.getItemStack(), CreatureAttribute.UNDEFINED);

											for (AttributeModifier modifier : event.getItemStack().getAttributeModifiers(EquipmentSlotType.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
												weaponDamage += (float) modifier.getAmount();
											}

											if (itemCapability != null) {

												for (AttributeModifier modifier : itemCapability.getAttributeModifiers(EquipmentSlotType.MAINHAND, playerpatch).get(Attributes.ATTACK_DAMAGE)) {
													weaponDamage += (float) modifier.getAmount();
												}
											}

											tooltip.remove(i);
											tooltip.add(i, new StringTextComponent(String.format(" %.0f ", playerpatch.getDamageToEntity(null, null, weaponDamage))).append(new TranslationTextComponent(Attributes.ATTACK_DAMAGE.getDescriptionId())).withStyle(TextFormatting.DARK_GREEN));
										}
									}
								}
							}
						}
					}
				}
			}
		}

		@SubscribeEvent
		public static void cameraSetupEvent(CameraSetup event) {
			renderEngine.setRangedWeaponThirdPerson(event, renderEngine.minecraft.options.getCameraType(), event.getRenderPartialTicks());

			if (renderEngine.zoomCount > 0) {
				if (renderEngine.zoomOutTimer > 0) {
					renderEngine.zoomOutTimer--;
				} else {
					renderEngine.zoomCount = renderEngine.aiming ? renderEngine.zoomCount + 1 : renderEngine.zoomCount - 1;
				}

				renderEngine.zoomCount = Math.min(renderEngine.zoomMaxCount, renderEngine.zoomCount);
			}

			renderEngine.correctCamera(event, (float)event.getRenderPartialTicks());
		}

		@SubscribeEvent
		public static void fogEvent(RenderFogEvent event) {
		}

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

					if (Minecraft.renderNames()) {
						renderEngine.battleModeUI.renderGui(playerpatch, event.getPartialTicks());
					}
				}
			}
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
			if (renderEngine.zoomCount > 0 && renderEngine.minecraft.options.getCameraType() == PointOfView.THIRD_PERSON_BACK) {
				renderEngine.aimHelper.doRender(event.getMatrixStack(), event.getPartialTicks());
			}
		}

	}
}