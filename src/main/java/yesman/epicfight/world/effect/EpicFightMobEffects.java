package yesman.epicfight.world.effect;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class EpicFightMobEffects {
	public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, EpicFightMod.MODID);

	public static final RegistryObject<Effect> STUN_IMMUNITY = EFFECTS.register("stun_immunity", () ->
			new VisibleMobEffect(EffectType.BENEFICIAL, 16758016, new ResourceLocation(EpicFightMod.MODID, "textures/mob_effect/stun_immunity.png")));

	public static final RegistryObject<Effect> BLOOMING = EFFECTS.register("blooming", () ->
			new VisibleMobEffect(EffectType.BENEFICIAL, 16735744, new ResourceLocation(EpicFightMod.MODID, "textures/mob_effect/blooming.png")));

	public static final RegistryObject<Effect> INSTABILITY = EFFECTS.register("instability", () ->
			new VisibleMobEffect(EffectType.HARMFUL, 0, (effectInstance) -> Math.min(effectInstance.getAmplifier(), 2)
					, new ResourceLocation(EpicFightMod.MODID, "textures/mob_effect/instability1.png")
					, new ResourceLocation(EpicFightMod.MODID, "textures/mob_effect/instability2.png")
					, new ResourceLocation(EpicFightMod.MODID, "textures/mob_effect/instability3.png")));

	public static void addOffhandModifier() {
		Effects.DIG_SPEED.addAttributeModifier(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get(), "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", 0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL);
		Effects.DIG_SLOWDOWN.addAttributeModifier(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get(), "55FCED67-E92A-486E-9800-B47F202C4386", -0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
}