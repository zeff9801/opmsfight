package yesman.epicfight.world.capabilities.item;

public enum Styles implements Style {
    COMMON(true), ONE_HAND(true), TWO_HAND(false), MOUNT(true), RANGED(false), SHEATH(false), OCHS(false);

    private final boolean canUseOffhand;
    private int universalOrdinal;

    Styles(boolean canUseOffhand) {
        this.canUseOffhand = canUseOffhand;
    }

    static {
        for (Styles style : values()) {
            style.universalOrdinal = Style.ENUM_MANAGER.assign(style);
        }
    }

    public boolean canUseOffhand() {
        return this.canUseOffhand;
    }

    @Override
    public int universalOrdinal() {
        return this.universalOrdinal;
    }
} 