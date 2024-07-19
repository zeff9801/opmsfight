
package yesman.epicfight.client.input;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.ClientEngine;

@OnlyIn(Dist.CLIENT)
public class CombatKeyMapping extends KeyBinding {
    public CombatKeyMapping(String description, InputMappings.Type type, int code, String category) {
        super(description, type, code, category);
    }

    @Override
    public boolean isActiveAndMatches(InputMappings.Input keyCode) {
        return super.isActiveAndMatches(keyCode) && ClientEngine.getInstance().isBattleMode();
    }
}
