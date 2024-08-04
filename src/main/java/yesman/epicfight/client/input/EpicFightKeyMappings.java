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
	public static final KeyBinding SWITCH_MODE = new KeyBinding("key." + EpicFightMod.MODID + ".switch_mode", GLFW.GLFW_KEY_R, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyBinding DODGE = new KeyBinding("key." + EpicFightMod.MODID + ".dodge", GLFW.GLFW_KEY_LEFT_ALT, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyBinding GUARD = new KeyBinding("key." + EpicFightMod.MODID + ".guard", GLFW.GLFW_MOUSE_BUTTON_RIGHT, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyBinding MOVER_SKILL = new  CombatKeyMapping("key." + EpicFightMod.MODID + ".mover_skill", InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyBinding LOCK_ON = new KeyBinding("key." + EpicFightMod.MODID + ".lock_on", GLFW.GLFW_KEY_G, "key." + EpicFightMod.MODID + ".combat");



    public static void registerKeys() {
		ClientRegistry.registerKeyBinding(SWITCH_MODE);
		ClientRegistry.registerKeyBinding(DODGE);
		ClientRegistry.registerKeyBinding(GUARD);
		ClientRegistry.registerKeyBinding(MOVER_SKILL);
		ClientRegistry.registerKeyBinding(LOCK_ON);
	}
}