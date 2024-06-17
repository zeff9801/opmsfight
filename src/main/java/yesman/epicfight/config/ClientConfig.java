package yesman.epicfight.config;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class ClientConfig {
	public final IntValue longPressCountConfig;
	public final BooleanValue showHealthIndicator;
	public final BooleanValue showTargetIndicator;
	public final BooleanValue filterAnimation;
	public final DoubleValue aimHelperColor;
	public final BooleanValue enableAimHelper;
	public final BooleanValue cameraAutoSwitch;
	public final BooleanValue autoPreparation;
	public final BooleanValue offBloodEffects;
	public final ConfigValue<List<? extends String>> battleAutoSwitchItems;
	public final ConfigValue<List<? extends String>> miningAutoSwitchItems;
	
	public ClientConfig(ForgeConfigSpec.Builder config) {
		this.longPressCountConfig = config.defineInRange("ingame.long_press_count", 2, 1, 10);
		this.showHealthIndicator = config.define("ingame.show_health_indicator", () -> true);
		this.showTargetIndicator = config.define("ingame.show_target_indicator", () -> true);
		this.filterAnimation = config.define("ingame.filter_animation", () -> true);
		this.aimHelperColor = config.defineInRange("ingame.laser_pointer_color", 0.328125D, 0.0D, 1.0D);
		this.enableAimHelper = config.define("ingame.enable_laser_pointer", () -> true);
		this.cameraAutoSwitch = config.define("ingame.camera_auto_switch", () -> false);
		this.autoPreparation = config.define("ingame.auto_preparation", () -> false);
		this.offBloodEffects = config.define("ingame.off_gore", () -> false);
		this.battleAutoSwitchItems = config.defineList("ingame.battle_autoswitch_items", Lists.newArrayList(), (element) -> {
			if (element instanceof String) {
				return ((String)element).contains(":");
			}
			return false;
		});
		this.miningAutoSwitchItems = config.defineList("ingame.mining_autoswitch_items", Lists.newArrayList(), (element) -> {
			if (element instanceof String) {
				return ((String)element).contains(":");
			}
			return false;
		});
	}
	public enum HealthBarShowOptions {
		NONE, HURT, TARGET;

		@Override
		public String toString() {
			return this.name().toLowerCase();
		}

		public HealthBarShowOptions nextOption() {
			return HealthBarShowOptions.values()[(this.ordinal() + 1) % 3];
		}
	}
}