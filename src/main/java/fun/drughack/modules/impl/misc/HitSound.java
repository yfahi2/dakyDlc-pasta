package fun.drughack.modules.impl.misc;

import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.ListSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import org.jetbrains.annotations.NotNull;

public class HitSound extends Module {

    private final @NotNull BooleanSetting bell = new BooleanSetting("bell", true, () -> false);
    private final @NotNull BooleanSetting crime = new BooleanSetting("crime", false, () -> false);
    private final @NotNull BooleanSetting nya = new BooleanSetting("nya", false, () -> false);
    private final @NotNull BooleanSetting skeet = new BooleanSetting("skeet", false, () -> false);
    private final @NotNull BooleanSetting uwu = new BooleanSetting("uwu", false, () -> false);
    private final @NotNull NumberSetting Volume = new NumberSetting(
            "Volume",
            1.00f,   // стандартное 16:9
            0.1f,     // минимально
            2.0f,     // максимально
            0.01f     // шаг
    );
    private final @NotNull ListSetting mode = new ListSetting(
            "Sound",
            bell, crime, nya, skeet, uwu
    );

    public HitSound() {
        super("HitSound", Category.Misc);
    }

    public @NotNull String getSelectedSound() {
        if (crime.getValue()) return "simplevisuals:crime";
        if (nya.getValue()) return "simplevisuals:nya";
        if (skeet.getValue()) return "simplevisuals:skeet";
        if (uwu.getValue()) return "simplevisuals:uwu";
        return "simplevisuals:bell"; // дефолт
    }
    public @NotNull NumberSetting getVolume() {
        return Volume;
    }
}
