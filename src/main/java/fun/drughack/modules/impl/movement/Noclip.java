package fun.drughack.modules.impl.movement;

import fun.drughack.api.events.Event;
import fun.drughack.api.events.impl.EventCollision;
import fun.drughack.api.events.impl.EventPacket;
import fun.drughack.api.events.impl.EventRender3D;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.impl.combat.Aura;
import fun.drughack.modules.settings.api.Nameable;
import fun.drughack.modules.settings.impl.EnumSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import fun.drughack.utils.network.ChatUtils;
import fun.drughack.utils.network.NetworkUtils;
import fun.drughack.utils.render.Render3D;
import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Noclip extends Module {

    public Noclip(){
        super("Noclip",Category.Movement);
    }
    private final List<Packet<?>> packets = new CopyOnWriteArrayList<>();

    private Box box;
    private int tickCounter = 0;


    @EventHandler
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) {
            toggle();
        }

        if (event instanceof EventPacket ep) {
            if (ep instanceof EventPacket.All) {
                Packet<?> p = ep.packet;
                if (shouldPhase() && !(p instanceof KeepAliveC2SPacket) && !(p instanceof CommonPongC2SPacket)) {
                    packets.add(p);
                    ep.cancel();
                    ChatUtils.sendMessage("§c1 §f" );
                }
            }

            if (ep instanceof EventPacket.Receive && ep.packet instanceof PlayerPositionLookS2CPacket) {
                resumePackets();
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        mc.player.getYaw(),
                        mc.player.getPitch(),
                        mc.player.isOnGround(),
                        false
                ));
            }
        }

        if (event instanceof EventTick) {
            tickCounter++;
            if (tickCounter >= 10) {
                resumePackets();
                tickCounter = 0;
            }

            if (shouldPhase()) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        mc.player.getYaw(),
                        mc.player.getPitch(),
                        mc.player.isOnGround(),
                        false
                ));
            }
            mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
        }

        if (event instanceof EventRender3D) {
            if (box != null) {
                //  RenderUtil.render3D.drawHoleOutline(box, Color.WHITE, 2f);
            }
        }
    }
    @EventHandler
    private boolean shouldPhase() {
        if (mc.player == null || mc.world == null) return false;
        ChatUtils.sendMessage("§c3 §f" );
        Box hitbox = mc.player.getBoundingBox();
        BlockPos min = new BlockPos((int) Math.floor(hitbox.minX), (int) Math.floor(hitbox.minY), (int) Math.floor(hitbox.minZ));
        BlockPos max = new BlockPos((int) Math.floor(hitbox.maxX), (int) Math.floor(hitbox.maxY), (int) Math.floor(hitbox.maxZ));

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    if (!state.isAir() && mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).getBoundingBoxes().stream().anyMatch(box -> box.intersects(hitbox.offset(-pos.getX(), -pos.getY(), -pos.getZ())))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    @EventHandler
    private void resumePackets() {
        if (mc.player == null || mc.world == null) {
            toggle();
        }
        if (!packets.isEmpty()) {
            for (Packet<?> packet : new ArrayList<>(packets)) {
                NetworkUtils.sendSilentPacket(packet);
            }
            packets.clear();
            box = mc.player.getBoundingBox();
        }
    }

    @Override
    public void onDisable() {
        ChatUtils.sendMessage("§c2 §f" );
        super.onDisable();
        resumePackets();
        box = null;
        tickCounter = 0;
    }
}
