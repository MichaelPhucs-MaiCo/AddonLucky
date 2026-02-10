package maico.addonbuu.modules.autofish;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public record PositionAndRotation(Vec3d pos, float yaw, float pitch) {
    public PositionAndRotation(Entity entity) {
        this(entity.getPos(), entity.getYaw(), entity.getPitch());
    }

    public boolean isNearlyIdenticalTo(PositionAndRotation other) {
        return pos.distanceTo(other.pos) < 0.5 && Math.abs(yaw - other.yaw) < 5 && Math.abs(pitch - other.pitch) < 5;
    }
}
