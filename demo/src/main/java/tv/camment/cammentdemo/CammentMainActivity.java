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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.camment.clientsdk.model.Show;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.ShowMetadata;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.data.ShowProvider;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.utils.DateTimeUtils;
import tv.camment.cammentsdk.views.CammentAudioListener;
import tv.camment.cammentsdk.views.CammentOverlay;

public class CammentMainActivity extends CammentBaseActivity
        implements CammentAudioListener, MediaPlayer.OnPreparedListener {

    private static final long TEST_TIMESTAMP = 1507713480000L;

    private static final String EXTRA_SHOW_UUID = "extra_show_uuid";

    private static final String ARGS_PLAYER_POSITION = "args_player_position";

    private VideoView videoView;
    private int previousVolume = -1;
    private MediaController mediaController;
    private int currentPosition;

    private TextView tvShowOnHold;

    private ContentLoadingProgressBar contentLoadingProgressBar;
    private MediaPlayer mediaPlayer;

    private BroadcastReceiver broadcastReceiver;

    private boolean playerReady;

    private Show show;

    private Mode mode = Mode.UNDEFINED;

    private enum Mode {
        NORMAL,
        BLOCK,
        SYNC,
        UNDEFINED
    }

    public static void start(Context context, String showUuid) {
        Intent intent = new Intent(context, CammentMainActivity.class);
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

            if (contentLoadingProgressBar != null) {
                contentLoadingProgressBar.show();
            }

            CammentSDK.getInstance().setShowMetadata(new ShowMetadata(getIntent().getStringExtra(EXTRA_SHOW_UUID), null));

            currentPosition = 0;
            if (videoView != null) {
                videoView.stopPlayback();
                videoView.suspend();
            }
            retrieveShow();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camment_activity_main);

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(ARGS_PLAYER_POSITION);
        }

        CammentSDK.getInstance().setShowMetadata(new ShowMetadata(getIntent().getStringExtra(EXTRA_SHOW_UUID), null));

        tvShowOnHold = (TextView) findViewById(R.id.tv_on_hold);

        contentLoadingProgressBar = (ContentLoadingProgressBar) findViewById(R.id.cl_progressbar);
        contentLoadingProgressBar.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(android.R.color.holo_blue_dark),
                        PorterDuff.Mode.SRC_IN);
        videoView = (VideoView) findViewById(R.id.show_player);

        FrameLayout parentViewGroup = (FrameLayout) findViewById(R.id.fl_parent);

        CammentOverlay cammentOverlay = (CammentOverlay) findViewById(R.id.camment_overlay);

        cammentOverlay.setParentViewGroup(parentViewGroup);
        cammentOverlay.setCammentAudioListener(this);

        retrieveShow();

        MixpanelHelper.getInstance().trackEvent(MixpanelHelper.SHOW_SCREEN);
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
            getIntent().putExtra(ARGS_PLAYER_POSITION, currentPosition);
        }
        super.onPause();
    }


    @Override
    protected void onStop() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
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
            retrieveShow();
        }
    }

    //#KROK1
    private void retrieveShow() {
        show = ShowProvider.getShowByUuid(getIntent().getStringExtra(EXTRA_SHOW_UUID));
        if (show != null) {
//            if (TextUtils.equals(show.getUuid(), "f202de7a-b3e0-46e5-895a-aa612e261575")) {
//                show.setStartAt(BigDecimal.valueOf(TEST_TIMESTAMP));
//            }
            //#KROK2
            Log.d("SHOW", "retrieved from db");
            checkTimestamp();
        } else {
            Log.d("SHOW", "retrieving from server");
            ApiManager.getInstance().getShowApi()
                    .getShowByUuid(getIntent().getStringExtra(EXTRA_SHOW_UUID), getShowByUuidCallback());
        }
    }

    //#KROK2
    private void checkTimestamp() {
        final long showTimestamp = show.getStartAt() != null ? show.getStartAt().longValue() : -1;
        final long currentUTCTimestamp = DateTimeUtils.getCurrentUTCTimestamp();

        if (showTimestamp == -1
                || (showTimestamp <= currentUTCTimestamp
                && (currentUTCTimestamp - showTimestamp) >= 60 * 60 * 1000)) { //#KROK2a & //#KROK2d
            mode = Mode.NORMAL;
        } else if (showTimestamp > currentUTCTimestamp) { //#KROK2b
            mode = Mode.BLOCK;
        } else if (showTimestamp <= currentUTCTimestamp
                && (currentUTCTimestamp - showTimestamp) < 60 * 60 * 1000) { //#KROK2c
            mode = Mode.SYNC;
        }

        Log.d("MODE", mode.name());
        handleTimeTickBroadcastReceiver();
        handleBlockingOfUI();
        handleMediaController();
        shouldSyncUser();
        prepareAndPlayVideo();
    }

    private void handleTimeTickBroadcastReceiver() {
        if (mode == Mode.BLOCK) {
            registerTimeTickBroadcastReceiver();
        } else {
            unregisterTimeTickBroadcastReceiver();
        }
    }

    private void handleBlockingOfUI() {
        tvShowOnHold.setText(mode == Mode.BLOCK ? "Show starts at " + DateTimeUtils.getTimeOnlyStringForUI(show.getStartAt().longValue()) : "");
        tvShowOnHold.setVisibility(mode == Mode.BLOCK ? View.VISIBLE : View.GONE);
    }

    private void handleMediaController() {
        mediaController = new MediaController(this, mode != Mode.SYNC) {
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    onBackPressed();
                }

                return super.dispatchKeyEvent(event);
            }
        };

        mediaController.setAnchorView(videoView);

        int topContainerId = getResources().getIdentifier("mediacontroller_progress", "id", "android");
        SeekBar seekBarVideo = (SeekBar) mediaController.findViewById(topContainerId);
        seekBarVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mode == Mode.SYNC;
            }
        });
    }


    private void registerTimeTickBroadcastReceiver() {
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_TICK)) {
                        Log.d("TIME TICK", "time tick");
                        retrieveShow();
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    private void unregisterTimeTickBroadcastReceiver() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    private void shouldSyncUser() {
        if (mode == Mode.SYNC) {
            Log.d("SYNC", "syncing...");
            currentPosition = (int) (DateTimeUtils.getCurrentUTCTimestamp() - show.getStartAt().longValue());
        }
    }


    private void prepareAndPlayVideo() {
        if (mode == Mode.BLOCK)
            return;

        if (videoView != null
                && mediaController != null) {
            if (show != null) {
                Uri uri = Uri.parse(show.getUrl());
                videoView.setMediaController(mode == Mode.SYNC ? null : mediaController);
                videoView.setVideoURI(uri);
                videoView.setOnPreparedListener(this);

                if (currentPosition == 0) {
                    currentPosition = getIntent().getIntExtra(ARGS_PLAYER_POSITION, 0);
                }

                videoView.seekTo(currentPosition);
                videoView.start();
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        playerReady = true;
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setLooping(mode != Mode.SYNC);

        if (contentLoadingProgressBar != null) {
            contentLoadingProgressBar.hide();
        }

        if (mode == Mode.SYNC) {
            if (previousVolume > -1) {
                setVolume(previousVolume);
            }
        }
    }


    private CammentCallback<Show> getShowByUuidCallback() {
        return new CammentCallback<Show>() {
            @Override
            public void onSuccess(Show result) {
                if (result != null
                        && !TextUtils.isEmpty(result.getUrl())) {
//                    if (TextUtils.equals(result.getUuid(), "f202de7a-b3e0-46e5-895a-aa612e261575")) {
//                        result.setStartAt(BigDecimal.valueOf(TEST_TIMESTAMP));
//                    }

                    show = result;
                    //#KROK2
                    checkTimestamp();
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getShows", exception);
            }
        };
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

        setVolume((int) (previousVolume * 0.6f));
    }

    @Override
    public void onCammentPlaybackEnded() {
        setVolume(previousVolume);
    }

    @Override
    public void onCammentRecordingStarted() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        previousVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        setVolume((int) (previousVolume * 0.3f));
    }

    @Override
    public void onCammentRecordingEnded() {
        setVolume(previousVolume);
    }

    @Override
    public void onBackPressed() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goBack();
            }
        }, 150);
    }

    private void goBack() {
        super.onBackPressed();

        overridePendingTransition(R.anim.camment_slide_in_left, R.anim.camment_slide_out_right);
    }


    private void setVolume(int amount) {
        if (mediaPlayer != null
                && playerReady) {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, amount, 0);
        }
    }

}