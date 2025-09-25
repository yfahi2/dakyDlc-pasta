package fun.drughack.utils.render.fonts;

import fun.drughack.api.render.msdf.MsdfFont;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Fonts {
    public Font BOLD, MEDIUM, REGULAR, SEMIBOLD, ICONS;

    static {
        BOLD = new Font(MsdfFont.builder().atlas("sf_bold").data("sf_bold").build());
        MEDIUM = new Font(MsdfFont.builder().atlas("sf_medium").data("sf_medium").build());
        REGULAR = new Font(MsdfFont.builder().atlas("sf_regular").data("sf_regular").build());
        SEMIBOLD = new Font(MsdfFont.builder().atlas("sf_semibold").data("sf_semibold").build());
        ICONS = new Font(MsdfFont.builder().atlas("icons").data("icons").build());
    }
}