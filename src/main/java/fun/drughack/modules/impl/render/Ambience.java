package fun.drughack.modules.impl.render;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventPacket;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.network.Server;
import fun.drughack.utils.render.fonts.Fonts;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.world.WorldUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.jetbrains.annotations.NotNull;

public class Ambience extends Module {

    public final BooleanSetting ctime = new BooleanSetting("Менять время", true);
    public final NumberSetting ctimeVal = new NumberSetting("Время", 21, 0, 23, 1f);

    public Ambience() {
        super("Ambience", Category.Render);
    }

    private long oldTime;
    private boolean hasStoredTime = false;

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.world != null) {
            oldTime = mc.world.getTime();
            hasStoredTime = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.world != null && hasStoredTime) {
            mc.world.setTime(oldTime, oldTime, true);
        }
    }

    @EventHandler
    private void onPacketReceive(EventPacket.@NotNull Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket && ctime.getValue()) {
            oldTime = ((WorldTimeUpdateS2CPacket) event.getPacket()).time();
            event.cancel();
        }
    }

    @EventHandler
    public void onUpdate(EventTick eventTick) {
        if (ctime.getValue() && mc.world != null) {
            long time = ctimeVal.getValue().longValue() * 1000;
            mc.world.setTime(time, time, true);
        }
    }
}
