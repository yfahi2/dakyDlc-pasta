package fun.drughack.utils.movement;

import fun.drughack.utils.Wrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@UtilityClass
public class MoveUtils implements Wrapper {

    public boolean isMoving() {
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
    }
    public static Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }
    public static void setMotion(final double motion) {
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();

        if (forward == 0 && strafe == 0) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                forward = (forward > 0) ? 1 : (forward < 0 ? -1 : 0);
            }

            float yawRad = (float) Math.toRadians(yaw + 90.0f);
            float cosYaw = MathHelper.cos(yawRad);
            float sinYaw = MathHelper.sin(yawRad);

            double velocityX = forward * motion * cosYaw + strafe * motion * sinYaw;
            double velocityZ = forward * motion * sinYaw - strafe * motion * cosYaw;

            mc.player.setVelocity(velocityX, mc.player.getVelocity().y, velocityZ);
        }
    }

    public static double[] forward(final double d) {
        float f = mc.player.input.movementForward;
        float f2 = mc.player.input.movementSideways;
        float f3 = mc.player.getYaw();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += ((f > 0.0f) ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += ((f > 0.0f) ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        final double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        final double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        final double d4 = f * d * d3 + f2 * d * d2;
        final double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }


    public static void setMotionWidthY(float motion, final double y) {
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();

        if (forward == 0 && strafe == 0) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                forward = (forward > 0) ? 1 : (forward < 0 ? -1 : 0);
            }

            float yawRad = (float) Math.toRadians(yaw + 90.0f);
            float cosYaw = MathHelper.cos(yawRad);
            float sinYaw = MathHelper.sin(yawRad);

            double velocityX = forward * motion * cosYaw + strafe * motion * sinYaw;
            double velocityZ = forward * motion * sinYaw - strafe * motion * cosYaw;

            mc.player.setVelocity(velocityX, y, velocityZ);
        }
    }
}
