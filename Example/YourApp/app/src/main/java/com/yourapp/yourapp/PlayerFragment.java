package com.yourapp.yourapp;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.ShowMetadata;
import tv.camment.cammentsdk.views.CammentAudioListener;
import tv.camment.cammentsdk.views.CammentOverlay;

import static com.google.android.exoplayer2.Player.REPEAT_MODE_ALL;

public class PlayerFragment extends Fragment implements CammentAudioListener {

    private static final String EXTRA_SHOW_UUID = "extra_show_uuid";

    private SimpleExoPlayer player;
    private SimpleExoPlayerView showPlayerView;
    private float previousVolume;

    public static PlayerFragment getInstance(String showUuid) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SHOW_UUID, showUuid);
        fragment.setArguments(bundle);
        return fragment;
    }

    public PlayerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_player, container, false);

        // Don't forget to set new show metadata, if this can happen in your application
        // If second parameter is empty, default text "Hey, join our private chat!" will be used when sending invitation link - you can pass own text here
        CammentSDK.getInstance().setShowMetadata(new ShowMetadata(getArguments().getString(EXTRA_SHOW_UUID), null));

        showPlayerView = layout.findViewById(R.id.showPlayerView);

        CammentOverlay cammentOverlay = layout.findViewById(R.id.cammentOverlay);
        cammentOverlay.setParentViewGroup(showPlayerView);

        CammentSDK.getInstance().setCammentAudioListener(this);

        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareAndPlayVideo();
    }

    private void prepareAndPlayVideo() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);

        showPlayerView.setPlayer(player);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), Util.getUserAgent(getActivity(), getString(R.string.app_name)));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // Uri for player should be passed according to current showUuid
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse("http://techslides.com/demos/sample-videos/small.mp4"), dataSourceFactory, extractorsFactory, null, null);
        player.setPlayWhenReady(true);
        player.setRepeatMode(REPEAT_MODE_ALL);
        player.prepare(videoSource);
    }

    @Override
    public void onCammentPlaybackStarted() {
        previousVolume = player.getVolume();
        player.setVolume(previousVolume * 0.6f);
    }

    @Override
    public void onCammentPlaybackEnded() {
        player.setVolume(previousVolume);
    }

    @Override
    public void onCammentRecordingStarted() {
        previousVolume = player.getVolume();
        player.setVolume(previousVolume * 0.3f);
    }

    @Override
    public void onCammentRecordingEnded() {
        player.setVolume(previousVolume);
    }

}
