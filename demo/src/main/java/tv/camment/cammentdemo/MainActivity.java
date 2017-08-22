package tv.camment.cammentdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
import tv.camment.cammentsdk.views.CammentAudioListener;
import tv.camment.cammentsdk.views.CammentOverlay;

public class MainActivity extends AppCompatActivity implements CammentAudioListener {

    private SimpleExoPlayer player;
    private SimpleExoPlayerView showPlayerView;
    private CammentOverlay cammentOverlay;
    private float previousVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CammentSDK.getInstance().setShowUuid("df64bc2e-7b76-11e7-bb31-be2e44b06b34");

        showPlayerView = (SimpleExoPlayerView) findViewById(R.id.showPlayerView);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        showPlayerView.setPlayer(player);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Camment"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse("https://dl3zp6ge83i42.cloudfront.net/camment-app-shows/dc54f691-4af7-49cc-8352-4cd080e4e948.mp4"), dataSourceFactory, extractorsFactory, null, null);
        player.setPlayWhenReady(true);
        player.prepare(videoSource);

        cammentOverlay = (CammentOverlay) findViewById(R.id.cammentOverlay);

        cammentOverlay.setParentViewGroup(showPlayerView);
        cammentOverlay.setCammentAudioListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "onActivityResult");

        CammentSDK.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        CammentSDK.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCammentPlaybackStarted() {
        Log.d("main", "onCammentPlaybackStarted");
        previousVolume = player.getVolume();
        player.setVolume(previousVolume / 2);
    }

    @Override
    public void onCammentPlaybackEnded() {
        Log.d("main", "onCammentPlaybackEnded");
        player.setVolume(previousVolume);
    }

    @Override
    public void onCammentRecordingStarted() {
        Log.d("main", "onCammentRecordingStarted " + player.getVolume());
        previousVolume = player.getVolume();
        player.setVolume(0.0f);
    }

    @Override
    public void onCammentRecordingEnded() {
        Log.d("main", "onCammentRecordingEnded " + previousVolume);
        player.setVolume(previousVolume);
    }

}
