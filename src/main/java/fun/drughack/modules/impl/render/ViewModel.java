package fun.drughack.modules.impl.render;

import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.impl.NumberSetting;

public class ViewModel extends Module {

    public NumberSetting mainX = new NumberSetting("MainX", 0f, -2f, 2f, 0.05f);
    public NumberSetting mainY = new NumberSetting("MainY", 0f, -2f, 2f, 0.05f);
    public NumberSetting mainZ = new NumberSetting("MainZ", 0f, -2f, 2f, 0.05f);
    public NumberSetting offX = new NumberSetting("OffX", 0f, -2f, 2f, 0.05f);
    public NumberSetting offY = new NumberSetting("OffY", 0f, -2f, 2f, 0.05f);
    public NumberSetting offZ = new NumberSetting("OffZ", 0f, -2f, 2f, 0.05f);
    public ViewModel() {
        super("ViewModel", Category.Render);
    }
}