package fun.drughack.modules.impl.movement;

import fun.drughack.api.events.impl.rotations.EventMotion;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.math.TimerUtils;
import fun.drughack.utils.world.ServerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public class GrimElytra extends Module {
    private TimerUtils ticks = new TimerUtils();
    int ticksTwo = 0;
    public GrimElytra(){
        super("GrimGlide", Category.Movement);
    }
    @EventHandler
    public void onEvent(EventMotion event) {
        if ((mc.player == null || mc.world == null) || !mc.player.isGliding()) return;
        ticksTwo++;
        Vec3d pos = mc.player.getPos();

        float yaw = mc.player.getYaw();
        double forward = 0.087;
        double motion = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ);

        float valuePidor = ServerUtil.isRw() ? 48 : 52;
        if (motion >= valuePidor) {
            forward = 0f;
            motion = 0;
        }

        double dx = -Math.sin(Math.toRadians(yaw)) * forward;
        double dz = Math.cos(Math.toRadians(yaw)) * forward;
        mc.player.setVelocity(dx * MathUtils.randomFloat(1.1f, 1.21f), mc.player.getVelocity().y - 0.02f, dz * MathUtils.randomFloat(1.1f, 1.21f));

        if (ticks.passed(50)) {
            mc.player.setPosition(
                    pos.getX() + dx,
                    pos.getY(),
                    pos.getZ() + dz
            );

            ticks.reset();
        }
        mc.player.setVelocity(dx * MathUtils.randomFloat(1.1f, 1.21f), mc.player.getVelocity().y + 0.016f, dz * MathUtils.randomFloat(1.1f, 1.21f));
    }
}

