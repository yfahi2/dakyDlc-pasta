package fun.drughack.modules.impl.movement;

import fun.drughack.api.events.impl.EventPacket;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AirSuck extends Module {
    public AirSuck() {
        super("AirSuck", Category.Movement);
    }



    @EventHandler
    public void onUpdate(EventTick event) {
        event.cancel();
    }

    @EventHandler
    public void onPacket(EventPacket.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.cancel();
        }
    }
}
