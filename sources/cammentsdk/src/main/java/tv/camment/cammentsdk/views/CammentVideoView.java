package tv.camment.cammentsdk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import tv.camment.cammentsdk.CammentSDK;

public final class CammentVideoView extends VideoView {

    private boolean mediaPlayerPrepared = false;
    private boolean isPlaying = false;

    public CammentVideoView(Context context) {
        super(context);
    }

    public CammentVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CammentVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMediaPlayerPrepared(boolean mediaPlayerPrepared) {
        this.mediaPlayerPrepared = mediaPlayerPrepared;
    }

    @Override
    public void start() {
        super.start();

        isPlaying = true;

        if (mediaPlayerPrepared) {
            CammentSDK.getInstance().onPlaybackStarted(getCurrentPosition());
        }
    }

    @Override
    public void pause() {
        super.pause();

        isPlaying = false;

        if (mediaPlayerPrepared) {
            CammentSDK.getInstance().onPlaybackPaused(getCurrentPosition());
        }
    }

    @Override
    public void seekTo(int msec) {
        super.seekTo(msec);

        if (mediaPlayerPrepared) {
            CammentSDK.getInstance().onPlaybackPositionChanged(getCurrentPosition(), isPlaying() || isPlaying);
        }
    }

}
