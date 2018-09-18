package tv.camment.cammentsdk.views;

import android.util.Log;

import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.exoplayer.ExoPlaybackException;
import tv.camment.cammentsdk.exoplayer.PlaybackParameters;
import tv.camment.cammentsdk.exoplayer.Player;
import tv.camment.cammentsdk.exoplayer.Timeline;
import tv.camment.cammentsdk.exoplayer.source.TrackGroupArray;
import tv.camment.cammentsdk.exoplayer.trackselection.TrackSelectionArray;
import tv.camment.cammentsdk.helpers.MixpanelHelper;


final class CammentPlayerEventListener implements Player.EventListener {

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
    public void onTimelineChanged(Timeline timeline, Object o, int i) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean b) {

    }

    @Override
    public void onPositionDiscontinuity(int i) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

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
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    interface OnResetLastCammentPlayedListener {

        void resetLastCammentPlayed();

    }

}
