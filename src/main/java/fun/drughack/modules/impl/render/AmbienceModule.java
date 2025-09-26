package fun.drughack.modules.impl.render;

import fun.drughack.api.events.impl.EventPacket;
import fun.drughack.api.events.impl.FogEvent;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.api.Nameable;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.EnumSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import fun.drughack.screen.clickgui.components.impl.ColorSettings;
import fun.drughack.utils.render.ColorUti;
import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.awt.*;

public class AmbienceModule extends Module {

    NumberSetting timeSetting = new NumberSetting("Время", 10, 1f, 24000f, 1f);
    public final NumberSetting distanceFog = new NumberSetting("Дистанция Тумана", 50f, 10f, 100f, 0.01f);
    public final ColorSettings all = new ColorSettings("Target Color", "Цвет ауры");
    public final BooleanSetting a = new BooleanSetting("Кастом туман", true);



    public AmbienceModule() {
        super("AmbienceModule", Category.Render);
    }


    @EventHandler
    public void onPacket(EventPacket.Receive event) {
        if (mc == null || mc.world == null) return;

        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            event.cancel();
            long time = timeSetting.getValue().longValue();
            mc.world.setTime(time, time, true);
        }
    }
}

