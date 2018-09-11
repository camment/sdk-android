package com.yourapp.yourapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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

public class MainActivity extends BaseActivity
        implements CammentAudioListener {

    private static final String EXTRA_SHOW_UUID = "extra_show_uuid";

    private SimpleExoPlayer player;
    private SimpleExoPlayerView showPlayerView;
    private float previousVolume;

    public static void start(Context context, String showUuid) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_SHOW_UUID, showUuid);
        context.startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);

        Intent oldIntent = getIntent();

        if (!TextUtils.equals(oldIntent.getStringExtra(EXTRA_SHOW_UUID), newIntent.getStringExtra(EXTRA_SHOW_UUID))) {
            setIntent(newIntent);

            // Activity is started with new intent, don't forget to set new show metadata, if this can happen in your application
            CammentSDK.getInstance().setShowMetadata(new ShowMetadata(getIntent().getStringExtra(EXTRA_SHOW_UUID), null));

            if (player != null) {
                player.stop();
                player.release();
                player = null;
            }
            prepareAndPlayVideo();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Don't forget to set new show metadata, if this can happen in your application
        // If second parameter is empty, default text "Hey, join our private chat!" will be used when sending invitation link - you can pass own text here
        CammentSDK.getInstance().setShowMetadata(new ShowMetadata(getIntent().getStringExtra(EXTRA_SHOW_UUID), null));

        showPlayerView = findViewById(R.id.showPlayerView);

        CammentOverlay cammentOverlay = findViewById(R.id.cammentOverlay);
        cammentOverlay.setParentViewGroup(showPlayerView);
        cammentOverlay.setRecordButtonMarginBottom(56);

        CammentSDK.getInstance().setCammentAudioListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepareAndPlayVideo();
    }

    private void prepareAndPlayVideo() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        showPlayerView.setPlayer(player);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // Uri for player should be passed according to current showUuid
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse("http://techslides.com/demos/sample-videos/small.mp4"), dataSourceFactory, extractorsFactory, null, null);
        player.setPlayWhenReady(true);
        player.setRepeatMode(REPEAT_MODE_ALL);
        player.prepare(videoSource);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //IMPORTANT to pass camera and microphone permission to CammentSDK
        CammentSDK.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
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
