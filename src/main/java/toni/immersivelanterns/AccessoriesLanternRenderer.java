package toni.immersivelanterns;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Constants;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.client.SimpleAccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.pond.AccessoriesAPIAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import toni.immersivelanterns.foundation.IPlayerLanternDataAccessor;
import toni.immersivelanterns.foundation.ImmersiveLanternsMixinConfigPlugin;
import toni.immersivelanterns.foundation.config.AllConfigs;

#if mc == "201"
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
#endif

class AccessoriesLanternRenderer extends BaseLanternRenderer implements SimpleAccessoryRenderer {
    public static void register() {
        AccessoriesRendererRegistry.registerRenderer(Items.LANTERN, AccessoriesLanternRenderer::new);
        AccessoriesRendererRegistry.registerRenderer(Items.SOUL_LANTERN, AccessoriesLanternRenderer::new);
    }

    public static boolean isEquipped(Player player) {
        var accessories = (AccessoriesAPIAccess) player;
        var capability = accessories.accessoriesCapability();
        if (capability == null)
            return false;

        return capability.isEquipped(stack -> stack.getItem() == Items.LANTERN || stack.getItem() == Items.SOUL_LANTERN);
    }

    public static ItemStack getEquipped(Player player) {
        var accessories = (AccessoriesAPIAccess) player;
        var capability = accessories.accessoriesCapability();
        if (capability == null)
            return null;

        var equipped = capability.getEquipped(stack -> stack.getItem() == Items.LANTERN || stack.getItem() == Items.SOUL_LANTERN);
        return equipped.get(0).reference().getStack();
    }

    @Override
    public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, PoseStack matrices, EntityModel<M> model, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (reference.entity() instanceof Player player && model instanceof PlayerModel<M> playerModel) {
            boolean isWearingArmor = false;
            for (var armor : player.getArmorSlots()) {
                #if mc > "201"
                if (armor.is(ItemTags.LEG_ARMOR) || armor.is(ItemTags.CHEST_ARMOR)) {
                    isWearingArmor = true;
                    break;
                }
                #else
                if (armor.getItem() instanceof ArmorItem armorItem && (armorItem.getEquipmentSlot() == EquipmentSlot.LEGS || armorItem.getEquipmentSlot() == EquipmentSlot.CHEST)) {
                    isWearingArmor = true;
                    break;
                }
                #endif
            }

            var lanternTop = 11f / 16f;

            var xOffset = AllConfigs.client().leftHandedLanterns.get() ? 0.1f : 2f;
            var zOffset = AllConfigs.client().backLanterns.get() ? (isWearingArmor ? -3.1f : -3f) : -1f;

            var hipOffset = isWearingArmor ? new Vec3(xOffset + 0.05f, -1.25f, zOffset + 0.05f) : new Vec3(xOffset - 0.1f, -1.25f, zOffset - 0.1f);

            AccessoryRenderer.transformToModelPart(matrices, playerModel.body, hipOffset.x, hipOffset.y, hipOffset.z);

            matrices.translate(0.5f, lanternTop, 0.5f);

            Vector4f localPosition = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
            localPosition.mulTranspose(matrices.last().pose());

            var hipPosition = new Vec3(localPosition.x(), localPosition.y(), localPosition.z());
            hipPosition = hipPosition.add(player.getPosition(partialTicks));

            Vec3 update = Minecraft.getInstance().screen == null && AllConfigs.client().enablePhysics.get()
                ? updatePendulum(player, hipPosition, partialTicks)
                : Vec3.ZERO;

            var xRot = update.z;
            xRot += (Math.min(0, playerModel.rightLeg.xRot / 3)) - (AllConfigs.client().backLanterns.get() ? -0.1f : 0.1f);
            xRot -= playerModel.body.xRot;

            matrices.mulPose((new Quaternionf()).rotationZYX((float) update.x, 0f, (float) xRot));
            matrices.translate(-0.5f, -lanternTop, -0.5f);

            var blockstate = Block.byItem(stack.getItem()).defaultBlockState();
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockstate, matrices, multiBufferSource, light, OverlayTexture.NO_OVERLAY);

        }
    }

    @Override
    public <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, EntityModel<M> model, PoseStack matrices) {

    }
}
