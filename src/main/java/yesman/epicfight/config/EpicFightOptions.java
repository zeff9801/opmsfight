package yesman.epicfight.config;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.client.gui.widget.ColorSlider;
import yesman.epicfight.config.Option.DoubleOption;
import yesman.epicfight.config.Option.IntegerOption;
import yesman.epicfight.config.OptionHandler.BooleanOptionHandler;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EpicFightOptions {
	public static final float A_TICK = 0.05F;
	public static final float GENERAL_ANIMATION_CONVERT_TIME = 0.15F;

	public final IntegerOption longPressCount;
	public final BooleanOptionHandler filterAnimation;
	public final BooleanOptionHandler showHealthIndicator;
	public final BooleanOptionHandler showTargetIndicator;
	public final DoubleOption aimHelperColor;
	public final BooleanOptionHandler enableAimHelperPointer;
	public final BooleanOptionHandler cameraAutoSwitch;
	public final BooleanOptionHandler autoPreparation;
	public final BooleanOptionHandler offBloodEffects;
	public final BooleanOptionHandler useAnimationShader;
	public BooleanOptionHandler firstPersonModel;
	public final Set<Item> battleAutoSwitchItems;
	public final Set<Item> miningAutoSwitchItems;
	public int aimHelperRealColor;
	public BooleanOptionHandler aimingCorrection;
	public boolean shaderModeSwitchingLocked = false;

	public EpicFightOptions() {
		ClientConfig config = ConfigManager.INGAME_CONFIG;
		this.aimingCorrection = new BooleanOptionHandler(config.aimingCorrection.get());
		this.firstPersonModel = new BooleanOptionHandler(config.firstPersonModel.get());
		this.longPressCount = new IntegerOption(config.longPressCountConfig.get(), 1, 10);
		this.filterAnimation = new BooleanOptionHandler(config.filterAnimation.get());
		this.showHealthIndicator = new BooleanOptionHandler(config.showHealthIndicator.get());
		this.showTargetIndicator = new BooleanOptionHandler(config.showTargetIndicator.get());
		this.aimHelperColor = new DoubleOption(config.aimHelperColor.get(), 0.0D, 1.0D);
		this.enableAimHelperPointer = new BooleanOptionHandler(config.enableAimHelper.get());
		this.aimHelperRealColor = ColorSlider.toColorInteger(config.aimHelperColor.get());
		this.cameraAutoSwitch = new BooleanOptionHandler(config.cameraAutoSwitch.get());
		this.autoPreparation = new BooleanOptionHandler(config.autoPreparation.get());
		this.offBloodEffects = new BooleanOptionHandler(config.offBloodEffects.get());
		this.useAnimationShader = new BooleanOptionHandler(config.useAnimationShader.get());
		this.battleAutoSwitchItems = new HashSet<>(config.battleAutoSwitchItems.get().stream()
				.map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));

		this.miningAutoSwitchItems = new HashSet<>(config.miningAutoSwitchItems.get().stream()
				.map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));


	}

	public void resetSettings() {
		this.longPressCount.setDefaultValue();
		this.useAnimationShader.setDefaultValue();
		this.filterAnimation.setDefaultValue();
		this.showHealthIndicator.setDefaultValue();
		this.showTargetIndicator.setDefaultValue();
		this.aimHelperColor.setDefaultValue();
		this.enableAimHelperPointer.setDefaultValue();
		this.cameraAutoSwitch.setDefaultValue();
		this.autoPreparation.setDefaultValue();
		this.offBloodEffects.setDefaultValue();
		this.firstPersonModel.setDefaultValue();
		this.aimingCorrection.setDefaultValue();
		this.aimHelperRealColor = ColorSlider.toColorInteger(this.aimHelperColor.getValue());
	}

	public void save() {
		ClientConfig config = ConfigManager.INGAME_CONFIG;
		config.longPressCountConfig.set(this.longPressCount.getValue());
		config.filterAnimation.set(this.filterAnimation.getValue());
		config.showHealthIndicator.set(this.showHealthIndicator.getValue());
		config.firstPersonModel.set(this.firstPersonModel.getValue());
		this.aimingCorrection = new BooleanOptionHandler(config.aimingCorrection.get());
		config.showTargetIndicator.set(this.showTargetIndicator.getValue());
		config.aimHelperColor.set(this.aimHelperColor.getValue());
		config.useAnimationShader.set(this.useAnimationShader.getValue());
		config.enableAimHelper.set(this.enableAimHelperPointer.getValue());
		config.cameraAutoSwitch.set(this.cameraAutoSwitch.getValue());
		config.autoPreparation.set(this.autoPreparation.getValue());
		config.offBloodEffects.set(this.offBloodEffects.getValue());
		this.aimHelperRealColor = ColorSlider.toColorInteger(this.aimHelperColor.getValue());
		config.battleAutoSwitchItems.set(Lists.newArrayList(this.battleAutoSwitchItems.stream().map((item) -> item.getRegistryName().toString()).iterator()));
		config.miningAutoSwitchItems.set(Lists.newArrayList(this.miningAutoSwitchItems.stream().map((item) -> item.getRegistryName().toString()).iterator()));
	}
}