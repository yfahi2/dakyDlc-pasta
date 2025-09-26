package fun.drughack.modules.impl.render;

import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.impl.NumberSetting;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class AspectRatio extends Module {

    private final @NotNull NumberSetting aspectRatio = new NumberSetting(
            "Aspect Ratio",
            1.777f,   // стандартное 16:9
            0.5f,     // минимально
            3.0f,     // максимально
            0.01f     // шаг
    );

    public AspectRatio() {
        super("AspectRatio", Category.Render);
    }
}