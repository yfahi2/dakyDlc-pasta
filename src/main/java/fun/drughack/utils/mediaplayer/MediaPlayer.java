package fun.drughack.utils.mediaplayer;

import dev.redstones.mediaplayerinfo.*;
import fun.drughack.utils.render.Render2D;
import lombok.*;
import net.minecraft.client.texture.AbstractTexture;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter @Setter
public class MediaPlayer {

    private BufferedImage image;
    private AbstractTexture texture;
    private String title = "", artist = "", owner = "", lastTitle = "", progress = "00:00 / 00:00";
    private long duration = 0, position = 0;
    private boolean changeTrack;
    private IMediaSession session;
    private List<IMediaSession> sessions;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void onTick() {
        executor.submit(() -> {
            sessions = MediaPlayerInfo.Instance.getMediaSessions();
            session = sessions.stream()
                    .filter(s -> (!s.getMedia().getArtist().isEmpty() || !s.getMedia().getTitle().isEmpty()))
                    .findFirst()
                    .orElse(null);

            if (session == null) {
                title = "";
                lastTitle = "";
                artist = "";
                owner = "";
                image = null;
                progress = "00:00 / 00:00";

                if (texture != null) {
                    texture.close();
                    texture = null;
                }
                return;
            }

            MediaInfo info = session.getMedia();
            title = info.getTitle();
            artist = info.getArtist();
            duration = info.getDuration();
            position = info.getPosition();
            image = info.getArtwork();
            owner = session.getOwner();


            progress = formatTime(position) + " / " + formatTime(duration);

            boolean trackChanged = lastTitle == null || !lastTitle.equals(title);
            if (trackChanged) {
                changeTrack = true;
                lastTitle = title;

                progress = formatTime(position) + " / " + formatTime(duration);
            }

            if (changeTrack || trackChanged || (image != null && texture == null)) {
                if (image != null) {
                    if (texture != null) {
                        texture.close();
                    }
                    texture = Render2D.convert(image);
                }
                changeTrack = false;
            }
        });
    }


    private String formatTime(long millis) {

        long seconds = millis;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
    public boolean fullNullCheck() {
        return session == null || texture == null || lastTitle.isEmpty();
    }
}