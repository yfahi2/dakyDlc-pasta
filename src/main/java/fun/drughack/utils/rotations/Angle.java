package fun.drughack.utils.rotations;

import fun.drughack.utils.Wrapper;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Getter
@Setter
@ToString
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Angle implements Wrapper {
    public static Angle DEFAULT = new Angle(0, 0);
    float yaw, pitch;

    public Angle adjustSensitivity() {
        double gcd = RotationUtils.getGCDValue();




        float adjustedYaw = adjustAxis(yaw,  mc.player.getYaw(), gcd);
        float adjustedPitch = adjustAxis(pitch, mc.player.getPitch(), gcd);

        return new Angle(adjustedYaw, MathHelper.clamp(adjustedPitch, -90f, 90f));
    }

    private float adjustAxis(float axisValue, float previousValue, double gcd) {
        float delta = axisValue - previousValue;
        return previousValue + Math.round(delta / gcd) * (float) gcd;
    }

    public final Vec3d toVector() {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
    }

    @ToString
    @Getter
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class VecRotation {
        final Angle angle;
        final Vec3d vec;
    }
}
