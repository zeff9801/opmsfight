package yesman.epicfight.client.input;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public class EpicFightKeyMappings {
	public static final KeyBinding SPECIAL_SKILL_TOOLTIP = new KeyBinding("key." + EpicFightMod.MODID + ".show_tooltip", 80, "key." + EpicFightMod.MODID + ".gui");
	public static final KeyBinding SWITCH_MODE = new KeyBinding("key." + EpicFightMod.MODID + ".switch_mode", 82, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyBinding DODGE = new KeyBinding("key." + EpicFightMod.MODID + ".dodge", 342, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyBinding SPECIAL_SKILL = new SpecialAttackKeyMapping("key." + EpicFightMod.MODID + ".weapon_special_skill", InputMappings.Type.MOUSE, 0, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyBinding SKILL_EDIT = new KeyBinding("key." + EpicFightMod.MODID + ".skill_gui", 75, "key." + EpicFightMod.MODID + ".gui");
	public static final KeyBinding LOCK_ON = new KeyBinding("key." + EpicFightMod.MODID + ".lock_on", GLFW.GLFW_KEY_G, "key." + EpicFightMod.MODID + ".combat");

    public static void registerKeys() {
		ClientRegistry.registerKeyBinding(SPECIAL_SKILL_TOOLTIP);
		ClientRegistry.registerKeyBinding(SWITCH_MODE);
		ClientRegistry.registerKeyBinding(DODGE);
		ClientRegistry.registerKeyBinding(SPECIAL_SKILL);
		ClientRegistry.registerKeyBinding(SKILL_EDIT);
	}
}