package yesman.epicfight.client.events.engine;

import com.google.common.collect.Sets;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.Hand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.gui.screen.IngameConfigurationScreen;
import yesman.epicfight.client.gui.screen.SkillEditScreen;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.mixin.MixinMinecraftInvoker;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.skill.ChargeableSkill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.entity.eventlistener.MovementInputEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillExecuteEvent;
import yesman.epicfight.world.gamerule.EpicFightGamerules;

import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ControllEngine {
	private final Set<Object> packets = Sets.newHashSet();
	private final Minecraft minecraft;
	private ClientPlayerEntity player;
	private LocalPlayerPatch playerpatch;
	private int weaponInnatePressCounter = 0;
	private int sneakPressCounter = 0;
	private int moverPressCounter = 0;
	private int lastHotbarLockedTime;
	private boolean weaponInnatePressToggle = false;
	private boolean sneakPressToggle = false;
	private boolean moverPressToggle = false;
	private boolean attackLightPressToggle = false;
	private boolean hotbarLocked;
	private boolean chargeKeyUnpressed;
	private int reserveCounter;
	private KeyBinding reservedKey;
	private SkillSlot reservedOrChargingSkillSlot;
	private KeyBinding currentChargingKey;

	public GameSettings options;

	public ControllEngine() {
		Events.controllEngine = this;
		this.minecraft = Minecraft.getInstance();
		this.options = this.minecraft.options;
	}
	
	public void setPlayerPatch(LocalPlayerPatch playerpatch) {
		this.weaponInnatePressCounter = 0;
		this.weaponInnatePressToggle = false;
		this.sneakPressCounter = 0;
		this.sneakPressToggle = false;
		this.attackLightPressToggle = false;
		this.player = playerpatch.getOriginal();
		this.playerpatch = playerpatch;
	}

	public LocalPlayerPatch getPlayerPatch() {
		return this.playerpatch;
	}


	public boolean canPlayerMove(EntityState playerState) {
		return !playerState.movementLocked() || this.player.isRidingJumpable();
	}
	
	public boolean canPlayerRotate(EntityState playerState) {
		return !playerState.turningLocked() || this.player.isRidingJumpable();
	}


	public void handleEpicFightKeyMappings() {
		if (EpicFightKeyMappings.SKILL_EDIT.consumeClick()) {
			if (this.playerpatch.getSkillCapability() != null) {
				Minecraft.getInstance().setScreen(new SkillEditScreen(this.playerpatch.getSkillCapability()));
			}
		}

		if (EpicFightKeyMappings.CONFIG.consumeClick()) {
			Minecraft.getInstance().setScreen(new IngameConfigurationScreen(this.minecraft, null));
		}

		while (EpicFightKeyMappings.ATTACK.consumeClick()) {
			if (this.playerpatch.isBattleMode() && this.currentChargingKey != EpicFightKeyMappings.ATTACK) {
				if (!EpicFightKeyMappings.ATTACK.getKey().equals(EpicFightKeyMappings.WEAPON_INNATE_SKILL.getKey())) {
					SkillSlot slot = (!this.player.isOnGround() && !this.player.isInWater() && this.player.getDeltaMovement().y > 0.05D) ? SkillSlots.AIR_ATTACK : SkillSlots.BASIC_ATTACK;

					if (this.playerpatch.getSkill(slot).sendExecuteRequest(this.playerpatch, this).isExecutable()) {
						this.player.resetAttackStrengthTicker();
						this.attackLightPressToggle = false;
						this.releaseAllServedKeys();
					} else {
						if (!this.player.isSpectator() && slot == SkillSlots.BASIC_ATTACK) {
							this.reserveKey(slot, EpicFightKeyMappings.ATTACK);
						}
					}

					this.lockHotkeys();
					this.attackLightPressToggle = false;
					this.weaponInnatePressToggle = false;
					this.weaponInnatePressCounter = 0;
				} else {
					if (!this.weaponInnatePressToggle) {
						this.weaponInnatePressToggle = true;
					}
				}

				//Disable vanilla attack
				if (this.options.keyAttack.getKey() == EpicFightKeyMappings.ATTACK.getKey()) {
					this.disableKey(this.options.keyAttack);
				}
			}
		}

		while (EpicFightKeyMappings.DODGE.consumeClick()) {
			if (this.playerpatch.isBattleMode() && this.currentChargingKey != EpicFightKeyMappings.DODGE) {
				if (EpicFightKeyMappings.DODGE.getKey().getValue() == this.options.keyShift.getKey().getValue()) {
					if (this.player.getVehicle() == null) {
						if (!this.sneakPressToggle) {
							this.sneakPressToggle = true;
						}
					}
				} else {
					SkillSlot skillCategory = (this.playerpatch.getEntityState().knockDown()) ? SkillSlots.KNOCKDOWN_WAKEUP : SkillSlots.DODGE;
					SkillContainer skill = this.playerpatch.getSkill(skillCategory);

					if (skill.sendExecuteRequest(this.playerpatch, this).shouldReserverKey()) {
						this.reserveKey(SkillSlots.DODGE, EpicFightKeyMappings.DODGE);
					}
				}
			}
		}

		while (EpicFightKeyMappings.GUARD.consumeClick()) {
		}

		while (EpicFightKeyMappings.MOVER_SKILL.consumeClick()) {
			if (this.playerpatch.isBattleMode() && !this.playerpatch.isChargingSkill()) {
				if (EpicFightKeyMappings.MOVER_SKILL.getKey().getValue() == this.options.keyJump.getKey().getValue()) {
					SkillContainer skillContainer = this.playerpatch.getSkill(SkillSlots.MOVER);
					SkillExecuteEvent event = new SkillExecuteEvent(this.playerpatch, skillContainer);

					if (skillContainer.canExecute(playerpatch, event) && this.player.getVehicle() == null) {
						if (!this.moverPressToggle) {
							this.moverPressToggle = true;
						}
					}
				} else {
					SkillContainer skill = this.playerpatch.getSkill(SkillSlots.MOVER);
					skill.sendExecuteRequest(this.playerpatch, this);
				}
			}
		}

		while (EpicFightKeyMappings.MOVER_SKILL.consumeClick()) {
			if (this.playerpatch.isBattleMode() && !this.playerpatch.isChargingSkill()) {
				if (EpicFightKeyMappings.MOVER_SKILL.getKey().getValue() == this.options.keyJump.getKey().getValue()) {
					SkillContainer skillContainer = this.playerpatch.getSkill(SkillSlots.MOVER);
					SkillExecuteEvent event = new SkillExecuteEvent(this.playerpatch, skillContainer);

					if (skillContainer.canExecute(playerpatch, event) && this.player.getVehicle() == null) {
						if (!this.moverPressToggle) {
							this.moverPressToggle = true;
						}
					}
				} else {
					SkillContainer skill = this.playerpatch.getSkill(SkillSlots.MOVER);
					skill.sendExecuteRequest(this.playerpatch, this);
				}
			}
		}

		while (EpicFightKeyMappings.SWITCH_MODE.consumeClick()) {
			if (this.playerpatch.getOriginal().level.getGameRules().getBoolean(EpicFightGamerules.CAN_SWITCH_COMBAT)) {
				this.playerpatch.toggleMode();
			}
		}

		while (EpicFightKeyMappings.LOCK_ON.consumeClick()) {
			this.playerpatch.toggleLockOn();
		}

		//Disable swap hand items
		if (this.playerpatch.getEntityState().inaction() || (!this.playerpatch.getHoldingItemCapability(Hand.MAIN_HAND).canBePlacedOffhand())) {
			this.disableKey(this.minecraft.options.keySwapOffhand);
		}

		this.tick();
	}

	private void tick() {
		if (this.playerpatch == null || !this.playerpatch.isBattleMode() || Minecraft.getInstance().isPaused()) {
			return;
		}

		if (this.player.tickCount - this.lastHotbarLockedTime > 20 && this.hotbarLocked) {
			this.unlockHotkeys();
		}

		if (this.weaponInnatePressToggle) {
			if (!this.isKeyDown(EpicFightKeyMappings.WEAPON_INNATE_SKILL)) {
				this.attackLightPressToggle = true;
				this.weaponInnatePressToggle = false;
				this.weaponInnatePressCounter = 0;
			} else {
				if (EpicFightKeyMappings.WEAPON_INNATE_SKILL.getKey().equals(EpicFightKeyMappings.ATTACK.getKey())) {
					if (this.weaponInnatePressCounter > EpicFightMod.CLIENT_CONFIGS.longPressCount.getValue()) {
						if (this.playerpatch.getSkill(SkillSlots.WEAPON_INNATE).sendExecuteRequest(this.playerpatch, this).shouldReserverKey()) {
							if (!this.player.isSpectator()) {
								this.reserveKey(SkillSlots.WEAPON_INNATE, EpicFightKeyMappings.WEAPON_INNATE_SKILL);
							}
						} else {
							this.lockHotkeys();
						}

						this.weaponInnatePressToggle = false;
						this.weaponInnatePressCounter = 0;
					} else {
						this.weaponInnatePressCounter++;
					}
				}
			}
		}

		if (this.attackLightPressToggle) {
			SkillSlot slot = (!this.player.isOnGround() && !this.player.isInWater() && this.player.getDeltaMovement().y > 0.05D) ? SkillSlots.AIR_ATTACK : SkillSlots.BASIC_ATTACK;

			if (this.playerpatch.getSkill(slot).sendExecuteRequest(this.playerpatch, this).isExecutable()) {
				this.player.resetAttackStrengthTicker();
				this.releaseAllServedKeys();
			} else {
				if (!this.player.isSpectator() && slot == SkillSlots.BASIC_ATTACK) {
					this.reserveKey(slot, EpicFightKeyMappings.ATTACK);
				}
			}

			this.lockHotkeys();

			this.attackLightPressToggle = false;
			this.weaponInnatePressToggle = false;
			this.weaponInnatePressCounter = 0;
		}

		if (this.sneakPressToggle) {
			if (!this.isKeyDown(this.options.keyShift)) {
				SkillSlot skillSlot = (this.playerpatch.getEntityState().knockDown()) ? SkillSlots.KNOCKDOWN_WAKEUP : SkillSlots.DODGE;
				SkillContainer skill = this.playerpatch.getSkill(skillSlot);

				if (skill.sendExecuteRequest(this.playerpatch, this).shouldReserverKey()) {
					this.reserveKey(skillSlot, this.options.keyShift);
				}

				this.sneakPressToggle = false;
				this.sneakPressCounter = 0;
			} else {
				if (this.sneakPressCounter > EpicFightMod.CLIENT_CONFIGS.longPressCount.getValue()) {
					this.sneakPressToggle = false;
					this.sneakPressCounter = 0;
				} else {
					this.sneakPressCounter++;
				}
			}
		}

		if (this.currentChargingKey != null) {
			SkillContainer skill = this.playerpatch.getSkill(this.reservedOrChargingSkillSlot);

			if (skill.getSkill() instanceof ChargeableSkill chargingSkill) {
				if (!this.isKeyDown(this.currentChargingKey)) {
					this.chargeKeyUnpressed = true;
				}

				if (this.chargeKeyUnpressed) {
					if (this.playerpatch.getSkillChargingTicks() > chargingSkill.getMinChargingTicks()) {
						if (skill.getSkill() != null) {
							skill.sendExecuteRequest(this.playerpatch, this);
						}

						this.releaseAllServedKeys();
					}
				}

				if (this.playerpatch.getSkillChargingTicks() >= chargingSkill.getAllowedMaxChargingTicks()) {
					this.releaseAllServedKeys();
				}
			} else {
				this.releaseAllServedKeys();
			}
		}

		if (this.reservedKey != null) {
			if (this.reserveCounter > 0) {
				SkillContainer skill = this.playerpatch.getSkill(this.reservedOrChargingSkillSlot);
				this.reserveCounter--;

				if (skill.getSkill() != null) {
					if (skill.sendExecuteRequest(this.playerpatch, this).isExecutable()) {
						this.releaseAllServedKeys();
						this.lockHotkeys();
					}
				}
			} else {
				this.releaseAllServedKeys();
			}
		}

		if (this.playerpatch.getEntityState().inaction() || this.hotbarLocked) {
			for (int i = 0; i < 9; ++i) {
				while (this.options.keyHotbarSlots[i].consumeClick());
			}

			while (this.options.keyDrop.consumeClick());
		}
	}

	private void inputTick(MovementInput input) {
		if (this.moverPressToggle) {
			if (!this.isKeyDown(this.options.keyJump)) {
				this.moverPressToggle = false;
				this.moverPressCounter = 0;

				if (this.player.isOnGround()) {
					input.jumping = true;
				}
			} else {
				if (this.moverPressCounter > EpicFightMod.CLIENT_CONFIGS.longPressCount.getValue()) {
					SkillContainer skill = this.playerpatch.getSkill(SkillSlots.MOVER);
					skill.sendExecuteRequest(this.playerpatch, this);

					this.moverPressToggle = false;
					this.moverPressCounter = 0;
				} else {
					input.jumping = false;
					this.moverPressCounter++;
				}
			}
		}

		if (!this.canPlayerMove(this.playerpatch.getEntityState())) {
			input.forwardImpulse = 0F;
			input.leftImpulse = 0F;
			input.up = false;
			input.down = false;
			input.left = false;
			input.right = false;
			input.jumping = false;
			input.shiftKeyDown = false;
			this.player.sprintTriggerTime = -1;
			this.player.setSprinting(false);
		}

		if (this.player.isAlive()) {
			this.playerpatch.getEventListener().triggerEvents(EventType.MOVEMENT_INPUT_EVENT, new MovementInputEvent(this.playerpatch, input));
		}
	}

	private void reserveKey(SkillSlot slot, KeyBinding keyMapping) {
		this.reservedKey = keyMapping;
		this.reservedOrChargingSkillSlot = slot;
		this.reserveCounter = 8;
	}

	private void releaseAllServedKeys() {
		this.chargeKeyUnpressed = true;
		this.currentChargingKey = null;
		this.reservedOrChargingSkillSlot = null;
		this.reserveCounter = -1;
		this.reservedKey = null;
	}

	public void setChargingKey(SkillSlot chargingSkillSlot, KeyBinding keyMapping) {
		this.chargeKeyUnpressed = false;
		this.currentChargingKey = keyMapping;
		this.reservedOrChargingSkillSlot = chargingSkillSlot;
		this.reserveCounter = -1;
		this.reservedKey = null;
	}

	public boolean isKeyDown(KeyBinding key) {
		if (key.getKey().getType() == InputMappings.Type.KEYSYM) {
			return key.isDown() || GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), key.getKey().getValue()) > 0;
		} else if(key.getKey().getType() == InputMappings.Type.MOUSE) {
			return key.isDown() || GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), key.getKey().getValue()) > 0;
		} else {
			return false;
		}
	}

	public void disableKey(KeyBinding keyMapping) {
		while (keyMapping.consumeClick()) {}
		this.setKeyBind(keyMapping, false);
	}

	public void setKeyBind(KeyBinding key, boolean setter) {
		KeyBinding.set(key.getKey(), setter);
	}

	public void lockHotkeys() {
		this.hotbarLocked = true;
		this.lastHotbarLockedTime = this.player.tickCount;

		for (int i = 0; i < 9; ++i) {
			while (this.options.keyHotbarSlots[i].consumeClick());
		}
	}

	public void unlockHotkeys() {
		this.hotbarLocked = false;
	}

	public void addPacketToSend(Object packet) {
		this.packets.add(packet);
	}

	@OnlyIn(Dist.CLIENT)
	@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
	public static class Events {
		static ControllEngine controllEngine;

		@SubscribeEvent
		public static void mouseScrollEvent(InputEvent.MouseScrollEvent event) {
			if (controllEngine.minecraft.player != null && controllEngine.playerpatch != null && controllEngine.playerpatch.getEntityState().inaction()) {
				if (controllEngine.minecraft.screen == null) {
					event.setCanceled(true);
				}
			}
		}

		@SubscribeEvent
		public static void moveInputEvent(InputUpdateEvent  event) {
			if (controllEngine.playerpatch == null) {
				return;
			}

			controllEngine.inputTick(event.getMovementInput());
		}

		@SubscribeEvent
		public static void clientTickEndEvent(TickEvent.ClientTickEvent event) {
			if (controllEngine.minecraft.player == null) {
				return;
			}

			if (event.phase == TickEvent.Phase.END) {
				for (Object packet : controllEngine.packets) {
					EpicFightNetworkManager.sendToServer(packet);
				}

				controllEngine.packets.clear();
			}
		}
	}
}