package fun.drughack.modules.impl.combat;

import fun.drughack.api.events.impl.EventPacket;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.network.ChatUtils;
import fun.drughack.utils.network.NetworkUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module {

    public Criticals() {
        super("Cricials 1.17", Category.Combat);
    }

    @EventHandler
    public void onPacketSend(EventPacket.Send e) {
        if (fullNullCheck()) return;
        if (e.getPacket() instanceof PlayerInteractEntityC2SPacket) {
            if (!mc.player.isOnGround()) {
                mc.player.setSprinting(false);
                ChatUtils.sendMessage("1");
                sendHit(-(mc.player.fallDistance = (float) MathUtils.getRandom(1e-5f, 1e-4f)), false);
                mc.player.setSprinting(true);
            }
        }
    }

    private void sendHit(double offsetY, boolean ground) {
        NetworkUtils.sendSilentPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(),
                mc.player.getY() + offsetY,
                mc.player.getZ(),
                mc.player.getYaw(),
                mc.player.getPitch(),
                ground,
                mc.player.horizontalCollision
        ));
    }
}