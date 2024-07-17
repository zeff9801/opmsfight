package yesman.epicfight.events;

import com.google.common.collect.Multimap;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionExpiryEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPPlayAnimation;
import yesman.epicfight.network.server.SPPotion;
import yesman.epicfight.network.server.SPPotion.Action;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.projectile.ProjectilePatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.eventlistener.DealtDamageEvent;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.ProjectileHitEvent;
import yesman.epicfight.world.gamerule.EpicFightGamerules;

@Mod.EventBusSubscriber(modid=EpicFightMod.MODID)
public class EntityEvents {
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void spawnEvent(EntityJoinWorldEvent event) {
		EntityPatch<Entity> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), EntityPatch.class);

		if (entitypatch != null && !entitypatch.isInitialized()) {
			entitypatch.onJoinWorld(event.getEntity(), event);
		}
	}

	@SubscribeEvent
	public static void updateEvent(LivingUpdateEvent event) {
		EntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), EntityPatch.class);

		if (entitypatch != null && entitypatch.getOriginal() != null) {
			entitypatch.tick(event);
		}
	}

	@SubscribeEvent
	public static void deathEvent(LivingDeathEvent event) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);

		if (entitypatch != null) {
			entitypatch.onDeath(event);
		}

		//Chicken explosion code
//		if (event.getEntity() instanceof ChickenEntity) {
//			Vector3d pos = event.getEntity().position();
//
//			for (int i = -1; i <= 1; i += 2) {
//				for (int j = -1; j <= 1; j += 2) {
//					for (int k = 0; k < 8; k++) {
//						float power = 0.4F;
//						float powerX = event.getEntityLiving().getRandom().nextFloat() * power;
//						float powerY = (event.getEntityLiving().getRandom().nextFloat() + 0.5F) * power;
//						float powerZ = event.getEntityLiving().getRandom().nextFloat() * power;
//
//						event.getEntity().level.addParticle(EpicFightParticles.FEATHER.get(), pos.x, pos.y, pos.z
//								, i * powerX, powerY, j * powerZ);
//					}
//				}
//			}
//		}
	}

	@SubscribeEvent
	public static void knockBackEvent(LivingKnockBackEvent event) {
		HurtableEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), HurtableEntityPatch.class);

		if (entitypatch != null && entitypatch.shouldCancelKnockback()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void hurtEvent(LivingHurtEvent event) {
		EpicFightDamageSource epicFightDamageSource = null;
		Entity trueSource = event.getSource().getEntity();

		if (trueSource != null) {
			LivingEntityPatch<?> attackerEntityPatch = EpicFightCapabilities.getEntityPatch(trueSource, LivingEntityPatch.class);
			float baseDamage = event.getAmount();


			if (event.getSource() instanceof EpicFightDamageSource  instance) {
				epicFightDamageSource = instance;
			} else if (event.getSource() instanceof IndirectEntityDamageSource && event.getSource().getDirectEntity() != null) {
				ProjectilePatch<?> projectileCap = event.getSource().getDirectEntity().getCapability(EpicFightCapabilities.CAPABILITY_PROJECTILE, null).orElse(null);

				if (projectileCap != null) {
					epicFightDamageSource = projectileCap.getEpicFightDamageSource(event.getSource());
				}
			} else if (attackerEntityPatch != null) {
				epicFightDamageSource = attackerEntityPatch.getEpicFightDamageSource();
				baseDamage = attackerEntityPatch.getModifiedBaseDamage(baseDamage);
			}

			if (epicFightDamageSource != null) {
				LivingEntity hitEntity = event.getEntityLiving();



				if (attackerEntityPatch instanceof ServerPlayerPatch playerpatch) {
					DealtDamageEvent.Hurt dealDamageHurt = new DealtDamageEvent.Hurt(playerpatch, hitEntity, epicFightDamageSource, event);
					playerpatch.getEventListener().triggerEvents(EventType.DEALT_DAMAGE_EVENT_HURT, dealDamageHurt);
				}

				float totalDamage = epicFightDamageSource.getDamageModifier().getTotalValue(baseDamage);

				if (trueSource instanceof LivingEntity livingEntity && epicFightDamageSource.getExtraDamages() != null) {
					for (ExtraDamageInstance extraDamage : epicFightDamageSource.getExtraDamages()) {
						totalDamage += extraDamage.get(livingEntity, epicFightDamageSource.getHurtItem(), hitEntity, baseDamage);
					}
				}

				HurtableEntityPatch<?> hitHurtableEntityPatch = EpicFightCapabilities.getEntityPatch(hitEntity, HurtableEntityPatch.class);
				LivingEntityPatch<?> hitLivingEntityPatch = EpicFightCapabilities.getEntityPatch(hitEntity, LivingEntityPatch.class);
				ServerPlayerPatch hitPlayerPatch = EpicFightCapabilities.getEntityPatch(hitEntity, ServerPlayerPatch.class);

				if (hitPlayerPatch != null) {
					HurtEvent.Post hurtEvent = new HurtEvent.Post(hitPlayerPatch, epicFightDamageSource, totalDamage);
					hitPlayerPatch.getEventListener().triggerEvents(EventType.HURT_EVENT_POST, hurtEvent);
					totalDamage = hurtEvent.getAmount();
				}

				event.setAmount(totalDamage);

//				TODO Once we implement execution skills
//				if (epicFightDamageSource.is(EpicFightDamageSources.TYPE.EXECUTION)) {
//					float amount = event.getAmount();
//					event.setAmount(2147483647F);
//
//					if (hitLivingEntityPatch != null) {
//						int executionResistance = hitLivingEntityPatch.getExecutionResistance();
//
//						if (executionResistance > 0) {
//
////							hitLivingEntityPatch.setExecutionResistance(executionResistance - 1);
//							event.setAmount(amount);
//						}
//					}
//				}

				if (event.getAmount() > 0.0F) {
					if (hitHurtableEntityPatch != null) {
						StunType stunType = epicFightDamageSource.getStunType();
						float stunTime = 0.0F;
						float knockBackAmount = 0.0F;
						float stunShield = hitHurtableEntityPatch.getStunShield();

						if (stunShield > epicFightDamageSource.getImpact()) {
							if (stunType == StunType.SHORT || stunType == StunType.LONG) {
								stunType = StunType.NONE;
							}
						}

						hitHurtableEntityPatch.setStunShield(stunShield - epicFightDamageSource.getImpact());

						switch (stunType) {
							case SHORT:
								// Solution by Cyber2049(github): Fix stun immunity
								stunType = StunType.NONE;

								if (!hitEntity.hasEffect(EpicFightMobEffects.STUN_IMMUNITY.get()) && (hitHurtableEntityPatch.getStunShield() == 0.0F)) {
									float totalStunTime = (0.25F + epicFightDamageSource.getImpact() * 0.1F) * (1.0F - hitHurtableEntityPatch.getStunReduction());

									if (totalStunTime >= 0.075F) {
										stunTime = totalStunTime - 0.1F;
										boolean isLongStun = totalStunTime >= 0.83F;
										stunTime = isLongStun ? 0.83F : stunTime;
										stunType = isLongStun ? StunType.LONG : StunType.SHORT;
										knockBackAmount = Math.min(isLongStun ? epicFightDamageSource.getImpact() * 0.05F : totalStunTime, 2.0F);
									}

									stunTime *= (float) (1.0F - hitEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
								}
								break;
							case LONG:
								stunType = hitEntity.hasEffect(EpicFightMobEffects.STUN_IMMUNITY.get()) ? StunType.NONE : StunType.LONG;
								knockBackAmount = Math.min(epicFightDamageSource.getImpact() * 0.05F, 5.0F);
								stunTime = 0.83F;
								break;
							case HOLD:
								stunType = StunType.SHORT;
								stunTime = epicFightDamageSource.getImpact() * 0.25F;
								break;
							case KNOCKDOWN:
								stunType = hitEntity.hasEffect(EpicFightMobEffects.STUN_IMMUNITY.get()) ? StunType.NONE : StunType.KNOCKDOWN;
								knockBackAmount = Math.min(epicFightDamageSource.getImpact() * 0.05F, 5.0F);
								stunTime = 2.0F;
								break;
							case NEUTRALIZE:
								stunType = StunType.NEUTRALIZE;
								hitHurtableEntityPatch.playSound(EpicFightSounds.NEUTRALIZE_MOBS, 3.0F, 0.0F, 0.1F);
								EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument(((ServerWorld)hitEntity.level), hitEntity, event.getSource().getDirectEntity());
								knockBackAmount = 0.0F;
								stunTime = 2.0F;
							default:
								break;
						}

						Vector3d sourcePosition = epicFightDamageSource.getInitialPosition();
						hitHurtableEntityPatch.setStunReductionOnHit(stunType);
						boolean stunApplied = hitHurtableEntityPatch.applyStun(stunType, stunTime);

						if (sourcePosition != null) {
							if (!(hitEntity instanceof PlayerEntity) && stunApplied) {
								hitEntity.lookAt(EntityAnchorArgument.Type.FEET, sourcePosition);
							}

							if (knockBackAmount > 0.0F) {
								knockBackAmount *= 40.0F / hitHurtableEntityPatch.getWeight();

								hitHurtableEntityPatch.knockBackEntity(sourcePosition, knockBackAmount);
							}
						}
					}
				}
			}
		} else {
			if (event.getSource() == DamageSource.FALL && event.getAmount() > 1.0F && event.getEntity().level.getGameRules().getBoolean(EpicFightGamerules.HAS_FALL_ANIMATION)) {
				LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);

				if (entitypatch != null && !entitypatch.getEntityState().inaction()) {
					StaticAnimation fallAnimation = entitypatch.getAnimator().getLivingAnimation(LivingMotions.LANDING_RECOVERY, entitypatch.getHitAnimation(StunType.FALL));

					if (fallAnimation != null) {
						entitypatch.playAnimationSynchronized(fallAnimation, 0);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void damageEvent(LivingDamageEvent event) {
		Entity attacker = event.getSource().getEntity();

		if (attacker != null) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(attacker, ServerPlayerPatch.class);
			EpicFightDamageSource epicFightDamageSource = null;

			if (event.getSource() instanceof EpicFightDamageSource instance) {
				epicFightDamageSource = instance;
			} else if (event.getSource() instanceof IndirectEntityDamageSource && event.getSource().getDirectEntity() != null) {
				ProjectilePatch<?> projectileCap = event.getSource().getDirectEntity().getCapability(EpicFightCapabilities.CAPABILITY_PROJECTILE, null).orElse(null);

				if (projectileCap != null) {
					epicFightDamageSource = projectileCap.getEpicFightDamageSource(event.getSource());
				}
			} else if (playerpatch != null) {
				epicFightDamageSource = playerpatch.getEpicFightDamageSource();
			}

			if (playerpatch != null && epicFightDamageSource != null) {
				playerpatch.getEventListener().triggerEvents(EventType.DEALT_DAMAGE_EVENT_DAMAGE, new DealtDamageEvent.Damage(playerpatch, event.getEntityLiving(), epicFightDamageSource, event));
			}
		}
	}

	@SubscribeEvent
	public static void attackEvent(LivingAttackEvent event) {
		if (event.getEntity().level.isClientSide()) {
			return;
		}

		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);
		DamageSource damageSource = null;

		if (event.getEntityLiving().getHealth() > 0.0F) {
			LivingEntityPatch<?> attackerPatch = EpicFightCapabilities.getEntityPatch(event.getSource().getEntity(), LivingEntityPatch.class);

			if (event.getSource() instanceof EpicFightDamageSource efDamageSource) {
				damageSource = efDamageSource;
			} else if (event.getSource() instanceof IndirectEntityDamageSource && event.getSource().getDirectEntity() != null) {
				ProjectilePatch<?> projectilepatch = event.getSource().getDirectEntity().getCapability(EpicFightCapabilities.CAPABILITY_PROJECTILE).orElse(null);

				if (projectilepatch != null) {
					damageSource = projectilepatch.getEpicFightDamageSource(event.getSource());
				}
			} else if (attackerPatch != null && attackerPatch.getEpicFightDamageSource() != null) {
				damageSource = attackerPatch.getEpicFightDamageSource();
			}

			if (damageSource == null) {
				damageSource = event.getSource();
			}

			if (damageSource instanceof EpicFightDamageSource efDamageSource && event.getSource().getEntity() instanceof ServerPlayerEntity serverplayer) {
				ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(serverplayer, ServerPlayerPatch.class);
				DealtDamageEvent.Attack dealDamageAttack = new DealtDamageEvent.Attack(playerpatch, event.getEntityLiving(), efDamageSource, event);
				playerpatch.getEventListener().triggerEvents(EventType.DEALT_DAMAGE_EVENT_ATTACK, dealDamageAttack);

				if (dealDamageAttack.isCanceled()) {
					event.setCanceled(true);
					return;
				}
			}

			AttackResult result = entitypatch != null ? entitypatch.tryHurt(damageSource, event.getAmount()) : AttackResult.success(event.getAmount());

			if (attackerPatch != null) {
				attackerPatch.setLastAttackResult(result);
			}

			if (!result.resultType.dealtDamage()) {
				event.setCanceled(true);
			} else if (event.getAmount() != result.damage) {
				EpicFightDamageSource deflictedDamage = EpicFightDamageSources.copy(damageSource);

				event.setCanceled(true);
				event.getEntity().hurt(deflictedDamage, result.damage);
			}
		}
	}

	//TODO ShieldBlockEvent doesn't exist in 1.16.5, we can make our own one though
//	@SubscribeEvent
//	public static void shieldEvent(ShieldBlockEvent event) {
//		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);
//
//		if (entitypatch != null) {
//			entitypatch.playAnimationSynchronized(Animations.BIPED_HIT_SHIELD, 0.0F);
//		}
//	}

	@SubscribeEvent
	public static void dropEvent(LivingDropsEvent event) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);

		if (entitypatch != null) {
			if (entitypatch.onDrop(event)) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void projectileImpactEvent(ProjectileImpactEvent event) {
		ProjectilePatch<?> projectilepatch = event.getEntity().getCapability(EpicFightCapabilities.CAPABILITY_PROJECTILE, null).orElse(null);

		if (!event.getEntity().level.isClientSide() && projectilepatch != null) {
			if (projectilepatch.onProjectileImpact(event)) {
				event.setCanceled(true);
				return;
			}
		}

		if (event.getRayTraceResult() instanceof EntityRayTraceResult rayresult) {
			if (rayresult.getEntity() != null) {
				if (rayresult.getEntity() instanceof ServerPlayerEntity) {
					ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(rayresult.getEntity(), ServerPlayerPatch.class);
					boolean canceled = playerpatch.getEventListener().triggerEvents(EventType.PROJECTILE_HIT_EVENT, new ProjectileHitEvent(playerpatch, event));

					if (canceled) {
						event.setCanceled(true);
					}
				}

				if (event.getEntity() instanceof ProjectileEntity projectileEntity) {
					if (projectileEntity.getOwner() != null) {
						if (rayresult.getEntity().equals(projectileEntity.getOwner().getVehicle())) {
							event.setCanceled(true);
						}

						if (rayresult.getEntity() instanceof PartEntity<?> partEntity) {
							Entity parent = partEntity.getParent();

							if (projectileEntity.getOwner().is(parent)) {
								event.setCanceled(true);
							}
						}
					}
				}

			}
		}
	}

	@SubscribeEvent
	public static void itemAttributeModifierEvent(ItemAttributeModifierEvent event) {
		CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(event.getItemStack());

		if (!itemCap.isEmpty()) {
			Multimap<Attribute, AttributeModifier> multimap = itemCap.getAttributeModifiers(event.getSlotType(), null);

			for (Attribute key : multimap.keys()) {
				for (AttributeModifier modifier : multimap.get(key)) {
					event.addModifier(key, modifier);
				}
			}
		}
	}

	@SubscribeEvent
	public static void equipChangeEvent(LivingEquipmentChangeEvent event) {
		HurtableEntityPatch<?> hurtableEntitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), HurtableEntityPatch.class);

		if (hurtableEntitypatch != null) {
			hurtableEntitypatch.setDefaultStunReduction(event.getSlot(), event.getFrom(), event.getTo());
		}

		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);
		CapabilityItem fromCap = EpicFightCapabilities.getItemStackCapability(event.getFrom());
		CapabilityItem toCap = EpicFightCapabilities.getItemStackCapability(event.getTo());

		if (event.getSlot() != EquipmentSlotType.OFFHAND) {
			if (fromCap != null) {
				event.getEntityLiving().getAttributes().removeAttributeModifiers(fromCap.getAttributeModifiers(event.getSlot(), entitypatch));
			}

			if (toCap != null) {
				event.getEntityLiving().getAttributes().addTransientAttributeModifiers(toCap.getAttributeModifiers(event.getSlot(), entitypatch));
			}
		}

		if (entitypatch != null && entitypatch.getOriginal() != null) {
			if (event.getSlot().getType() == EquipmentSlotType.Group.HAND) {
				Hand hand = event.getSlot() == EquipmentSlotType.MAINHAND ? Hand.MAIN_HAND : Hand.OFF_HAND;
				entitypatch.updateHeldItem(fromCap, toCap, event.getFrom(), event.getTo(), hand);
			} else if (event.getSlot().getType() == EquipmentSlotType.Group.ARMOR) {
				entitypatch.updateArmor(fromCap, toCap, event.getSlot());
			}
		}

	}

	@SubscribeEvent
	public static void sizingEvent(EntityEvent.Size event) {
		if (event.getEntity() instanceof EnderDragonEntity) {
			event.setNewSize(EntitySize.scalable(5.0F, 3.0F));
		}
	}


	@SubscribeEvent
	public static void effectAddEvent(PotionAddedEvent event) {
		if (!event.getEntity().level.isClientSide()) {
			EpicFightNetworkManager.sendToAll(new SPPotion(event.getPotionEffect().getEffect(), Action.ACTIVATE, event.getEntity().getId()));
		}
	}

	@SubscribeEvent
	public static void effectRemoveEvent(PotionRemoveEvent event) {
		if (!event.getEntity().level.isClientSide() && event.getPotionEffect() != null) {
			EpicFightNetworkManager.sendToAll(new SPPotion(event.getPotionEffect().getEffect(), Action.REMOVE, event.getEntity().getId()));
		}
	}

	@SubscribeEvent
	public static void effectExpiryEvent(PotionExpiryEvent event) {
		if (!event.getEntity().level.isClientSide()) {
			EpicFightNetworkManager.sendToAll(new SPPotion(event.getPotionEffect().getEffect(), Action.REMOVE, event.getEntity().getId()));
		}
	}

	@SubscribeEvent
	public static void mountEvent(EntityMountEvent event) {
		EntityPatch<?> mountEntity = EpicFightCapabilities.getEntityPatch(event.getEntityMounting(), EntityPatch.class);

		if (!event.getWorldObj().isClientSide() && mountEntity instanceof HumanoidMobPatch && mountEntity.getOriginal() != null) {
			if (event.getEntityBeingMounted() instanceof MobEntity) {
				((HumanoidMobPatch<?>)mountEntity).onMount(event.isMounting(), event.getEntityBeingMounted());
			}
		}
	}


	@SubscribeEvent
	public static void jumpEvent(LivingJumpEvent event) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);

		if (entitypatch != null && entitypatch.isLogicalClient()) {
			if (!entitypatch.getEntityState().inaction() && !event.getEntity().isInWater()) {
				StaticAnimation jumpAnimation = entitypatch.getClientAnimator().getJumpAnimation();
				entitypatch.getAnimator().playAnimation(jumpAnimation, 0);
				EpicFightNetworkManager.sendToServer(new CPPlayAnimation(jumpAnimation.getId(), 0, true, false));
			}
		}
	}

	@SubscribeEvent
	public static void fallEvent(LivingFallEvent event) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);

		if (entitypatch != null) {
			entitypatch.onFall(event);
		}
	}

	@SubscribeEvent
	public static void playerFallEvent(PlayerFlyableFallEvent event) {
		PlayerPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), PlayerPatch.class);

		if (entitypatch != null) {
			entitypatch.onFall(new LivingFallEvent(event.getEntityLiving(), event.getDistance(), event.getMultiplier()));
		}
	}



}