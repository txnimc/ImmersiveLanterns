package toni.immersivelanterns.foundation.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import toni.immersivelanterns.ImmersiveLanterns;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.uniforms.IdMapUniforms$HeldItemSupplier")
public class IrisIdMapUniformsMixin {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack onAddIdMapUniforms(LocalPlayer instance, InteractionHand interactionHand) {
        if (interactionHand == InteractionHand.MAIN_HAND)
            return instance.getItemInHand(interactionHand);

        var equipped = ImmersiveLanterns.getEquipped(instance);
        if (equipped == null)
            return instance.getItemInHand(interactionHand);

        return equipped;
    }
}
