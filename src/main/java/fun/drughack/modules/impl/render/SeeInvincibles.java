package fun.drughack.modules.impl.render;

import fun.drughack.api.events.Event;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import net.minecraft.entity.player.PlayerEntity;

public class SeeInvincibles extends Module {
    public SeeInvincibles() {
        super("SeeInvincibles", Category.Render);
    }
    public void onTick(EventTick tick) {
        if (mc.world != null) {
            for (PlayerEntity entity : mc.world.getPlayers()){
                if(entity.isInvisible()){
                    entity.setInvisible(false);
                }
            }
        }
    }
}
