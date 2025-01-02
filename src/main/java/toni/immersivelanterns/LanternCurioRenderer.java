package toni.immersivelanterns;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

#if FORGELIKE
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import toni.immersivelanterns.foundation.config.AllConfigs;
import toni.immersivelanterns.foundation.mixin.ModelPartAccessor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.ArrayList;
#endif

public class LanternCurioRenderer extends BaseLanternRenderer #if FORGELIKE implements ICurioRenderer #endif {

    public static void register() {
        #if FORGELIKE
        CuriosRendererRegistry.register(Items.LANTERN, LanternCurioRenderer::new);
        CuriosRendererRegistry.register(Items.SOUL_LANTERN, LanternCurioRenderer::new);
        #endif
    }


    public static boolean isEquipped(Player player) {
        #if FORGELIKE
        var curios = CuriosApi.getCuriosInventory(player);
        if (!curios.isPresent())
            return false;

        #if NEO
        return curios.get().isEquipped(stack -> stack.getItem() == Items.LANTERN || stack.getItem() == Items.SOUL_LANTERN);
        #else
        return curios.resolve().get().isEquipped(stack -> stack.getItem() == Items.LANTERN || stack.getItem() == Items.SOUL_LANTERN);
        #endif

        #else
        return false;
        #endif
    }

    public static ItemStack getEquipped(Player player) {
        #if FORGELIKE
        if (!isEquipped(player))
           return null;

        return CuriosApi.getCuriosInventory(player)
            #if FORGE .resolve() #endif
            .get()
            .findFirstCurio(stack -> stack.getItem() == Items.LANTERN || stack.getItem() == Items.SOUL_LANTERN)
            .get()
            .stack();
        #else
        return null;
        #endif
    }

    #if FORGELIKE
    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
        ItemStack stack,
        SlotContext slotContext,
        PoseStack matrices,
        RenderLayerParent<T, M> renderLayerParent,
        MultiBufferSource multiBufferSource,
        int light, float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch)
    {
        if (slotContext.entity() instanceof Player player && renderLayerParent.getModel() instanceof PlayerModel<?> playerModel) {
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
            matrices.pushPose();

            var xOffset = AllConfigs.client().leftHandedLanterns.get() ? 0.1f : 2f;
            var zOffset = AllConfigs.client().backLanterns.get() ? (isWearingArmor ? -3.1f : -3f) : -1f;

            var hipOffset = isWearingArmor ? new Vec3(xOffset + 0.05f, -1.25f, zOffset + 0.05f) : new Vec3(xOffset - 0.1f, -1.25f, zOffset - 0.1f);

            transformToModelPart(matrices, playerModel.body, hipOffset.x, hipOffset.y, hipOffset.z);

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
            matrices.popPose();
        }
    }

    static void transformToModelPart(PoseStack poseStack, ModelPart part, Number xPercent, Number yPercent, Number zPercent) {
        part.translateAndRotate(poseStack);
        var aabb = getAABB(part);
        poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f);
        poseStack.translate(
            xPercent != null ? Mth.lerp((-xPercent.doubleValue() + 1) / 2, aabb.getFirst().x, aabb.getSecond().x) : 0,
            yPercent != null ? Mth.lerp((-yPercent.doubleValue() + 1) / 2, aabb.getFirst().y, aabb.getSecond().y) : 0,
            zPercent != null ? Mth.lerp((-zPercent.doubleValue() + 1) / 2, aabb.getFirst().z, aabb.getSecond().z) : 0
        );
        poseStack.scale(8, 8, 8);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
    }

    private static Pair<Vec3, Vec3> getAABB(ModelPart part) {
        Vec3 min = new Vec3(0, 0, 0);
        Vec3 max = new Vec3(0, 0, 0);

        if (part.getClass().getSimpleName().contains("EMFModelPart")) {
            var parts = new ArrayList<ModelPart>();

            parts.add(part);
            parts.addAll(((ModelPartAccessor) (Object) part).getChildren().values());

            for (var modelPart : parts) {
                for (ModelPart.Cube cube : ((ModelPartAccessor) (Object) modelPart).getCubes()) {
                    min = new Vec3(
                        Math.min(min.x, Math.min(cube.minX + modelPart.x, cube.maxX + modelPart.x)),
                        Math.min(min.y, Math.min(cube.minY + modelPart.y, cube.maxY + modelPart.y)),
                        Math.min(min.z, Math.min(cube.minZ + modelPart.z, cube.maxZ + modelPart.z))
                    );
                    max = new Vec3(
                        Math.max(max.x, Math.max(cube.minX + modelPart.x, cube.maxX + modelPart.x)),
                        Math.max(max.y, Math.max(cube.minY + modelPart.y, cube.maxY + modelPart.y)),
                        Math.max(max.z, Math.max(cube.minZ + modelPart.z, cube.maxZ + modelPart.z))
                    );
                }
            }
        } else {
            for (ModelPart.Cube cube : ((ModelPartAccessor) (Object) part).getCubes()) {
                min = new Vec3(
                    Math.min(min.x, Math.min(cube.minX, cube.maxX)),
                    Math.min(min.y, Math.min(cube.minY, cube.maxY)),
                    Math.min(min.z, Math.min(cube.minZ, cube.maxZ))
                );
                max = new Vec3(
                    Math.max(max.x, Math.max(cube.minX, cube.maxX)),
                    Math.max(max.y, Math.max(cube.minY, cube.maxY)),
                    Math.max(max.z, Math.max(cube.minZ, cube.maxZ))
                );
            }
        }

        return Pair.of(min, max);
    }
    #endif
}