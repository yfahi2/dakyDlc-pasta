package fun.drughack.modules.settings;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventSettingChange;
import lombok.*;

import java.util.function.Supplier;

@Getter @Setter
public abstract class Setting<Value> {

    private final String name;
    protected Value value, defaultValue;
    private Supplier<Boolean> visible = () -> true;

    public Setting(String name, Value defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public Setting(String name, Value defaultValue, Supplier<Boolean> visible) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.visible = visible;
    }

    public void setValue(Value value) {
        EventSettingChange event = new EventSettingChange(this);
        DrugHack.getInstance().getEventHandler().post(event);
        if (!event.isCancelled()) this.value = value;
    }

    public void reset() {
        this.value = defaultValue;
    }

    public boolean isVisible() {
        return visible.get();
    }
}