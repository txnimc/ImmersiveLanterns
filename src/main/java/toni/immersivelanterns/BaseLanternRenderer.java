package toni.immersivelanterns;

import com.mojang.math.Constants;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import toni.immersivelanterns.foundation.IPlayerLanternDataAccessor;
import toni.immersivelanterns.foundation.ImmersiveLanternsMixinConfigPlugin;
import toni.immersivelanterns.foundation.config.AllConfigs;

public class BaseLanternRenderer {
    Vec3 updatePendulum(Player player, Vec3 newPos, double dt) {
        if (ImmersiveLanternsMixinConfigPlugin.isIrisRenderingShadows())
            return Vec3.ZERO;

        var lanternData = (IPlayerLanternDataAccessor) player;

        var forceStrength = AllConfigs.client().bounciness.get();
        var wasCrouching = lanternData.immersiveLanterns$getWasCrouching();
        var isCrouching = player.isCrouching();
        lanternData.immersiveLanterns$setWasCrouching(isCrouching);

        var zAngle = lanternData.immersiveLanterns$getZAngle();
        var zVel = lanternData.immersiveLanterns$getZVel();

        var xAngle = lanternData.immersiveLanterns$getXAngle();
        var xVel = lanternData.immersiveLanterns$getXVel();

        if (!wasCrouching && isCrouching) {
            xAngle = Constants.PI / 8;
            zAngle = Constants.PI / 10;
        }

        var oldPos = lanternData.immersiveLanterns$getLastHipPosition();
        if (oldPos == null)
            oldPos = newPos;

        var delta = newPos.subtract(oldPos);
        var deltaForward = transformToPlayerRelativeMovement(delta, player.getForward());

        var zForce = 9.81f * Math.sin(zAngle);
        zForce += deltaForward.z * forceStrength * -50;
        zForce += deltaForward.y * forceStrength * -20;
        var zAccel = (-1 * zForce);
        zVel += (float) (zAccel * (dt / 20));
        zVel *= 0.98f;
        zAngle += (float) (zVel * (dt / 20));
        zAngle = Math.max(-Constants.PI / 3, Math.min(Constants.PI / 3, zAngle));

        var xForce = 9.81f * Math.sin(xAngle);
        xForce += deltaForward.x * forceStrength * -50;
        xForce += deltaForward.y * forceStrength * -20;
        var xAccel = (-1 * xForce);
        xVel += (float) (xAccel * (dt / 20));
        xVel *= 0.98f;
        xAngle += (float) (xVel * (dt / 20));
        xAngle = Math.max(-Constants.PI / 3, Math.min(Constants.PI / 3, xAngle));

        lanternData.immersiveLanterns$setZVel(Mth.clamp(zVel, -3f, 3f));
        lanternData.immersiveLanterns$setZAngle(zAngle);

        lanternData.immersiveLanterns$setXVel(Mth.clamp(xVel, -3f, 3f));
        lanternData.immersiveLanterns$setXAngle(xAngle);

        lanternData.immersiveLanterns$setLastHipPosition(newPos);

        return new Vec3(
            Math.abs(xAngle) < 0.01f ? 0f : xAngle,
            0f,
            Math.abs(zAngle) < 0.01f ? 0f : zAngle);
    }

    public Vec3 transformToPlayerRelativeMovement(Vec3 deltaMovement, Vec3 forwardVector) {
        // Normalize the forward vector to ensure it has a length of 1
        Vec3 forward = forwardVector.normalize();

        // Compute the right vector by taking the cross product of forward and up vectors
        // Assume the player's up vector is (0, 1, 0) if the player is standing upright
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = forward.cross(up).normalize();

        // Recompute the up vector to ensure it is perpendicular to both forward and right
        up = right.cross(forward).normalize();

        // Now project the world space delta movement onto the local coordinate system
        // The local movement is the dot product of deltaMovement with each of the local axes
        double localForward = deltaMovement.dot(forward);
        double localRight = deltaMovement.dot(right);
        double localUp = deltaMovement.dot(up);

        // Return the movement relative to the player's local space as a new Vec3
        return new Vec3(localRight, localUp, localForward);
    }
}
