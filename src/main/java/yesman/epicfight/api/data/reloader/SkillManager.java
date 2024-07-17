package yesman.epicfight.api.data.reloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class SkillManager extends JsonReloadListener {
	public SkillManager(Gson p_i51536_1_, String p_i51536_2_) {
		super(p_i51536_1_, p_i51536_2_);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, IResourceManager iResourceManager, IProfiler iProfiler) {

	}
	/*public static final ResourceKey<Registry<Skill>> SKILL_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(EpicFightMod.MODID, "skill"));
	private static final List<CompoundNBT> SKILL_PARAMS = Lists.newArrayList();
	private static final Gson GSON = (new GsonBuilder()).create();
	private static Set<String> namespaces;
	
	public static List<CompoundNBT> getSkillParams() {
		return Collections.unmodifiableList(SKILL_PARAMS);
	}
	
	public static void createSkillRegistry(NewRegistryEvent event) {
		event.create(RegistryBuilder.of(new ResourceLocation(EpicFightMod.MODID, "skill")).addCallback(SkillRegistryCallbacks.INSTANCE));
	}
	
	public static void registerSkills(RegisterEvent event) {
		if (event.getRegistryKey().equals(SKILL_REGISTRY_KEY)) {
			final SkillBuildEvent skillBuildEvnet = new SkillBuildEvent();
			ModLoader.get().postEvent(skillBuildEvnet);
			
			namespaces = ImmutableSet.copyOf(skillBuildEvnet.getNamespaces());
			
			event.register(SKILL_REGISTRY_KEY, (helper) -> {
				skillBuildEvnet.getAllSkills().forEach((skill) -> {
					helper.register(skill.getRegistryName(), skill);
				});
			});
		}
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
		return getSkillRegistry().getValues().stream().filter(skill -> predicate.test(skill)).map(skill -> skill.getRegistryName());
	}
	
	public static Set<String> getNamespaces() {
		return namespaces;
	}
	
	public static void reloadAllSkillsAnimations() {
		IForgeRegistry<Skill> skillRegistry = getSkillRegistry();
		skillRegistry.getValues().forEach((skill) -> skill.registerPropertiesToAnimation());
	}
	
	public static IForgeRegistry<Skill> getSkillRegistry() {
		return RegistryManager.ACTIVE.getRegistry(SKILL_REGISTRY_KEY);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void processServerPacket(SPDatapackSyncSkilk packet) {
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
			CompoundNBT tag = TagParser.parseTag(entry.getValue().toString());
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
	}*/
}