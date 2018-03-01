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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.InitializationException;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.MobileAnalyticsManager;
import com.camment.clientsdk.model.Camment;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.RenderersFactory;
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
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

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
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.messages.AdMessage;
import tv.camment.cammentsdk.camera.CameraGLView;
import tv.camment.cammentsdk.camera.CammentDefaultRenderersFactory;
import tv.camment.cammentsdk.camera.RecordingHandler;
import tv.camment.cammentsdk.data.AdvertisementProvider;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.data.model.ChatItem;
import tv.camment.cammentsdk.events.MediaCodecFailureEvent;
import tv.camment.cammentsdk.events.OnboardingEvent;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.MixpanelHelper;
import tv.camment.cammentsdk.helpers.OnboardingPreferences;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.helpers.Step;
import tv.camment.cammentsdk.receiver.NetworkChangeReceiver;
import tv.camment.cammentsdk.utils.CommonUtils;
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
        CammentPlayerEventListener.OnResetLastCammentPlayedListener, PullableView.PullListener {

    private static final String EXTRA_SUPER_STATE = "extra_super_state";
    private static final String EXTRA_AD_UUID = "extra_ad_uuid";

    private static final int CAMMENT_LOADER = 1;
    private static final int ADS_LOADER = 2;

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
    private PullableView pullableView;
    private OnboardingOverlay onboardingOverlay;
    private AdDetailView adDetailView;

    private CammentsAdapter adapter;

    private SimpleExoPlayer player;
    private DefaultDataSourceFactory dataSourceFactory;
    private CacheDataSourceFactory cacheDataSourceFactory;
    private DefaultExtractorsFactory extractorsFactory;

    private RecordingHandler recordingHandler;

    private ExoPlayer.EventListener exoEventListener;
    private String lastVideoCammentUuid;

    private static MobileAnalyticsManager analytics;

    private DrawerLayout drawerLayout;

    private NetworkChangeReceiver networkReceiver;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CAMMENT_LOADER) {
            return CammentProvider.getCammentLoader();
        } else {
            return AdvertisementProvider.getAdvertisementLoader();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
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
        } else {
            List<ChatItem<AdMessage>> ads = AdvertisementProvider.listFromCursor(data);
            adapter.setAdsData(ads);
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
        RenderersFactory renderersFactory = new CammentDefaultRenderersFactory(context);

        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Camment"));

        cacheDataSourceFactory = new CacheDataSourceFactory(AWSManager.getInstance().getExoPlayerCache(), dataSourceFactory);
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
        if (analytics != null) {
            analytics.getSessionClient().pauseSession();
            analytics.getEventClient().submitEvents();
        }

        EventBus.getDefault().unregister(this);

        super.onDetachedFromWindow();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        stopCammentPlayback();
        if (analytics != null) {
            analytics.getSessionClient().pauseSession();
            analytics.getEventClient().submitEvents();
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_SUPER_STATE, super.onSaveInstanceState());

        if (adDetailView != null
                && adDetailView.getVisibility() == VISIBLE) {
            ChatItem<AdMessage> adMessage = adDetailView.getData();
            if (adMessage != null) {
                bundle.putString(EXTRA_AD_UUID, adMessage.getUuid());
            }
        }

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable(EXTRA_SUPER_STATE);

            String adUuid = bundle.getString(EXTRA_AD_UUID);
            if (!TextUtils.isEmpty(adUuid)) {
                ChatItem<AdMessage> adByUuid = AdvertisementProvider.getAdByUuid(adUuid);
                if (adByUuid != null
                        && adByUuid.getContent() != null) {
                    onAdClick(adByUuid);
                }
            }
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

        if (getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) getContext()).getSupportLoaderManager().initLoader(CAMMENT_LOADER, null, this);
            ((AppCompatActivity) getContext()).getSupportLoaderManager().initLoader(ADS_LOADER, null, this);
        }

        if (analytics != null) {
            analytics.getSessionClient().resumeSession();
        }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onFinishInflate() {
        flCamera = (SquareFrameLayout) findViewById(R.id.cmmsdk_fl_camera);
        vRecordIndicator = findViewById(R.id.cmmsdk_v_record_indicator);

        rvCamments = (CammentRecyclerView) findViewById(R.id.cmmsdk_rv_camments);
        ibRecord = (RecordingButton) findViewById(R.id.cmmsdk_ib_record);

        pullableView = (PullableView) findViewById(R.id.cmmsdk_pullable_view);
        pullableView.addBoundView(new BoundView(ibRecord, Collections.<Transformation>singletonList(new TranslateTransformation())));
        pullableView.setListener(this);

        adapter = new CammentsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvCamments.setLayoutManager(layoutManager);
        rvCamments.setAdapter(adapter);
        ((DefaultItemAnimator) rvCamments.getItemAnimator()).setSupportsChangeAnimations(false);

        onboardingOverlay = (OnboardingOverlay) findViewById(R.id.cmmsdk_onboarding_overlay);
        onboardingOverlay.setAnchorViews(ibRecord, rvCamments);

        adDetailView = (AdDetailView) findViewById(R.id.cmmsdk_ad_detail_view);

        drawerLayout = (DrawerLayout) findViewById(R.id.cmmsdk_drawer_layout);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (onboardingOverlay != null) {
                    onboardingOverlay.hideTooltipIfNeeded(Step.INVITE);
                    onboardingOverlay.hideTooltipIfNeeded(Step.TUTORIAL);
                }
            }
        });

        if (getContext() instanceof AppCompatActivity) {
            FragmentManager fm = ((AppCompatActivity) getContext()).getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.drawer, DrawerFragment.newInstance()).commit();
        }

        super.onFinishInflate();
    }

    @Override
    public void onCammentClick(CammentViewHolder cammentViewHolder, CCamment camment, TextureView textureView) {
        rvCamments.showSmallThumbnailsForAllChildren();

        if (exoEventListener != null) {
            player.removeListener(exoEventListener);
        }

        if (!camment.getUuid().equals(lastVideoCammentUuid)) {
            cammentViewHolder.setItemViewScale(cammentViewHolder.getItemViewScale() == SDKConfig.CAMMENT_SMALL ? SDKConfig.CAMMENT_BIG : SDKConfig.CAMMENT_SMALL);

            exoEventListener = new CammentPlayerEventListener(cammentAudioListener,
                    cammentViewHolder, this);
            player.addListener(exoEventListener);

            player.setVideoTextureView(textureView);
            ExtractorMediaSource videoSource = new ExtractorMediaSource(FileUtils.getInstance().getVideoUri(camment), cacheDataSourceFactory, extractorsFactory, null, null);
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
        onboardingOverlay.hideTooltipIfNeeded(Step.PLAY);
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
    public void onAdClick(ChatItem<AdMessage> adMessage) {
        if (adDetailView != null) {
            adDetailView.setData(adMessage);
        }
    }

    @Override
    public void onCloseAdClick(ChatItem<AdMessage> chatItem) {
        if (chatItem != null) {
            AdvertisementProvider.deleteAdByUuid(chatItem.getUuid());
        }

        if (adDetailView != null) {
            adDetailView.setVisibility(GONE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean dispatched = false;

        if (parentViewGroup != null) {
            dispatched = parentViewGroup.dispatchTouchEvent(event);
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
            recordingHandler.stopRecording(cancelled, cammentAudioListener);
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

        if (cammentAudioListener != null) {
            MixpanelHelper.getInstance().trackEvent(MixpanelHelper.CAMMENT_RECORD);

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

    private void onStartOnboarding() {
        onboardingOverlay.displayTooltip(Step.RECORD);
    }

    private void onMaybeLaterOnboarding() {
        onboardingOverlay.displayTooltip(Step.LATER);
    }

    private void onHideMaybeLaterIfNeededOnboarding() {
        onboardingOverlay.hideTooltipIfNeeded(Step.LATER);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserGroupChangeEvent event) {
        if (adapter != null) {
            adapter.setData(null);
        }

        if (getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) getContext()).getSupportLoaderManager().destroyLoader(CAMMENT_LOADER);
            ((AppCompatActivity) getContext()).getSupportLoaderManager().initLoader(CAMMENT_LOADER, null, this);
            ((AppCompatActivity) getContext()).getSupportLoaderManager().destroyLoader(ADS_LOADER);
            ((AppCompatActivity) getContext()).getSupportLoaderManager().initLoader(ADS_LOADER, null, this);
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
        Toast.makeText(getContext(), R.string.cmmsdk_video_codec_issue, Toast.LENGTH_LONG).show();
        onRecordingStop(true);
    }

}
