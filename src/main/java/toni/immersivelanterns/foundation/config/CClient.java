package toni.immersivelanterns.foundation.config;

import toni.lib.config.ConfigBase;

public class CClient extends ConfigBase {

    public final ConfigGroup client = group(0, "client", "Client Settings");

    public final ConfigBool leftHandedLanterns = b(false, "Left-Handed Lanterns", "Whether lantern accessories should switch to the left side or not.");
    public final ConfigBool backLanterns = b(false, "Back Lanterns", "Whether lantern accessories should switch to the back of the player or not.");
    public final ConfigBool enablePhysics = b(true, "Enable Lantern Swinging Physics", "No performance impact, just cosmetic.");
    public final ConfigFloat bounciness = f(1f, 0.1f, 10f, "Lantern Bounciness", "How affected by in-world forces lantern physics should be. Default 1f");
    public final ConfigBool waterSensitiveBeltLight = b(false, "Water Sensitive Belt Light", "When enabled, belt lantern dynamic light is extinguished while submerged in water.");

    @Override
    public String getName() {
        return "client";
    }
}
