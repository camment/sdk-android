package tv.camment.cammentdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.OnDeeplinkGetListener;
import tv.camment.cammentsdk.data.ShowProvider;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.utils.DateTimeUtils;
import tv.camment.cammentsdk.views.CammentAudioListener;
import tv.camment.cammentsdk.views.CammentOverlay;

public class CammentMainActivity extends AppCompatActivity
        implements CammentAudioListener,
        OnDeeplinkGetListener, MediaPlayer.OnPreparedListener {

    private static final long START_TIMESTAMP = 1506621600000L;

    private static final String PASSCODE = "test";

    private static final String EXTRA_SHOW_UUID = "extra_show_uuid";

    private static final String ARGS_PLAYER_POSITION = "args_player_position";

    private VideoView videoView;
    private int previousVolume;
    private MediaController mediaController;
    private int currentPosition;

    private TextView tvShowOnHold;

    private ContentLoadingProgressBar contentLoadingProgressBar;
    private MediaPlayer mediaPlayer;

    private BroadcastReceiver broadcastReceiver;

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

        boolean showStarted = didShowStart();

        tvShowOnHold = (TextView) findViewById(R.id.tv_on_hold);

        if (isTestPasscode()) {
            tvShowOnHold.setText("Show starts at " + DateTimeUtils.getTimeOnlyStringForUI(START_TIMESTAMP));
            tvShowOnHold.setVisibility(showStarted ? View.GONE : View.VISIBLE);
        }

        contentLoadingProgressBar = (ContentLoadingProgressBar) findViewById(R.id.cl_progressbar);
        contentLoadingProgressBar.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(android.R.color.holo_blue_dark),
                        PorterDuff.Mode.SRC_IN);
        videoView = (VideoView) findViewById(R.id.show_player);

        mediaController = new MediaController(this, !isTestPasscode()) {
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    onBackPressed();
                }

                return super.dispatchKeyEvent(event);
            }
        };
        mediaController.setAnchorView(videoView);

        FrameLayout parentViewGroup = (FrameLayout) findViewById(R.id.fl_parent);

        CammentOverlay cammentOverlay = (CammentOverlay) findViewById(R.id.camment_overlay);

        cammentOverlay.setParentViewGroup(parentViewGroup);
        cammentOverlay.setCammentAudioListener(this);

        prepareAndPlayVideo(!isTestPasscode() || showStarted);
    }

    private boolean didShowStart() {
        long currentUTCTimestamp = DateTimeUtils.getCurrentUTCTimestamp();

        return currentUTCTimestamp >= START_TIMESTAMP;
    }

    private boolean isTestPasscode() {
        return TextUtils.equals(PASSCODE, GeneralPreferences.getInstance().getProviderPasscode());
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
        }
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isTestPasscode()) {
            if (broadcastReceiver == null) {
                broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_TICK)) {
                            Log.d("TIME TICK", "time tick");
                            if (didShowStart()) {
                                startShow();
                            }
                        }
                    }
                };
            }
            registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        }
    }

    @Override
    protected void onStop() {
        if (isTestPasscode()
                && broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        if (videoView != null) {
            currentPosition = videoView.getCurrentPosition();
            videoView.stopPlayback();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null
                && !videoView.isPlaying()
                && mediaController != null) {
            if (isTestPasscode()) {
                boolean showStarted = didShowStart();
                tvShowOnHold.setVisibility(showStarted ? View.GONE : View.VISIBLE);
                prepareAndPlayVideo(showStarted);
            } else {
                prepareAndPlayVideo(true);
            }
        }
    }

    private void prepareAndPlayVideo(boolean start) {
        if (videoView != null
                && mediaController != null) {
            Uri uri = Uri.parse(ShowProvider.getShowByUuid(getIntent().getStringExtra(EXTRA_SHOW_UUID)).getUrl());
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(uri);
            videoView.setOnPreparedListener(this);
            videoView.seekTo(currentPosition);
            if (start) {
                videoView.start();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CammentSDK.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        CammentSDK.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCammentPlaybackStarted() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        previousVolume = (int) Math.floor(am.getStreamVolume(AudioManager.STREAM_MUSIC) * 20 / 3);

        setVolume(previousVolume / 2);
    }

    @Override
    public void onCammentPlaybackEnded() {
        setVolume(previousVolume);
    }

    @Override
    public void onCammentRecordingStarted() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        previousVolume = (int) Math.floor(am.getStreamVolume(AudioManager.STREAM_MUSIC) * 20 / 3);

        setVolume(0);
    }

    @Override
    public void onCammentRecordingEnded() {
        setVolume(previousVolume);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.camment_slide_in_left, R.anim.camment_slide_out_right);
    }

    @Override
    public void onDeeplinkGetStarted() {
        if (contentLoadingProgressBar != null) {
            contentLoadingProgressBar.show();
        }
    }

    @Override
    public void onDeeplinkGetEnded() {
        if (contentLoadingProgressBar != null) {
            contentLoadingProgressBar.hide();
        }
    }

    private void setVolume(int amount) {
        final int max = 100;
        final double numerator = max - amount > 0 ? Math.log(max - amount) : 0;
        final float volume = (float) (1 - (numerator / Math.log(max)));

        mediaPlayer.setVolume(volume, volume);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        mediaPlayer.setLooping(true);

        if (isTestPasscode()) {
            Log.d("SYNC", "sync");

            syncUser();

            int topContainerId = getResources().getIdentifier("mediacontroller_progress", "id", "android");
            SeekBar seekBarVideo = (SeekBar) mediaController.findViewById(topContainerId);
            seekBarVideo.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });
        }
    }

    private void syncUser() {
        Log.d("SYNC", "syncing...");
        int seekTo = (int) (DateTimeUtils.getCurrentUTCTimestamp() - START_TIMESTAMP);

        videoView.seekTo(seekTo);
    }

    private void startShow() {
        if (videoView != null) {
            videoView.seekTo(currentPosition);
            videoView.start();
        }
    }

}
