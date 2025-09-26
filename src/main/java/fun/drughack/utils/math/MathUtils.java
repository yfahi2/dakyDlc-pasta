package fun.drughack.utils.math;

import fun.drughack.utils.Wrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class MathUtils implements Wrapper {

    public boolean isHovered(float x, float y, float width, float height, float mouseX, float mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public static double interpolate2(double current, double old, double scale) {
        return old + (current - old) * scale;
    }
    public static double interpolate3(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    public static Vec3d interpolate2(Vec3d end, Vec3d start, float multiple) {
        return new Vec3d(
                interpolate2(end.getX(), start.getX(), multiple),
                interpolate2(end.getY(), start.getY(), multiple),
                interpolate2(end.getZ(), start.getZ(), multiple));
    }
    public static float getRotationsStep(float current, float target, float step) {
        float diff = target - current;


        diff = (diff % 360 + 540) % 360 - 180;

        if (diff > step) {
            return current + step;
        } else if (diff < -step) {
            return current - step;
        } else {
            return target;
        }
    }
    public static int getAlpha(int hex) {
        return hex >> 24 & 255;
    }

    public static float[] getRotationTo(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;

        double distance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distance));

        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -89f, 89f)};
    }
    public static double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }
    public static double interpolate4(double prev, double now, double delta) {
        return prev + (now - prev) * delta;
    }

    public static float rotateTo(float current, float target, float step) {
        float diff = MathHelper.wrapDegrees(target - current);
        if (diff < 0) step = -step;
        else if (diff == 0) return current;
        return current + Math.max(Math.min(diff, step), -step);
    }
    public static float[] getRotations1(Vec3d pos) {
        Vec3d eyes = mc.player.getEyePos();
        double diffX = pos.x - eyes.x;
        double diffY = pos.y - eyes.y;
        double diffZ = pos.z - eyes.z;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, dist)));

        return new float[]{yaw, pitch};
    }
    public float randomFloat(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min) + min);
    }
    private final Set<Integer> usedValues = new HashSet<>();

    public int uniqueRandomInt(int min, int max) {
        if (usedValues.size() >= max - min) {
            usedValues.clear();
        }

        int result;
        do {
            result = ThreadLocalRandom.current().nextInt(min, max);
        } while (!usedValues.add(result));

        return result;
    }
    private final Set<Integer> usedFloatValues = new HashSet<>();

    public float uniqueRandomFloat(float min, float max) {
        if (usedFloatValues.size() >= (long)((max - min) * 1e6)) {
            usedFloatValues.clear();
        }

        float result;
        int intBits;
        do {
            result = min + ThreadLocalRandom.current().nextFloat() * (max - min);
            intBits = Float.floatToIntBits(result);
        } while (!usedFloatValues.add(intBits));

        return result;
    }
    public float round(float number) {
        return Math.round(number * 10f) / 10f;
    }

    public float round(float num, float increment) {
        float value = Math.round(num / increment) * increment;
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
    public static double getRandom(double min, double max) {
        if (min == max) {
            return min;
        } else {
            if (min > max) {
                double d = min;
                min = max;
                max = d;
            }

            return ThreadLocalRandom.current().nextDouble(min, max);
        }
    }
    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickDelta(false);
    }
    public Vec3d transform(Matrix4f matrix, float x, float y, float z) {
        Vector3f vector3f = matrix.transformPosition(x, y, z, new Vector3f());
        return new Vec3d(vector3f.x(), vector3f.y(), vector3f.z());
    }
    public static float[] getRotations(Vec3d pos) {
        Vec3d eyes = mc.player.getEyePos();
        double dX = pos.x - eyes.x;
        double dY = pos.y - eyes.y;
        double dZ = pos.z - eyes.z;

        double distance = Math.sqrt(dX * dX + dZ * dZ);

        float yaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dY, distance));

        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch)};
    }
    public String getCurrentTime() {
        ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Europe/Moscow"));
        return date.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public boolean inFov(Vec3d pos, int fov, float yaw) {
        double deltaX = pos.getX() - mc.player.getX();
        double deltaZ = pos.getZ() - mc.player.getZ();
        float angle = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        float yawDelta = MathHelper.wrapDegrees(angle - yaw);

        return Math.abs(yawDelta) <= fov;
    }

    public float getStep(float current, float target, float step) {
        if (Math.abs(target - current) <= step) return target;

        return current + Math.signum(target - current) * step;
    }
}