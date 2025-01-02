package toni.immersivelanterns.foundation.mixin;

import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import toni.immersivelanterns.ImmersiveLanterns;

@Mixin(value = DynamicLightHandlers.class, remap = false)
public class DynamicLightsMixin {

    @Inject(method = "registerDefaultHandlers", at = @At("HEAD"))
    private static void onRegisterDynLights(CallbackInfo ci) {
        DynamicLightHandlers.registerDynamicLightHandler(EntityType.PLAYER, DynamicLightHandler.makeHandler(player -> {
            var isEquipped = ImmersiveLanterns.isEquipped(player);
            return isEquipped ? 15 : 0;
        }, player -> true));
    }
}
