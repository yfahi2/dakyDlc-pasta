package fun.drughack.utils.render.fonts;

import fun.drughack.api.render.msdf.MsdfFont;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Fonts {
    public Font BOLD, MEDIUM, REGULAR, SEMIBOLD, SFPROSTEXT,SFPD,nur,ICONS;

    static {
        BOLD = new Font(MsdfFont.builder().atlas("sf_bold").data("sf_bold").build());
        MEDIUM = new Font(MsdfFont.builder().atlas("sf_medium").data("sf_medium").build());
        REGULAR = new Font(MsdfFont.builder().atlas("sf_regular").data("sf_regular").build());
        SEMIBOLD = new Font(MsdfFont.builder().atlas("sf_semibold").data("sf_semibold").build());
        SFPROSTEXT = new Font(MsdfFont.builder().atlas("sfprotext").data("sfprotext").build());
        nur = new Font(MsdfFont.builder().atlas("nursultan").data("nursultan").build());
        SFPD = new Font(MsdfFont.builder().atlas("sfdr").data("sfdr").build());
        ICONS = new Font(MsdfFont.builder().atlas("icons").data("icons").build());
    }
}