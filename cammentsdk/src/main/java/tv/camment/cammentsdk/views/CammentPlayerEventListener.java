package tv.camment.cammentsdk.views;

import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.helpers.MixpanelHelper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


final class CammentPlayerEventListener implements ExoPlayer.EventListener {

    private final CammentAudioListener cammentAudioListener;
    private final CammentViewHolder cammentViewHolder;
    private final OnResetLastCammentPlayedListener onResetLastCammentPlayedListener;

    CammentPlayerEventListener(CammentAudioListener cammentAudioListener,
                               CammentViewHolder cammentViewHolder,
                               OnResetLastCammentPlayedListener onResetLastCammentPlayedListener) {
        this.cammentAudioListener = cammentAudioListener;
        this.cammentViewHolder = cammentViewHolder;
        this.onResetLastCammentPlayedListener = onResetLastCammentPlayedListener;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        //cammentViewHolder.setThumbnailVisibility(isLoading ? VISIBLE : GONE);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_READY:
                if (cammentAudioListener != null) {
                    MixpanelHelper.getInstance().trackEvent(MixpanelHelper.CAMMENT_PLAY);

                    cammentAudioListener.onCammentPlaybackStarted();
                }
                break;
            case Player.STATE_ENDED:
                if (cammentAudioListener != null) {
                    cammentAudioListener.onCammentPlaybackEnded();
                }
                cammentViewHolder.setItemViewScale(SDKConfig.CAMMENT_SMALL);
                if (onResetLastCammentPlayedListener != null) {
                    onResetLastCammentPlayedListener.resetLastCammentPlayed();
                }
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int i) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e("onPlayerError", "error", error);
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    interface OnResetLastCammentPlayedListener {

        void resetLastCammentPlayed();

    }

}
