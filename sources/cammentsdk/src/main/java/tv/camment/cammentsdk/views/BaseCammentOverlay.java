package tv.camment.cammentsdk.views;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.InitializationException;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.MobileAnalyticsManager;
import com.camment.clientsdk.model.Camment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.PendingActions;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.camera.CDefaultRenderersFactory;
import tv.camment.cammentsdk.camera.CameraGLView;
import tv.camment.cammentsdk.camera.RecordingHandler;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.data.model.ChatItem;
import tv.camment.cammentsdk.events.CheckDisplayedCammentsEvent;
import tv.camment.cammentsdk.events.MediaCodecFailureEvent;
import tv.camment.cammentsdk.events.OnboardingEvent;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.exoplayer.ExoPlayerFactory;
import tv.camment.cammentsdk.exoplayer.Player;
import tv.camment.cammentsdk.exoplayer.RenderersFactory;
import tv.camment.cammentsdk.exoplayer.SimpleExoPlayer;
import tv.camment.cammentsdk.exoplayer.extractor.DefaultExtractorsFactory;
import tv.camment.cammentsdk.exoplayer.source.ExtractorMediaSource;
import tv.camment.cammentsdk.exoplayer.trackselection.AdaptiveTrackSelection;
import tv.camment.cammentsdk.exoplayer.trackselection.DefaultTrackSelector;
import tv.camment.cammentsdk.exoplayer.trackselection.TrackSelection;
import tv.camment.cammentsdk.exoplayer.trackselection.TrackSelector;
import tv.camment.cammentsdk.exoplayer.upstream.BandwidthMeter;
import tv.camment.cammentsdk.exoplayer.upstream.DefaultBandwidthMeter;
import tv.camment.cammentsdk.exoplayer.upstream.DefaultDataSourceFactory;
import tv.camment.cammentsdk.exoplayer.upstream.cache.CacheDataSourceFactory;
import tv.camment.cammentsdk.exoplayer.util.Util;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.OnboardingPreferences;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.helpers.SnackbarQueueHelper;
import tv.camment.cammentsdk.helpers.SnackbarType;
import tv.camment.cammentsdk.helpers.Step;
import tv.camment.cammentsdk.helpers.SyncHelper;
import tv.camment.cammentsdk.receiver.NetworkChangeReceiver;
import tv.camment.cammentsdk.utils.CommonUtils;
import tv.camment.cammentsdk.utils.ExoCacheUtils;
import tv.camment.cammentsdk.utils.FileUtils;
import tv.camment.cammentsdk.utils.LogUtils;
import tv.camment.cammentsdk.views.pullable.BoundView;
import tv.camment.cammentsdk.views.pullable.PullableView;
import tv.camment.cammentsdk.views.pullable.Transformation;
import tv.camment.cammentsdk.views.pullable.TranslateTransformation;

abstract class BaseCammentOverlay extends RelativeLayout
        implements
        CammentsAdapter.ActionListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        OnPreviewStartedListener,
        CammentPlayerEventListener.OnResetLastCammentPlayedListener, PullableView.PullListener, CammentListOnScrollListener.OnCammentLoadingMoreListener {

    private static final String EXTRA_SUPER_STATE = "extra_super_state";
    private static final String EXTRA_RECORD_MARGIN = "extra_record_margin";

    private static final int CAMMENT_LOADER = 1;

    private static final int THRESHOLD_X = 100;
    private static final int THRESHOLD_Y = 300;

    private float startX;
    private float startY;

    ViewGroup parentViewGroup;

    private SquareFrameLayout flCamera;
    private CameraGLView cameraGLView;
    private View vRecordIndicator;
    private CammentRecyclerView rvCamments;
    private RecordingButton ibRecord;
    private PullableView pullableView;
    private OnboardingOverlay onboardingOverlay;
    private AdDetailView adDetailView;

    private LinearLayoutManager layoutManager;
    private CammentListOnScrollListener cammentListOnScrollListener;

    private CammentsAdapter adapter;

    private SimpleExoPlayer player;
    private CacheDataSourceFactory cacheDataSourceFactory;
    private DefaultExtractorsFactory extractorsFactory;
    private DefaultDataSourceFactory dataSourceFactory;

    private RecordingHandler recordingHandler;

    private Player.EventListener exoEventListener;
    private String lastVideoCammentUuid;

    private static MobileAnalyticsManager analytics;

    private DrawerLayout drawerLayout;

    private NetworkChangeReceiver networkReceiver;

    private int recordButtonMarginBottom;

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return CammentProvider.getCammentLoader();
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CAMMENT_LOADER) {
            List<ChatItem<CCamment>> camments = CammentProvider.listFromCursor(data);
            adapter.setData(camments);

            if (camments != null) {
                if (camments.size() == 1
                        && !OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.PLAY)) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onboardingOverlay.displayTooltip(Step.PLAY);
                        }
                    }, 500);
                } else if (camments.size() > 0
                        && OnboardingPreferences.getInstance().isOnboardingStepLastRemaining(Step.DELETE)
                        && !OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.DELETE)) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onboardingOverlay.displayTooltip(Step.DELETE);
                        }
                    }, 500);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loadeßr) {

    }

    @Override
    public void onCammentLoadingMoreStarted() {
        if (adapter != null) {
            adapter.setLoading(true);
        }
    }

    @Override
    public void onCammentLoadingMoreFinished() {
        if (adapter != null) {
            adapter.setLoading(false);
        }
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
        LayoutInflater.from(context).inflate(R.layout.cmmsdk_camment_overlay, this);

        initViews();

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        RenderersFactory renderersFactory = new CDefaultRenderersFactory(context);

        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Camment"));

        cacheDataSourceFactory = new CacheDataSourceFactory(ExoCacheUtils.getInstance().getCache(), dataSourceFactory);
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
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            networkReceiver = new NetworkChangeReceiver();
            getContext().registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } else if (visibility == GONE) {
            if (networkReceiver != null) {
                try {
                    getContext().unregisterReceiver(networkReceiver);
                } catch (IllegalArgumentException e) {
                    LogUtils.debug("onException", "networkChangeReceiver", e);
                }
                networkReceiver = null;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopCammentPlayback();

        if (getContext() instanceof Activity
                && ((Activity) getContext()).isFinishing()) {
            UserGroupProvider.setAllAsNotActive();

            ApiManager.getInstance().getGroupApi().deleteMyActiveGroup();
        }

        if (analytics != null) {
            analytics.getSessionClient().pauseSession();
            analytics.getEventClient().submitEvents();
        }

        EventBus.getDefault().unregister(this);

        SyncHelper.getInstance().cleanAllHandlers();

        super.onDetachedFromWindow();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        stopCammentPlayback();
        if (analytics != null) {
            analytics.getSessionClient().pauseSession();
            analytics.getEventClient().submitEvents();
        }

        SyncHelper.getInstance().cleanAllHandlers();

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_SUPER_STATE, super.onSaveInstanceState());

        bundle.putInt(EXTRA_RECORD_MARGIN, recordButtonMarginBottom);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SyncHelper.getInstance().restartHandlersIfNeeded();

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable(EXTRA_SUPER_STATE);

            recordButtonMarginBottom = bundle.getInt(EXTRA_RECORD_MARGIN, 0);
        }
        super.onRestoreInstanceState(state);
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

        if (CammentSDK.getInstance().getCurrentActivity() instanceof FragmentActivity) {
            ((FragmentActivity) CammentSDK.getInstance().getCurrentActivity()).getSupportLoaderManager().initLoader(CAMMENT_LOADER, null, this);
        }

        if (analytics != null) {
            analytics.getSessionClient().resumeSession();
        }

        EventBus.getDefault().register(this);
    }

    private void initViews() {
        flCamera = findViewById(R.id.cmmsdk_fl_camera);

        vRecordIndicator = findViewById(R.id.cmmsdk_v_record_indicator);
        vRecordIndicator.getLayoutParams().width = CommonUtils.dpToPx(getContext(), SDKConfig.RECORD_INDICATOR_DP);
        vRecordIndicator.getLayoutParams().height = CommonUtils.dpToPx(getContext(), SDKConfig.RECORD_INDICATOR_DP);

        rvCamments = findViewById(R.id.cmmsdk_rv_camments);
        rvCamments.setCammentOverlay(this);

        int recordMargin = CommonUtils.dpToPx(getContext(), (int) ((SDKConfig.RECORD_NORMAL_DP * 1.5 - SDKConfig.RECORD_NORMAL_DP) / 2));

        ibRecord = findViewById(R.id.cmmsdk_ib_record);
        ibRecord.getLayoutParams().width = CommonUtils.dpToPx(getContext(), SDKConfig.RECORD_NORMAL_DP);
        ibRecord.getLayoutParams().height = CommonUtils.dpToPx(getContext(), SDKConfig.RECORD_NORMAL_DP);
        ((MarginLayoutParams) ibRecord.getLayoutParams()).setMargins(0, 0, recordMargin, recordMargin);

        pullableView = findViewById(R.id.cmmsdk_pullable_view);
        pullableView.getLayoutParams().width = CommonUtils.dpToPx(getContext(), SDKConfig.RECORD_NORMAL_DP);
        pullableView.getLayoutParams().height = CommonUtils.dpToPx(getContext(), SDKConfig.RECORD_NORMAL_DP);
        ((MarginLayoutParams) pullableView.getLayoutParams()).setMargins(0, 0, recordMargin, recordMargin);
        pullableView.addBoundView(new BoundView(ibRecord, Collections.<Transformation>singletonList(new TranslateTransformation())));
        pullableView.setListener(this);

        adapter = new CammentsAdapter(this);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvCamments.setLayoutManager(layoutManager);
        rvCamments.setAdapter(adapter);
        ((DefaultItemAnimator) rvCamments.getItemAnimator()).setSupportsChangeAnimations(false);
        rvCamments.clearOnScrollListeners();
        cammentListOnScrollListener = new CammentListOnScrollListener(layoutManager, this);
        rvCamments.addOnScrollListener(cammentListOnScrollListener);

        onboardingOverlay = findViewById(R.id.cmmsdk_onboarding_overlay);
        onboardingOverlay.setAnchorViews(ibRecord, rvCamments);

        adDetailView = findViewById(R.id.cmmsdk_ad_detail_view);

        drawerLayout = findViewById(R.id.cmmsdk_drawer_layout);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (onboardingOverlay != null) {
                    onboardingOverlay.hideTooltipIfNeeded(Step.INVITE);
                    onboardingOverlay.hideTooltipIfNeeded(Step.TUTORIAL);
                }
            }
        });

        if (CammentSDK.getInstance().getCurrentActivity() instanceof FragmentActivity) {
            FragmentManager fm = ((FragmentActivity) CammentSDK.getInstance().getCurrentActivity()).getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.drawer, DrawerFragment.newInstance()).commit();
        }
    }

    private void setMarginsForRecordViews() {
        int recordMargin = CommonUtils.dpToPx(getContext(), (int) ((SDKConfig.RECORD_NORMAL_DP * 1.5 - SDKConfig.RECORD_NORMAL_DP) / 2));

        if (ibRecord != null && pullableView != null) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) ibRecord.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.width = CommonUtils.dpToPx(getContext(), SDKConfig.RECORD_NORMAL_DP);
                layoutParams.height = CommonUtils.dpToPx(getContext(), SDKConfig.RECORD_NORMAL_DP);
                layoutParams.setMargins(0, 0, recordMargin, recordMargin + recordButtonMarginBottom);

                ibRecord.setLayoutParams(layoutParams);
            }

            MarginLayoutParams params = (MarginLayoutParams) pullableView.getLayoutParams();
            if (params != null) {
                params.width = CommonUtils.dpToPx(getContext(), SDKConfig.RECORD_NORMAL_DP);
                params.height = CommonUtils.dpToPx(getContext(), SDKConfig.RECORD_NORMAL_DP);
                params.setMargins(0, 0, recordMargin, recordMargin + recordButtonMarginBottom);

                pullableView.setLayoutParams(params);
            }
        }
    }

    @Override
    public void onCammentClick(CammentViewHolder cammentViewHolder, CCamment camment, TextureView textureView) {
        rvCamments.showSmallThumbnailsForAllChildren();

        if (exoEventListener != null) {
            player.removeListener(exoEventListener);
        }

        if (!camment.getUuid().equals(lastVideoCammentUuid)) {
            cammentViewHolder.setItemViewScale(cammentViewHolder.getItemViewScale() == SDKConfig.CAMMENT_SMALL ? SDKConfig.CAMMENT_BIG : SDKConfig.CAMMENT_SMALL);

            exoEventListener = new CammentPlayerEventListener(CammentSDK.getInstance().getCammentAudioListener(),
                    cammentViewHolder, this);
            player.addListener(exoEventListener);

            player.setVideoTextureView(textureView);
            ExtractorMediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(FileUtils.getInstance().getVideoUri(camment));
            player.prepare(videoSource);

            onboardingOverlay.hideTooltipIfNeeded(Step.PLAY);

            this.lastVideoCammentUuid = camment.getUuid();
        } else {
            if (CammentSDK.getInstance().getCammentAudioListener() != null) {
                CammentSDK.getInstance().getCammentAudioListener().onCammentPlaybackEnded();
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
        onboardingOverlay.hideTooltipIfNeeded(Step.PLAY);
    }

    @Override
    public void stopCammentIfPlaying(Camment camment) {
        if (camment.getUuid().equals(lastVideoCammentUuid)) {
            if (CammentSDK.getInstance().getCammentAudioListener() != null) {
                CammentSDK.getInstance().getCammentAudioListener().onCammentPlaybackEnded();
            }
            player.stop();
            this.lastVideoCammentUuid = null;
        }
    }

    @Override
    public void onLoadMoreIfPossible() {
        if (cammentListOnScrollListener != null) {
            cammentListOnScrollListener.loadMoreItems(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (parentViewGroup != null) {
            parentViewGroup.dispatchTouchEvent(event);
        }

        switch (event.getAction()) {
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
                        if (pullableView != null) {
                            pullableView.show();
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
                        if (pullableView != null) {
                            pullableView.hide();
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
    public void onReset(boolean cancelled, boolean callRecordingStop) {
        LogUtils.debug("PULL", "onReset cancelled: " + cancelled + " callRecordingStop: " + callRecordingStop);
        if (callRecordingStop) {
            onRecordingStop(cancelled);
        }
    }

    @Override
    public void onPullStart() {

    }

    @Override
    public void onAnchor() {
        onPulledDown();
    }

    @Override
    public void onPress() {
        onRecordingStart();
    }

    @Override
    public void onOnboardingHideMaybeLaterIfNeeded() {
        onHideMaybeLaterIfNeededOnboarding();
    }

    private void onPulledDown() {
        onboardingOverlay.hideTooltipIfNeeded(Step.INVITE);

        if (AuthHelper.getInstance().isLoggedIn()) {
            ApiManager.getInstance().getGroupApi().createEmptyUsergroupIfNeededAndGetDeeplink();
        } else {
            PendingActions.getInstance().addAction(PendingActions.Action.SHOW_SHARING_OPTIONS);
            AuthHelper.getInstance().checkLogin(null);
        }
    }

    private void onRecordingStart() {
        if (PermissionHelper.getInstance().hasPermissions()) {
            onboardingOverlay.hideTooltipIfNeeded(Step.RECORD);
            stopCammentPlayback();

            if (flCamera.getChildCount() < 2) {
                if (cameraGLView == null) {
                    cameraGLView = new CameraGLView(getContext(), flCamera);
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

    private void onRecordingStop(boolean cancelled) {
        AnimationUtils.cancelAppearAnimation();

        if (recordingHandler != null) {
            recordingHandler.stopRecording(cancelled, CammentSDK.getInstance().getCammentAudioListener());
        }

        AnimationUtils.stopRecordAnimation(vRecordIndicator);

        if (cancelled) {
            if (cameraGLView != null) {
                cameraGLView.onPause();
            }
            if (flCamera != null) {
                flCamera.setVisibility(GONE);
                flCamera.removeView(cameraGLView);
            }
        } else {
            AnimationUtils.animateDisappearCameraView(flCamera, cameraViewDisappearAnimatorListener);
        }
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

        if (CammentSDK.getInstance().getCammentAudioListener() != null) {
            CammentSDK.getInstance().getCammentAudioListener().onCammentRecordingStarted();
        }

        post(new Runnable() {
            @Override
            public void run() {
                AnimationUtils.startRecordAnimation(vRecordIndicator);
            }
        });
        recordingHandler.startRecording();
    }

    private void onStartOnboarding() {
        onboardingOverlay.displayTooltip(Step.RECORD);
    }

    private void onMaybeLaterOnboarding() {
        onboardingOverlay.displayTooltip(Step.LATER);
    }

    private void onHideMaybeLaterIfNeededOnboarding() {
        onboardingOverlay.hideTooltipIfNeeded(Step.LATER);
    }

    void setRecordButtonMarginBottom(int bottomMarginInDp) {
        if (ibRecord != null && pullableView != null) {
            this.recordButtonMarginBottom = CommonUtils.dpToPx(getContext(), bottomMarginInDp);

            setMarginsForRecordViews();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserGroupChangeEvent event) {
        if (adapter != null) {
            adapter.setData(null);
        }

        if (rvCamments != null) {
            rvCamments.clearOnScrollListeners();
            cammentListOnScrollListener = new CammentListOnScrollListener(layoutManager, this);
            rvCamments.addOnScrollListener(cammentListOnScrollListener);
        }

        if (CammentSDK.getInstance().getCurrentActivity() instanceof FragmentActivity) {
            ((FragmentActivity) CammentSDK.getInstance().getCurrentActivity()).getSupportLoaderManager().destroyLoader(CAMMENT_LOADER);
            ((FragmentActivity) CammentSDK.getInstance().getCurrentActivity()).getSupportLoaderManager().initLoader(CAMMENT_LOADER, null, this);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OnboardingEvent event) {
        if (event.shouldStart()) {
            onStartOnboarding();
        } else {
            onMaybeLaterOnboarding();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MediaCodecFailureEvent event) {
        SnackbarQueueHelper.getInstance().addSnackbar(new SnackbarQueueHelper.Snackbar(SnackbarType.CODEC_ISSUE, SnackbarQueueHelper.SHORT));

        onRecordingStop(true);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CheckDisplayedCammentsEvent event) {
        if (cammentListOnScrollListener != null) {
            cammentListOnScrollListener.loadMoreItems(true);
        }

        if (adapter != null) {
            adapter.checkDisplayedCamments();
        }
    }

}
