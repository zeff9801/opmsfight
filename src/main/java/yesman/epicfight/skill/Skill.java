package yesman.epicfight.skill;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPExecuteSkill;
import yesman.epicfight.network.server.SPSetSkillValue;
import yesman.epicfight.network.server.SPSetSkillValue.Target;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.SkillCancelEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Skill extends ForgeRegistryEntry<Skill> {
	public static class Builder<T extends Skill> {
		protected ResourceLocation registryName;
		protected SkillCategory category;
		protected ActivateType activateType;
		protected Resource resource;

		public Builder<T> setRegistryName(ResourceLocation registryName) {
			this.registryName = registryName;
			return this;
		}

		public Builder<T> setCategory(SkillCategory category) {
			this.category = category;
			return this;
		}

		public Builder<T> setActivateType(ActivateType activateType) {
			this.activateType = activateType;
			return this;
		}

		public Builder<T> setResource(Resource resource) {
			this.resource = resource;
			return this;
		}
	}

	public static Builder<Skill> createBuilder() {
		return new Builder<Skill>();
	}

	public static Skill.Builder<Skill> createIdentityBuilder() {
		return (new Skill.Builder<Skill>()).setCategory(SkillCategories.IDENTITY).setResource(Resource.NONE);
	}

	public static Skill.Builder<Skill> createMoverBuilder() {
		return (new Skill.Builder<Skill>()).setCategory(SkillCategories.MOVER).setResource(Resource.STAMINA);
	}

	private final Map<Attribute, AttributeModifier> attributes = Maps.newHashMap();
	protected final ResourceLocation registryName;
	protected final SkillCategory category;
	protected final ActivateType activateType;
	protected final Resource resource;
	protected float consumption;
	protected int maxDuration;
	protected int maxStackSize;
	protected int requiredXp;

	public Skill(Builder<? extends Skill> builder) {
		if (builder.registryName == null) {
			Exception e = new IllegalArgumentException("No registry name is given for " + this.getClass().getCanonicalName());
			e.printStackTrace();
		}

		this.registryName = builder.registryName;
		this.category = builder.category;
		this.activateType = builder.activateType;
		this.resource = builder.resource;
	}

	public void setParams(CompoundNBT parameters) {
		this.consumption = parameters.getFloat("consumption");
		this.maxDuration = parameters.getInt("max_duration");
		this.maxStackSize = parameters.contains("max_stacks") ? parameters.getInt("max_stacks") : 1;
		this.requiredXp = parameters.getInt("xp_requirement");

		this.attributes.clear();

		if (parameters.contains("attribute_modifiers")) {
			ListNBT attributeList = parameters.getList("attribute_modifiers", 10);

			for (INBT tag : attributeList) {
				CompoundNBT comp = (CompoundNBT)tag;
				String attribute = comp.getString("attribute");
				Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attribute));
				AttributeModifier modifier = ParseUtil.toAttributeModifier(comp);

				this.attributes.put(attr, modifier);
			}
		}
	}

	public boolean isExecutableState(PlayerPatch<?> executor) {
		return !executor.getOriginal().isSpectator() && !executor.footsOnGround() && executor.getEntityState().canUseSkill();
	}

	public boolean canExecute(PlayerPatch<?> executor) {
		return this.checkExecuteCondition(executor);
	}

	/**
	 * This makes the skill icon white if it returns false
	 */
	public boolean checkExecuteCondition(PlayerPatch<?> executor) {
		return true;
	}

	/**
	 * Get a packet to send to the server
	 */
	@OnlyIn(Dist.CLIENT)
	public Object getExecutionPacket(LocalPlayerPatch executor, PacketBuffer args) {
		return new CPExecuteSkill(executor.getSkill(this).getSlotId(), CPExecuteSkill.WorkType.ACTIVATE, args);
	}

	@OnlyIn(Dist.CLIENT)
	public PacketBuffer gatherArguments(LocalPlayerPatch executor, ControllEngine controllEngine) {
		return null;
	}

	public void executeOnServer(ServerPlayerPatch executor, PacketBuffer args) {
		SPSkillExecutionFeedback feedbackPacket = SPSkillExecutionFeedback.executed(executor.getSkill(this).getSlotId());

		if (executor.isChargingSkill()) {
			if (this instanceof ChargeableSkill chargingSkill) {
				feedbackPacket.getBuffer().writeInt(executor.getAccumulatedChargeAmount());
				chargingSkill.castSkill(executor, executor.getSkill(this), executor.getAccumulatedChargeAmount(), feedbackPacket, false);
				executor.resetSkillCharging();
				EpicFightNetworkManager.sendToPlayer(feedbackPacket, executor.getOriginal());
			}
		} else {
			executor.getSkill(this).activate();
			EpicFightNetworkManager.sendToPlayer(feedbackPacket, executor.getOriginal());
		}
	}

	public void cancelOnServer(ServerPlayerPatch executor, PacketBuffer args) {
		SkillCancelEvent skillCancelEvent = new SkillCancelEvent(executor, executor.getSkill(this));
		executor.getEventListener().triggerEvents(PlayerEventListener.EventType.SKILL_CANCEL_EVENT, skillCancelEvent);
		EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.expired(executor.getSkill(this).getSlotId()), executor.getOriginal());
	}

	public final float getDefaultConsumptionAmount(PlayerPatch<?> executer) {
		switch (this.resource) {
			case STAMINA:
				return executer.getModifiedStaminaConsume(this.consumption);
			case WEAPON_CHARGE:
				return 1;
			case COOLDOWN:
				return 1;
			default:
				return 0.0F;
		}
	}

	/**
	 * Instant feedback when the skill is executed successfully
	 * @param executor
	 * @param args
	 */
	@OnlyIn(Dist.CLIENT)
	public void executeOnClient(LocalPlayerPatch executor, PacketBuffer args) {
	}

	/**
	 * Called when the duration ends.
	 * @param executor
	 * @param args
	 */
	@OnlyIn(Dist.CLIENT)
	public void cancelOnClient(LocalPlayerPatch executor, PacketBuffer args) {
		SkillCancelEvent skillCancelEvent = new SkillCancelEvent(executor, executor.getSkill(this));
		executor.getEventListener().triggerEvents(PlayerEventListener.EventType.SKILL_CANCEL_EVENT, skillCancelEvent);
	}

	public void onInitiate(SkillContainer container) {
		container.maxDuration = this.maxDuration;

		for (Map.Entry<Attribute, AttributeModifier> stat : this.attributes.entrySet()) {
			ModifiableAttributeInstance attr = container.getExecuter().getOriginal().getAttribute(stat.getKey());

			if (!attr.hasModifier(stat.getValue())) {
				attr.addTransientModifier(stat.getValue());
			}
		}
	}

	/**
	 * When skill removed from the container
	 * @param container
	 */
	public void onRemoved(SkillContainer container) {
		for (Map.Entry<Attribute, AttributeModifier> stat : this.attributes.entrySet()) {
			ModifiableAttributeInstance attr = container.getExecuter().getOriginal().getAttribute(stat.getKey());

			if (attr.hasModifier(stat.getValue())) {
				attr.removeModifier(stat.getValue());
			}
		}
	}

	/**
	 * When stacks reach to zero
	 * @param container
	 */
	public void onReset(SkillContainer container) {
	}

	public void setConsumption(SkillContainer container, float value) {
		container.resource = Math.min(Math.max(value, 0), container.getMaxResource());

		if (value >= container.getMaxResource()) {
			if (container.stack < this.maxStackSize) {
				container.stack++;
				container.resource = 0;
				container.prevResource = 0;
			} else {
				container.resource = container.getMaxResource();
				container.prevResource = container.getMaxResource();
			}
		} else if (value == 0 && container.stack > 0) {
			--container.stack;
		}
	}

	public void updateContainer(SkillContainer container) {
		PlayerPatch<?> executer = container.getExecuter();
		container.prevResource = container.resource;
		container.prevDuration = container.duration;

		if (this.resource == Resource.COOLDOWN) {
			if (container.stack < this.maxStackSize) {
				container.setResource(container.resource + this.getCooldownRegenPerSecond(executer) * EpicFightOptions.A_TICK);
			}
		}

		if (container.isActivated()) {
			if (this.activateType == ActivateType.DURATION) {
				container.duration--;
			}

			boolean isEnd = false;

			if (this.activateType == ActivateType.TOGGLE) {
				if (container.stack <= 0 && !executer.getOriginal().isCreative()) {
					isEnd = true;
				}
			} else {
				if (container.duration <= 0) {
					isEnd = true;
				}
			}

			if (isEnd) {
				if (!container.getExecuter().isLogicalClient() && this.activateType != ActivateType.CHARGING) {
					this.cancelOnServer((ServerPlayerPatch)executer, null);
				}

				container.deactivate();
			}
		}

		if (this.activateType == Skill.ActivateType.CHARGING && container.getExecuter().getChargingSkill() == this) {
			ChargeableSkill chargingSkill = (ChargeableSkill)this;
			chargingSkill.chargingTick(executer);

			if (!container.getExecuter().isLogicalClient()) {
				container.getExecuter().resetActionTick();

				if (container.getExecuter().getSkillChargingTicks(1.0F) > chargingSkill.getAllowedMaxChargingTicks()) {
					SPSkillExecutionFeedback feedbackPacket = SPSkillExecutionFeedback.executed(executer.getSkill(this).getSlotId());
					feedbackPacket.getBuffer().writeInt(executer.getAccumulatedChargeAmount());
					chargingSkill.castSkill((ServerPlayerPatch)executer, container, container.getExecuter().getAccumulatedChargeAmount(), feedbackPacket, true);
					container.getExecuter().resetSkillCharging();

					EpicFightNetworkManager.sendToPlayer(feedbackPacket, (ServerPlayerEntity) container.getExecuter().getOriginal());
				}
			}
		}
	}

	public void setConsumptionSynchronize(ServerPlayerPatch executer, float amount) {
		setConsumptionSynchronize(executer, this, amount);
	}

	public void setMaxDurationSynchronize(ServerPlayerPatch executer, int amount) {
		setMaxDurationSynchronize(executer, this, amount);
	}

	public void setDurationSynchronize(ServerPlayerPatch executer, int amount) {
		setDurationSynchronize(executer, this, amount);
	}

	public void setStackSynchronize(ServerPlayerPatch executer, int amount) {
		setStackSynchronize(executer, this, amount);
	}

	public void setMaxResourceSynchronize(ServerPlayerPatch executer, float amount) {
		setMaxResourceSynchronize(executer, this, amount);
	}


	public ResourceLocation getSkillRegistryName() {
		return this.registryName;
	}

	public static void setConsumptionSynchronize(ServerPlayerPatch executer, Skill skill, float amount) {
		SkillContainer skillContainer = executer.getSkill(skill);
		skillContainer.setResource(amount);

		EpicFightNetworkManager.sendToPlayer(new SPSetSkillValue(Target.RESOURCE, skillContainer.getSlotId(), amount, false), executer.getOriginal());
	}

	public static void setDurationSynchronize(ServerPlayerPatch executer, Skill skill, int amount) {
		SkillContainer skillContainer = executer.getSkill(skill);
		skillContainer.setDuration(amount);

		EpicFightNetworkManager.sendToPlayer(new SPSetSkillValue(Target.DURATION, skillContainer.getSlotId(), amount, false), executer.getOriginal());
	}

	public static void setMaxDurationSynchronize(ServerPlayerPatch executer, Skill skill, int amount) {
		SkillContainer skillContainer = executer.getSkill(skill);
		skillContainer.setMaxDuration(amount);

		EpicFightNetworkManager.sendToPlayer(new SPSetSkillValue(Target.MAX_DURATION, skillContainer.getSlotId(), amount, false), executer.getOriginal());
	}

	public static void setStackSynchronize(ServerPlayerPatch executer, Skill skill, int amount) {
		SkillContainer skillContainer = executer.getSkill(skill);
		skillContainer.setStack(amount);

		EpicFightNetworkManager.sendToPlayer(new SPSetSkillValue(Target.STACK, skillContainer.getSlotId(), amount, false), executer.getOriginal());
	}

	public static void setMaxResourceSynchronize(ServerPlayerPatch executer, Skill skill, float amount) {
		SkillContainer skillContainer = executer.getSkill(skill);
		skillContainer.setMaxResource(amount);

		EpicFightNetworkManager.sendToPlayer(new SPSetSkillValue(Target.MAX_RESOURCE, skillContainer.getSlotId(), amount, false), executer.getOriginal());
	}

	public String getTranslationKey() {
		return String.format("skill.%s.%s", this.getSkillRegistryName().getNamespace(), this.getSkillRegistryName().getPath());
	}

	public float getCooldownRegenPerSecond(PlayerPatch<?> player) {
		return 1.0F;
	}

	public SkillCategory getCategory() {
		return this.category;
	}

	public int getMaxStack() {
		return this.maxStackSize;
	}

	public int getMaxDuration() {
		return this.maxDuration;
	}

	public float getConsumption() {
		return this.consumption;
	}

	public int getRequiredXp() {
		return this.requiredXp;
	}

	public Set<Map.Entry<Attribute, AttributeModifier>> getModfierEntry() {
		return this.attributes.entrySet();
	}

	public boolean resourcePredicate(PlayerPatch<?> playerpatch) {
		return playerpatch.consumeForSkill(this, this.resource);
	}

	public boolean shouldDeactivateAutomatically(PlayerPatch<?> executer) {
		return !executer.getOriginal().isCreative();
	}

	public ActivateType getActivateType() {
		return this.activateType;
	}

	public Resource getResourceType() {
		return this.resource;
	}

	public Skill getPriorSkill() {
		return null;
	}

	public Skill registerPropertiesToAnimation() {
		return this;
	}

	@OnlyIn(Dist.CLIENT)
	public void onScreen(LocalPlayerPatch playerpatch, float resolutionX, float resolutionY) {

	}

	@OnlyIn(Dist.CLIENT)
	public List<ITextComponent> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
		return Lists.newArrayList();
	}

	@OnlyIn(Dist.CLIENT)
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		return list;
	}

	@OnlyIn(Dist.CLIENT)
	public void drawOnGui(BattleModeGui gui, SkillContainer container, MatrixStack matStackIn, float x, float y, float scale, int width, int height) {
	}

	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getSkillTexture() {
		ResourceLocation name = this.getSkillRegistryName();
		return new ResourceLocation(name.getNamespace(), "textures/gui/skills/" + name.getPath() + ".png");
	}

	@OnlyIn(Dist.CLIENT)
	public boolean shouldDraw(SkillContainer container) {
		return false;
	}

	@Override
	public String toString() {
		return this.getSkillRegistryName().toString();
	}

	@OnlyIn(Dist.CLIENT)
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent(String.format("%s.%s.%s", "skill", this.getSkillRegistryName().getNamespace(), this.getSkillRegistryName().getPath()));
	}

	@OnlyIn(Dist.CLIENT)
	public List<WeaponCategory> getAvailableWeaponCategories() {
		return null;
	}

	public enum ActivateType {
		ONE_SHOT, DURATION, DURATION_INFINITE, TOGGLE, CHARGING
	}

	public enum Resource {
		NONE(
				(skill, playerpatch, amount) -> true,
				(skill, playerpatch, amount) -> {}
		),

		WEAPON_CHARGE(
				(skill, playerpatch, amount) -> playerpatch.getSkill(skill).getStack() >= amount,
				(skill, playerpatch, amount) -> {
					skill.setStackSynchronize(playerpatch, playerpatch.getSkill(skill).getStack() - 1);
				}
		),

		COOLDOWN(
				(skill, playerpatch, amount) -> playerpatch.getSkill(skill).getStack() >= amount,
				(skill, playerpatch, amount) -> {
					skill.setStackSynchronize(playerpatch, playerpatch.getSkill(skill).getStack() - 1);
				}
		),

		STAMINA(
				(skill, playerpatch, amount) -> playerpatch.hasStamina(amount),
				(skill, playerpatch, amount) -> {
					playerpatch.resetActionTick();
					playerpatch.setStamina(playerpatch.getStamina() - amount);
				}
		),

		HEALTH(
				(skill, playerpatch, amount) -> playerpatch.getOriginal().getHealth() > amount,
				(skill, playerpatch, amount) -> {
					playerpatch.getOriginal().setHealth(playerpatch.getOriginal().getHealth() - amount);
				}
		);

		public final ResourcePredicate predicate;
		public final ResourceConsumer consumer;

		Resource(ResourcePredicate predicate, ResourceConsumer consumer) {
			this.predicate = predicate;
			this.consumer = consumer;
		}

		@FunctionalInterface
		public interface ResourcePredicate {
			boolean canExecute(Skill skill, PlayerPatch<?> playerpatch, float amount);
		}

		@FunctionalInterface
		public interface ResourceConsumer {
			void consume(Skill skill, ServerPlayerPatch playerpatch, float amount);
		}
	}
}