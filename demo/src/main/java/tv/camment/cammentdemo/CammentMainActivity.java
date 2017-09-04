package tv.camment.cammentdemo;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.data.ShowProvider;
import tv.camment.cammentsdk.views.CammentAudioListener;
import tv.camment.cammentsdk.views.CammentOverlay;

public class CammentMainActivity extends AppCompatActivity
        implements CammentAudioListener {

    private static final String EXTRA_SHOW_UUID = "extra_show_uuid";

    private static final String ARGS_PLAYER_POSITION = "args_player_position";

    private VideoView videoView;
    private int previousVolume;
    private MediaController mediaController;
    private int currentPosition;

    public static void start(Context context, String showUuid) {
        Intent intent = new Intent(context, CammentMainActivity.class);
        intent.putExtra(EXTRA_SHOW_UUID, showUuid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camment_activity_main);

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(ARGS_PLAYER_POSITION);
        }

        CammentSDK.getInstance().setShowUuid(getIntent().getStringExtra(EXTRA_SHOW_UUID));

        videoView = (VideoView) findViewById(R.id.show_player);

        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        FrameLayout parentViewGroup = (FrameLayout) findViewById(R.id.fl_parent);

        CammentOverlay cammentOverlay = (CammentOverlay) findViewById(R.id.camment_overlay);

        cammentOverlay.setParentViewGroup(parentViewGroup);
        cammentOverlay.setCammentAudioListener(this);

        prepareAndPlayVideo(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARGS_PLAYER_POSITION, currentPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        if (videoView != null) {
            currentPosition = videoView.getCurrentPosition();
            videoView.stopPlayback();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null
                && mediaController != null) {
            prepareAndPlayVideo(false);
        }
    }

    private void prepareAndPlayVideo(boolean start) {
        if (videoView != null
                && mediaController != null) {
            Uri uri = Uri.parse(ShowProvider.getShowByUuid(getIntent().getStringExtra(EXTRA_SHOW_UUID)).getUrl());
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(uri);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setLooping(true);
                }
            });
            videoView.seekTo(currentPosition);
            if (start) {
                videoView.start();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CammentSDK.getInstance().onActivityResult(requestCode, resultCode, data, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        CammentSDK.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCammentPlaybackStarted() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        previousVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        am.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume / 2, 0);
    }

    @Override
    public void onCammentPlaybackEnded() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
    }

    @Override
    public void onCammentRecordingStarted() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        previousVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    }

    @Override
    public void onCammentRecordingEnded() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.camment_slide_in_left, R.anim.camment_slide_out_right);
    }

}
