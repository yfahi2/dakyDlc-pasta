package fun.drughack.modules.impl.misc;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventPacket;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class AutoAccept extends Module {

    public AutoAccept() {
        super("AutoAccept", Category.Misc);
    }

    @EventHandler
    public void onPacketReceive(EventPacket.Receive e) {
        if (fullNullCheck()) return;

        if (e.getPacket() instanceof GameMessageS2CPacket packet
                && (packet.content().getString().toLowerCase().contains("хочет")
                || packet.content().getString().toLowerCase().contains("телепортироваться")
                || packet.content().getString().toLowerCase().contains("к вам")
                || packet.content().getString().toLowerCase().contains("wants")
                || packet.content().getString().toLowerCase().contains("teleport")
                || packet.content().getString().toLowerCase().contains("to you"))) {
            for (String name : DrugHack.getInstance().getFriendManager().getFriends()) {
                if (packet.content().getString().contains(name)) {
                    mc.getNetworkHandler().sendChatCommand("tpaccept");
                    break;
                }
            }
        }
    }
}