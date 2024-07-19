package yesman.epicfight.api.data.reloader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ibm.icu.impl.locale.XCldrStub.ImmutableSet;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.api.utils.RegistryUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSyncSkill;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SkillManager extends JsonReloadListener {

	static {
		RegistryUtils.createRegistrry(new ResourceLocation(EpicFightMod.MODID, "skill"), Skill.class);
	}

	public static final IForgeRegistry<Skill> REGISTRY = RegistryManager.ACTIVE.getRegistry(Skill.class);;
	public static final DeferredRegister<Skill> SKILLS = DeferredRegister.create(REGISTRY, EpicFightMod.MODID);
	
	
	private static final List<CompoundNBT> SKILL_PARAMS = Lists.newArrayList();
	private static final Gson GSON = (new GsonBuilder()).create();
	private static Set<String> namespaces;

	public static List<CompoundNBT> getSkillParams() {
		return Collections.unmodifiableList(SKILL_PARAMS);
	}

	public static void registerSkills(RegistryEvent.Register<Skill> event) {
			final SkillBuildEvent skillBuildEvnet = new SkillBuildEvent(); //Collect all skills from side mods before registering them
			ModLoader.get().postEvent(skillBuildEvnet);

			namespaces = ImmutableSet.copyOf(skillBuildEvnet.getNamespaces());

			skillBuildEvnet.getAllSkills().forEach((skill) -> {
				skill.setRegistryName(skill.getSkillRegistryName());
				event.getRegistry().register(skill);
			});
	}

	public static Skill getSkill(String name) {
		IForgeRegistry<Skill> skillRegistry = getSkillRegistry();
		ResourceLocation rl;

		if (name.indexOf(':') >= 0) {
			rl = new ResourceLocation(name);
		} else {
			rl = new ResourceLocation(EpicFightMod.MODID, name);
		}

		if (skillRegistry.containsKey(rl)) {
			return skillRegistry.getValue(rl);
		} else {
			return null;
		}
	}

	public static Collection<Skill> getSkills(Predicate<Skill> predicate) {
		return getSkillRegistry().getValues().stream().filter(skill -> predicate.test(skill)).toList();
	}

	public static Stream<ResourceLocation> getSkillNames(Predicate<Skill> predicate) {
		return getSkillRegistry().getValues().stream().filter(skill -> predicate.test(skill)).map(skill -> skill.getSkillRegistryName());
	}

	public static Set<String> getNamespaces() {
		return namespaces;
	}

	public static void reloadAllSkillsAnimations() {
		IForgeRegistry<Skill> skillRegistry = getSkillRegistry();
		skillRegistry.getValues().forEach((skill) -> skill.registerPropertiesToAnimation());
	}

	public static IForgeRegistry<Skill> getSkillRegistry() {
		return REGISTRY;
	}

	@OnlyIn(Dist.CLIENT)
	public static void processServerPacket(SPDatapackSyncSkill packet) {
		IForgeRegistry<Skill> skillRegistry = getSkillRegistry();

		for (CompoundNBT tag : packet.getTags()) {
			if (!skillRegistry.containsKey(new ResourceLocation(tag.getString("id")))) {
				EpicFightMod.LOGGER.warn("Failed to syncronize Datapack for skill: " + tag.getString("id"));
				continue;
			}

			skillRegistry.getValue(new ResourceLocation(tag.getString("id"))).setParams(tag);
		}

		LocalPlayerPatch localplayerpatch = ClientEngine.getInstance().getPlayerPatch();

		if (localplayerpatch != null) {
			CapabilitySkill skillCapability = localplayerpatch.getSkillCapability();

			for (String skillName : packet.getLearnedSkills()) {
				skillCapability.addLearnedSkill(getSkill(skillName));
			}

			for (SkillContainer skill : skillCapability.skillContainers) {
				if (skill.getSkill() != null) {
					// Reload skill
					skill.setSkill(getSkill(skill.getSkill().toString()), true);
				}
			}

			skillCapability.skillContainers[SkillCategories.BASIC_ATTACK.universalOrdinal()].setSkill(EpicFightSkills.BASIC_ATTACK);
			skillCapability.skillContainers[SkillCategories.AIR_ATTACK.universalOrdinal()].setSkill(EpicFightSkills.AIR_ATTACK);
			skillCapability.skillContainers[SkillCategories.KNOCKDOWN_WAKEUP.universalOrdinal()].setSkill(EpicFightSkills.KNOCKDOWN_WAKEUP);
		}
	}

	private static Pair<ResourceLocation, CompoundNBT> parseParameters(Map.Entry<ResourceLocation, JsonElement> entry) {
		try {
			CompoundNBT tag = JsonToNBT.parseTag(entry.getValue().toString());
			tag.putString("id", entry.getKey().toString());
			SKILL_PARAMS.add(tag);

			return Pair.of(entry.getKey(), tag);
		} catch (CommandSyntaxException e) {
			EpicFightMod.LOGGER.warn("Can't parse skill parameter for " + entry.getKey() + " because of " + e.getMessage());
			e.printStackTrace();

			return Pair.of(entry.getKey(), new CompoundNBT());
		}
	}

	private static final SkillManager INSTANCE = new SkillManager();

	public static SkillManager getInstance() {
		return INSTANCE;
	}

	public SkillManager() {
		super(GSON, "skill_parameters");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManager, IProfiler profileFiller) {
		IForgeRegistry<Skill> skillRegistry = getSkillRegistry();
		SKILL_PARAMS.clear();

		objectIn.entrySet().stream().filter((entry) -> {
			if (!skillRegistry.containsKey(entry.getKey())) {
				EpicFightMod.LOGGER.warn("Skill " + entry.getKey() + " doesn't exist in the registry.");
				return false;
			}

			return true;
		}).map(SkillManager::parseParameters).forEach((pair) -> skillRegistry.getValue(pair.getFirst()).setParams(pair.getSecond()));
	}

	private static class SkillRegistryCallbacks implements IForgeRegistry.BakeCallback<Skill>, IForgeRegistry.CreateCallback<Skill>, IForgeRegistry.ClearCallback<Skill> {
		private static final ResourceLocation LEARNABLE_SKILLS = new ResourceLocation(EpicFightMod.MODID, "learnableskills");
		private static final SkillRegistryCallbacks INSTANCE = new SkillRegistryCallbacks();

		@Override
		@SuppressWarnings("unchecked")
		public void onBake(IForgeRegistryInternal<Skill> owner, RegistryManager stage) {
			final Map<ResourceLocation, Skill> learnableSkills = owner.getSlaveMap(LEARNABLE_SKILLS, Map.class);
			owner.getEntries().stream().filter((entry) -> entry.getValue().getCategory().learnable()).forEach((entry) -> learnableSkills.put(entry.getKey().location(), entry.getValue()));
		}

		@Override
		public void onCreate(IForgeRegistryInternal<Skill> owner, RegistryManager stage) {
			owner.setSlaveMap(LEARNABLE_SKILLS, Maps.newHashMap());
		}

		@Override
		public void onClear(IForgeRegistryInternal<Skill> owner, RegistryManager stage) {
			owner.getSlaveMap(LEARNABLE_SKILLS, Map.class).clear();
		}
	}
}