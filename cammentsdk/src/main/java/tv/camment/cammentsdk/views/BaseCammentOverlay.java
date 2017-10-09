package tv.camment.cammentsdk.views;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.InitializationException;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.MobileAnalyticsManager;
import com.camment.clientsdk.model.Camment;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.camera.CameraGLView;
import tv.camment.cammentsdk.camera.RecordingHandler;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.helpers.OnboardingPreferences;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.helpers.Step;
import tv.camment.cammentsdk.utils.CommonUtils;
import tv.camment.cammentsdk.utils.FileUtils;

abstract class BaseCammentOverlay extends RelativeLayout
        implements
        CammentsAdapter.ActionListener,
        RecordingButton.ActionsListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        OnPreviewStartedListener,
        CammentPlayerEventListener.OnResetLastCammentPlayedListener {

    private static final int THRESHOLD_X = 100;
    private static final int THRESHOLD_Y = 150;

    private float startX;
    private float startY;

    ViewGroup parentViewGroup;
    CammentAudioListener cammentAudioListener;

    private SquareFrameLayout flCamera;
    private CameraGLView cameraGLView;
    private View vRecordIndicator;
    private CammentRecyclerView rvCamments;
    private RecordingButton ibRecord;
    private OnboardingOverlay onboardingOverlay;
    private FrameLayout flDrawer;

    private CammentsAdapter adapter;

    private SimpleExoPlayer player;
    private DefaultDataSourceFactory dataSourceFactory;
    private DefaultExtractorsFactory extractorsFactory;

    private RecordingHandler recordingHandler;

    private ExoPlayer.EventListener exoEventListener;
    private String lastVideoCammentUuid;

    private static MobileAnalyticsManager analytics;

    private ProfileTracker profileTracker;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return CammentProvider.getCammentLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<CCamment> camments = CammentProvider.listFromCursor(data);
        adapter.setData(camments);

        if (camments != null) {
            if (camments.size() == 1
                    && !OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.PLAY)) {
                onboardingOverlay.displayTooltip(Step.PLAY);
            } else if (camments.size() > 0
                    && OnboardingPreferences.getInstance().isOnboardingStepLastRemaining(Step.DELETE)
                    && !OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.DELETE)) {
                onboardingOverlay.displayTooltip(Step.DELETE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private enum Mode {
        GOING_BACK,
        HIDE,
        SHOW,
        NONE
    }

    private Mode mode = Mode.NONE;

    BaseCammentOverlay(Context context) {
        super(context);
        init(context);
    }

    BaseCammentOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    BaseCammentOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    BaseCammentOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.cmmsdk_camment_overlay, this);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Camment"));
        extractorsFactory = new DefaultExtractorsFactory();

        player.setPlayWhenReady(true);

        PermissionHelper.getInstance().initPermissionHelper(CammentSDK.getInstance().getCurrentActivity());

        try {
            analytics = MobileAnalyticsManager.getOrCreateInstance(
                    CammentSDK.getInstance().getApplicationContext(),
                    "ea4151c5b77046bfb7213de5d02f514f",
                    "us-east-1:71549a59-04b7-4924-9973-0c35c9278e78"
            );
        } catch (InitializationException ex) {
            Log.e("CammentSDK", "Failed to initialize Amazon Mobile Analytics", ex);
        }

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                ApiManager.getInstance().getUserApi().updateUserInfo(false);
            }
        };

        profileTracker.startTracking();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopCammentPlayback();
        if (analytics != null) {
            analytics.getSessionClient().pauseSession();
            analytics.getEventClient().submitEvents();
        }
        if (profileTracker != null
                && profileTracker.isTracking()) {
            profileTracker.stopTracking();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        stopCammentPlayback();
        if (analytics != null) {
            analytics.getSessionClient().pauseSession();
            analytics.getEventClient().submitEvents();
        }
        if (profileTracker != null
                && profileTracker.isTracking()) {
            profileTracker.stopTracking();
        }
        return super.onSaveInstanceState();
    }

    private void stopCammentPlayback() {
        if (player != null) {
            player.stop();
            lastVideoCammentUuid = null;
        }
        if (rvCamments != null) {
            rvCamments.showSmallThumbnailsForAllChildren();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) getContext()).getSupportLoaderManager().initLoader(1, null, this);
        }

        if (analytics != null) {
            analytics.getSessionClient().resumeSession();
        }

        if (profileTracker != null
                && !profileTracker.isTracking()) {
            profileTracker.startTracking();
        }
    }

    @Override
    protected void onFinishInflate() {
        flCamera = (SquareFrameLayout) findViewById(R.id.cmmsdk_fl_camera);
        vRecordIndicator = findViewById(R.id.cmmsdk_v_record_indicator);

        rvCamments = (CammentRecyclerView) findViewById(R.id.cmmsdk_rv_camments);
        ibRecord = (RecordingButton) findViewById(R.id.cmmsdk_ib_record);

        adapter = new CammentsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvCamments.setLayoutManager(layoutManager);
        rvCamments.setAdapter(adapter);
        rvCamments.setItemAnimator(null);

        ibRecord.setListener(this);

        onboardingOverlay = (OnboardingOverlay) findViewById(R.id.cmmsdk_onboarding_overlay);
        onboardingOverlay.setAnchorViews(ibRecord, rvCamments);

        if (getContext() instanceof AppCompatActivity) {
            FragmentManager fm = ((AppCompatActivity) getContext()).getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.drawer, DrawerFragment.newInstance()).commit();
        }

        super.onFinishInflate();
    }

    @Override
    public void onCammentClick(CammentViewHolder cammentViewHolder, Camment camment, TextureView textureView) {
        rvCamments.showSmallThumbnailsForAllChildren();

        if (exoEventListener != null) {
            player.removeListener(exoEventListener);
        }

        if (!camment.getUuid().equals(lastVideoCammentUuid)) {
            cammentViewHolder.setItemViewScale(cammentViewHolder.getItemViewScale() == 0.5f ? 1.0f : 0.5f);

            exoEventListener = new CammentPlayerEventListener(cammentAudioListener,
                    cammentViewHolder, this);
            player.addListener(exoEventListener);

            player.setVideoTextureView(textureView);
            ExtractorMediaSource videoSource = new ExtractorMediaSource(FileUtils.getInstance().getVideoUri(camment), dataSourceFactory, extractorsFactory, null, null);
            player.prepare(videoSource);

            onboardingOverlay.hideTooltipIfNeeded(Step.PLAY);

            this.lastVideoCammentUuid = camment.getUuid();
        } else {
            if (cammentAudioListener != null) {
                cammentAudioListener.onCammentPlaybackEnded();
            }
            player.stop();
            this.lastVideoCammentUuid = null;
        }
    }

    @Override
    public void resetLastCammentPlayed() {
        this.lastVideoCammentUuid = null;
    }

    @Override
    public void onCammentBottomSheetDisplayed() {
        onboardingOverlay.hideTooltipIfNeeded(Step.DELETE);
    }

    @Override
    public void stopCammentIfPlaying(Camment camment) {
        if (camment.getUuid().equals(lastVideoCammentUuid)) {
            if (cammentAudioListener != null) {
                cammentAudioListener.onCammentPlaybackEnded();
            }
            player.stop();
            this.lastVideoCammentUuid = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean dispatched = false;

        if (parentViewGroup != null) {
            dispatched = parentViewGroup.dispatchTouchEvent(event);
        }

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                mode = Mode.NONE;
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    startX = event.getX();
                    startY = event.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (dispatched) {
                    mode = Mode.NONE;
                    break;
                }

                float stopX = event.getX();
                float stopY = event.getY();

                if (Math.abs(startX - stopX) > THRESHOLD_X) {
                    if (Math.abs(startY - stopY) < THRESHOLD_Y) {
                        if (startX < stopX) {
                            if (event.getPointerCount() == 1
                                    && mode != Mode.GOING_BACK) {
                                mode = Mode.SHOW;
                            } else if (event.getPointerCount() == 2) {
                                mode = Mode.GOING_BACK;
                            }
                        } else {
                            if (event.getPointerCount() == 1) {
                                mode = Mode.HIDE;
                            } else if (event.getPointerCount() == 2) {
                                mode = Mode.NONE;
                            }
                        }
                    } else {
                        mode = Mode.NONE;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                switch (mode) {
                    case GOING_BACK:
                        Activity currentActivity = CammentSDK.getInstance().getCurrentActivity();
                        if (currentActivity != null) {
                            currentActivity.onBackPressed();
                        }
                        break;
                    case SHOW:
                        onboardingOverlay.hideTooltipIfNeeded(Step.SHOW);

                        if (ibRecord != null) {
                            ibRecord.show();
                        }
                        if (rvCamments != null) {
                            rvCamments.show();
                        }
                        break;
                    case HIDE:
                        stopCammentPlayback();
                        onboardingOverlay.hideTooltipIfNeeded(Step.HIDE);

                        if (ibRecord != null) {
                            ibRecord.hide();
                        }
                        if (rvCamments != null) {
                            rvCamments.hide();
                        }
                        break;
                }
                mode = Mode.NONE;
                break;
        }

        return true;
    }


    @Override
    public void onPulledDown() {
        onboardingOverlay.hideTooltipIfNeeded(Step.INVITE);

        if (FacebookHelper.getInstance().isLoggedIn()) {
            ApiManager.getInstance().getGroupApi().createEmptyUsergroupIfNeededAndGetDeeplink();
        } else {
            if (getContext() instanceof Activity) {
                FacebookHelper.getInstance().logIn((Activity) getContext(), true);
            }
        }
    }

    @Override
    public void onRecordingStart() {
        if (PermissionHelper.getInstance().hasPermissions()) {
            onboardingOverlay.hideTooltipIfNeeded(Step.RECORD);
            stopCammentPlayback();

            if (flCamera.getChildCount() < 2) {
                if (cameraGLView == null) {
                    cameraGLView = new CameraGLView(getContext());
                }
                cameraGLView.setPreviewStartedListener(this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                final int dp2 = CommonUtils.dpToPx(getContext(), 2);
                params.setMargins(dp2, dp2, dp2, dp2);
                flCamera.addView(cameraGLView, 0, params);
            }

            AnimationUtils.animateAppearCameraView(flCamera, cameraViewAppearAnimatorListener);
        } else {
            PermissionHelper.getInstance().cameraAndMicTask();
        }
    }

    @Override
    public void onRecordingStop(boolean cancelled) {
        AnimationUtils.cancelAppearAnimation();

        if (recordingHandler != null) {
            recordingHandler.stopRecording(cancelled, cammentAudioListener);
        }

        AnimationUtils.stopRecordAnimation(vRecordIndicator);

        AnimationUtils.animateDisappearCameraView(flCamera, cameraViewDisappearAnimatorListener);
    }

    private final Animator.AnimatorListener cameraViewDisappearAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (cameraGLView != null) {
                cameraGLView.onPause();
            }
            if (flCamera != null) {
                flCamera.setVisibility(GONE);
                flCamera.removeView(cameraGLView);
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    private final Animator.AnimatorListener cameraViewAppearAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            flCamera.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animator) {

        }

        @Override
        public void onAnimationCancel(Animator animator) {
            getHandler().removeCallbacksAndMessages(null);
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    @Override
    public void onPreviewStarted() {
        cameraGLView.clearPreviewStartedListener();

        if (recordingHandler == null) {
            recordingHandler = new RecordingHandler(Executors.newSingleThreadExecutor(), cameraGLView);
        }

        if (cammentAudioListener != null) {
            cammentAudioListener.onCammentRecordingStarted();
        }

        post(new Runnable() {
            @Override
            public void run() {
                AnimationUtils.startRecordAnimation(vRecordIndicator);
            }
        });
        recordingHandler.startRecording();
    }

    @Override
    public void onOnboardingStart() {
        onboardingOverlay.displayTooltip(Step.RECORD);
    }

}
