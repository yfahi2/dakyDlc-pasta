package fun.drughack.modules.impl.misc;

import fun.drughack.api.events.impl.EventTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.movement.MoveUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class BedrockClip extends Module {

    public BedrockClip() {
        super("BedrockClip", Category.Misc);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (fullNullCheck()) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof BoatEntity && mc.player.distanceTo(entity) <= 2f) {

                mc.player.interact(entity, mc.player.getActiveHand());


                if (mc.player.getVehicle() == entity) {
                    if (MoveUtils.isMoving()) {
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), -1, mc.player.getZ(),mc.player.getYaw(), mc.player.getPitch(),mc.player.horizontalCollision,mc.player.horizontalCollision));
                    }
                }

                break;
            }
        }
    }
}