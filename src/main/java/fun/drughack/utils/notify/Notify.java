package fun.drughack.utils.notify;

import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.utils.Wrapper;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.math.TimerUtils;
import fun.drughack.utils.render.fonts.Fonts;
import fun.drughack.utils.render.Render2D;
import lombok.Getter;

import java.awt.*;

@Getter
public class Notify implements Wrapper {
    private final NotifyIcon icon;
    private final String notify;
    private final long delay;
    private float y;
    private final Color mainColor;
    private final Color statusColor;
    private final Animation animation = new Animation(300, 1f, true, Easing.BOTH_SINE);
    private final TimerUtils timer = new TimerUtils();

    public Notify(NotifyIcon icon, String notify, long delay, Color mainColor, Color statusColor) {
        this.icon = icon;
        this.notify = notify;
        this.delay = delay;
        this.mainColor = mainColor;
        this.statusColor = statusColor;
        this.y = mc.getWindow().getScaledHeight() / 2f + 10;
        timer.reset();
    }

    public void render(EventRender2D e, float picunY) {
        y = animate(y, picunY);


        String prefix = notify.replace("включен", "").replace("выключен", "");
        String status = notify.contains("включен") ? "включен" :
                notify.contains("выключен") ? "выключен" : "";


        float iconWidth = Fonts.ICONS.getWidth(icon.icon(), 8f);
        float prefixWidth = Fonts.MEDIUM.getWidth(prefix, 9f);
        float statusWidth = Fonts.MEDIUM.getWidth(status, 9f);
        float totalWidth = prefixWidth + (status.isEmpty() ? 0 : statusWidth) + iconWidth + 7f;


        float x = mc.getWindow().getScaledWidth() / 2f - (totalWidth / 2f);

        if (timer.passed(delay)) {
            animation.update(false);
        }


        Render2D.drawStyledRect(
                e.getContext().getMatrices(),
                x - 2.5f, y - 2.5f,
                totalWidth + 5f, 15f, 1.5f,
                new Color(0, 0, 0, (int) (175 * animation.getValue())),
                (int) (255 * animation.getValue())
        );


        Render2D.drawFont(
                e.getContext().getMatrices(),
                Fonts.ICONS.getFont(8f),
                icon.icon(),
                x + 1f, y + 1f,
                new Color(255, 255, 255, (int) (255 * animation.getValue()))
        );


        Render2D.drawFont(
                e.getContext().getMatrices(),
                Fonts.MEDIUM.getFont(9f),
                prefix,
                x + iconWidth + 4f, y - 0.5f,
                new Color(
                        mainColor.getRed(),
                        mainColor.getGreen(),
                        mainColor.getBlue(),
                        (int) (255 * animation.getValue())
                )
        );


        if (!status.isEmpty()) {
            Render2D.drawFont(
                    e.getContext().getMatrices(),
                    Fonts.MEDIUM.getFont(9f),
                    status,
                    x + iconWidth + 4f + prefixWidth, y - 0.5f,
                    new Color(
                            statusColor.getRed(),
                            statusColor.getGreen(),
                            statusColor.getBlue(),
                            (int) (255 * animation.getValue())
                    )
            );
        }
    }

    public float animate(float value, float target) {
        return value + (target - value) / 8f;
    }

    public boolean expired() {
        return timer.passed(delay) && animation.getValue() < 0.01f;
    }
}