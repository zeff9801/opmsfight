package yesman.epicfight.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyBindingMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(KeyBindingMap.class)
public class MixinKeyBindingMap {

    /**
     * @return
     * @Reason do not allow vanilla keybind picker, pick the vanilla attack keybind if combat mode is on.
     * Without this, the epic fight keybind with certain mods, will never get picked to be "clicked"
     */
    @Redirect(method = "getBinding", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isActiveAndMatches(Lnet/minecraft/client/util/InputMappings$Input;)Z"), remap = false)
    boolean inject_disableAttackInCombat(KeyBinding instance, InputMappings.Input input) {
        Minecraft mcInstance = Minecraft.getInstance();
        if (instance == mcInstance.options.keyAttack) {
            System.out.println("IDENTIFIED ATTACK KEYBIND");
            LocalPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(mcInstance.player, LocalPlayerPatch.class);
            if (playerPatch != null) {
                System.out.println("PLAYER IS IN COMBAT MODE, DENY VANILLA ATTACK KEYBIND FROM BEING PICKED");
                return !playerPatch.isBattleMode() && instance.isActiveAndMatches(input);
            }
        }
        return instance.isActiveAndMatches(input);
    }


}
