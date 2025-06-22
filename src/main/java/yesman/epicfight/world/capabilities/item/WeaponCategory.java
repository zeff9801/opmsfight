package yesman.epicfight.world.capabilities.item;

import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

public enum WeaponCategory implements ExtendableEnum {
    NOT_WEAPON, AXE, FIST, GREATSWORD, HOE, PICKAXE, SHOVEL, SWORD, KATANA, SPEAR, TACHI, TRIDENT, LONGSWORD, DAGGER, SHIELD, RANGED;

    public static final ExtendableEnumManager<WeaponCategory> ENUM_MANAGER = new ExtendableEnumManager<>("weapon_category");
    private int universalOrdinal;

    static {
        for (WeaponCategory category : values()) {
            category.universalOrdinal = ENUM_MANAGER.assign(category);
        }
    }

    @Override
    public int universalOrdinal() {
        return this.universalOrdinal;
    }
}