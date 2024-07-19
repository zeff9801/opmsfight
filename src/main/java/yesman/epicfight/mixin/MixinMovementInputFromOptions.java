package yesman.epicfight.mixin;

import net.minecraft.client.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import yesman.epicfight.mixin_interfaces.IExtendedMovementInput;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions extends MovementInput implements IExtendedMovementInput {

    @Shadow
    @Final
    private GameSettings options;

    @Override
    public void tick(boolean p_225607_1_, float p_234119_) {
        this.up = this.options.keyUp.isDown();
        this.down = this.options.keyDown.isDown();
        this.left = this.options.keyLeft.isDown();
        this.right = this.options.keyRight.isDown();
        this.forwardImpulse = this.up == this.down ? 0.0F : (this.up ? 1.0F : -1.0F);
        this.leftImpulse = this.left == this.right ? 0.0F : (this.left ? 1.0F : -1.0F);
        this.jumping = this.options.keyJump.isDown();
        this.shiftKeyDown = this.options.keyShift.isDown();
        if (p_225607_1_) {
            this.leftImpulse = p_234119_;
            this.forwardImpulse = p_234119_;
        }
    }
}
