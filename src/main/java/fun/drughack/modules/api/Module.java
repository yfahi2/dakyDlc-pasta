package fun.drughack.modules.api;

import fun.drughack.DrugHack;
import fun.drughack.modules.settings.Setting;
import fun.drughack.modules.settings.api.Bind;

import fun.drughack.utils.Wrapper;
import fun.drughack.utils.notify.Notify;
import fun.drughack.utils.notify.NotifyIcons;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Module implements Wrapper {
    private final String name, description;
    private final Category category;
    protected boolean toggled;
    @Setter private Bind bind = new Bind(-1, false);
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
        this.description = "descriptions" + "." + category.name().toLowerCase() + "." + name.toLowerCase();
    }
    public java.awt.Color getColor() {
        return toggled ? new Color(9, 128, 9,255) : new Color(128, 9, 9,255);
    }

    public void onEnable() {
        toggled = true;
        DrugHack.getInstance().getEventHandler().subscribe(this);
        if (!fullNullCheck()) DrugHack.getInstance().getNotifyManager().add(new Notify(NotifyIcons.successIcon, name + " включен", 1000,
                Color.WHITE, new Color(0, 255, 0)));
    }

    public void onDisable() {
        toggled = false;
        DrugHack.getInstance().getEventHandler().unsubscribe(this);
        if (!fullNullCheck()) DrugHack.getInstance().getNotifyManager().add(new Notify(NotifyIcons.failIcon, name  + " выключен", 1000,
                Color.WHITE, new Color(255, 0, 0)));

    }

    public void setToggled(boolean toggled) {
        if (toggled) onEnable();
        else onDisable();
    }

    public void toggle() {
        setToggled(!toggled);
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }
}