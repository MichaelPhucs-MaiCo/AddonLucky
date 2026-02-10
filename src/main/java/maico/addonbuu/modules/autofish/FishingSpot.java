package maico.addonbuu.modules.autofish;

import net.minecraft.util.math.Vec3d;

public record FishingSpot(PositionAndRotation input, Vec3d bobberPos, boolean openWater) {}
