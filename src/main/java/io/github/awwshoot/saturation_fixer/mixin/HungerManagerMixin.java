package io.github.awwshoot.saturation_fixer.mixin;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {


    @Shadow private float foodSaturationLevel;

    @Shadow private int foodLevel;

    @Shadow public abstract void addExhaustion(float exhaustion);

    // because this is called after foodTickTimer can potentially reset, a new timer will be used
    int regenTick = 0;

    @Inject(at = @At("TAIL"), method = "update")
    private void passiveRegeneration(PlayerEntity player, CallbackInfo ci) {

        if (player.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION) && this.foodLevel > 0 && player.getHealth() < player.getMaxHealth()) {
            // Regen every 5 seconds while hunger is above half, and every 10 seconds below
            regenTick++;
            if (regenTick > (this.foodLevel > 10 ? 100 : 200)) {
                regenTick = 0;
                player.heal(1);
                /* Because of how much saturation high-end vanilla and modded foods give,
                 * (often to compensate for the fact that saturation is basically a second health bar)
                 * not causing exhaustion at all leads to hunger becoming a complete non-issue.
                 * Adding exhaustion only when saturation is present is something of a compromise
                 * to prevent high saturation foods from just lasting forever without hurting the core idea
                 * of separating the hunger and health bars.
                 */
                if (this.foodSaturationLevel > 0) {
                    this.addExhaustion(1.0f);
                }
            }
        }
    }
}
