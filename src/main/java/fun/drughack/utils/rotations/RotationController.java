package fun.drughack.utils.rotations;

import fun.drughack.utils.Wrapper;
import net.minecraft.util.math.Vec2f;

public class RotationController implements Wrapper {

    public static RotationController INSTANCE = new RotationController();

    public RotationController() {

    }




    Angle currentAngle;

    Angle previousAngle;

    Angle serverAngle = Angle.DEFAULT;

    public void setRotation(Angle value) {
        if (value == null) {
            this.previousAngle = this.currentAngle != null ? this.currentAngle : (mc.player != null ? new Angle(mc.player.getYaw(), mc.player.getPitch()) : Angle.DEFAULT);
        } else {
            this.previousAngle = this.currentAngle;
        }
        this.currentAngle = value;
    }
    public Angle getRotation() {
        Vec2f playerRotation = mc.player.getRotationClient();
        return currentAngle != null ? currentAngle : RotationUtils.fromVec2f(playerRotation);
    }
}
