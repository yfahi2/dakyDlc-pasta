package fun.drughack.hud.impl;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.hud.HudElement;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.animations.infinity.InfinityAnimation;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.mediaplayer.MediaPlayer;
import fun.drughack.utils.network.Server;
import fun.drughack.utils.render.ColorUtils;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.resource.language.I18n;

import java.awt.*;



import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.hud.HudElement;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import meteordevelopment.orbit.EventHandler;
import java.awt.*;


import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.hud.HudElement;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class Music extends HudElement {

    private final Animation mediaAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);
    private boolean initialized = false;
    private long lastUpdateTime = 0;

    public Music() {
        super("Music");
    }

    @EventHandler
    public void onTick(EventTick e) {
        MediaPlayer mediaPlayer = DrugHack.getInstance().getMediaPlayer();
        if (Module.fullNullCheck()) return;
        DrugHack.getInstance().getMediaPlayer().onTick();
        String progress = formatTime(mediaPlayer.getPosition()) + " / " + formatTime(mediaPlayer.getDuration());

    }
    private String formatTime(long millis) {
        if (millis <= 0) return "00:00";
        long seconds = millis;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
    public void togglePlayPause() {
        MediaPlayer mediaPlayer = DrugHack.getInstance().getMediaPlayer();
        if (mediaPlayer.getSession() != null) {
            mediaPlayer.getSession().playPause();
        }
    }
    @Override
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck() || closed()) return;

        MediaPlayer mediaPlayer = DrugHack.getInstance().getMediaPlayer();
        String track = mediaPlayer.getLastTitle() +
                (mediaPlayer.getArtist().isEmpty() ? "" : " - " + mediaPlayer.getArtist());

        boolean mediaNull = mediaPlayer.fullNullCheck();
        mediaAnimation.update(!mediaNull);


        track = track.length() > 21 ? track.substring(0, 21) + "..." : track;

        float padding = 2f;
        float round = 6f;
        float width = 15 + Fonts.BOLD.getWidth(mediaNull ? "Music" : track, 7f) + padding * 9f;
        float height = 15f;

        if (!initialized) {
            float initX = mc.getWindow().getScaledWidth() / 2f - width / 2f;
            float initY = 4f;
            setBounds(initX, initY, width * 3, height * 3);
            initialized = true;
        }

        float x = getX();
        float y = getY();

        Render2D.startScissor(e.getContext(), x, y, width * 2, height * 3);

        if (!mediaNull) {
            Render2D.drawStyledRect(
                    e.getContext().getMatrices(),
                    x,
                    y,
                    width,
                    height * 3,
                    round,
                    new Color(0, 0, 0, 200),
                    255
            );


            Render2D.drawTexture(
                    e.getContext().getMatrices(),
                    x + padding,
                    y + padding + 5.5f,
                    height * 2,
                    height * 2,
                    4f,
                    DrugHack.getInstance().getMediaPlayer().getTexture(),
                    new Color(255, 255, 255, (int) (255 * mediaAnimation.getValue()))
            );


            Render2D.drawFont(e.getContext().getMatrices(),
                    Fonts.BOLD.getFont(7f),
                    track,
                    x + height * 2.2f,
                    y + height - 3 - Fonts.BOLD.getHeight(7f) / 2f,
                    new Color(255, 255, 255, (int) (255 * mediaAnimation.getValue()))
            );


            long currentTime = System.currentTimeMillis();
            long timePassed = currentTime - lastUpdateTime;
            long currentPosition = mediaPlayer.getPosition();
            lastUpdateTime = currentTime;




            Render2D.drawFont(e.getContext().getMatrices(),
                    Fonts.BOLD.getFont(7f),
                    mediaPlayer.getProgress(),
                    x + height * 2.2f,
                    y + height + 7 - Fonts.BOLD.getHeight(7f) / 2f,
                    new Color(255, 255, 255, (int) (255 * mediaAnimation.getValue())));



            float progressBarWidth = width - height * 2.2f - padding * 2;
            float progressBarHeight = 1.5f;
            float progressBarX = x + height * 2.2f;
            float progressBarY = y + height * 1.8f;


            Render2D.drawRoundedRect(
                    e.getContext().getMatrices(),
                    progressBarX,
                    progressBarY,
                    progressBarWidth,
                    (float) (progressBarHeight + 0.5),1,
                    new Color(100, 100, 100, 150)
            );


            float progressPercent = mediaPlayer.getDuration() > 0 ?
                    (float) mediaPlayer.getPosition() / mediaPlayer.getDuration() : 0;
            progressPercent = MathHelper.clamp(progressPercent, 0, 1);

            Render2D.drawRoundedRect(
                    e.getContext().getMatrices(),
                    progressBarX,
                    progressBarY,
                    progressBarWidth * progressPercent,
                    (float) (progressBarHeight + 0.5),1,
                    new Color(30, 215, 96, 255));
        }

        Render2D.stopScissor(e.getContext());
        super.onRender2D(e);
    }
}