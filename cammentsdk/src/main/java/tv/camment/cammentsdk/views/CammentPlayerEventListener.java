package tv.camment.cammentsdk.views;

import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


class CammentPlayerEventListener implements ExoPlayer.EventListener {

    private final CammentAudioListener cammentAudioListener;
    private final CammentViewHolder cammentViewHolder;

    CammentPlayerEventListener(CammentAudioListener cammentAudioListener,
                               CammentViewHolder cammentViewHolder) {
        this.cammentAudioListener = cammentAudioListener;
        this.cammentViewHolder = cammentViewHolder;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        cammentViewHolder.setThumbnailVisibility(isLoading ? VISIBLE : GONE);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_READY:
                if (cammentAudioListener != null) {
                    cammentAudioListener.onCammentPlaybackStarted();
                }
                break;
            case ExoPlayer.STATE_ENDED:
                if (cammentAudioListener != null) {
                    cammentAudioListener.onCammentPlaybackEnded();
                }
                cammentViewHolder.setItemViewScale(0.5f);
                break;
        }
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

}
