package tv.camment.cammentsdk.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.Usergroup;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.concurrent.Executors;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.camera.RecordingHandler;
import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.utils.CommonUtils;
import tv.camment.cammentsdk.utils.NoSqlHelper;


public class CammentOverlay extends RelativeLayout
        implements
        CammentsAdapter.ActionListener,
        RecordingButton.ActionsListener,
        PermissionHelper.PermissionsListener {

    private static final String ARG_SUPER_STATE = "arg_super_state";
    private static final String ARG_ACTIVE_GROUP_UUID = "arg_active_group";

    private static final int THRESHOLD = 100;

    private float startX;
    private float stopX;
    private float startY;
    private float stopY;

    private ViewGroup parentViewGroup;

    private FrameLayout flCamera;
    private CameraGLView cameraGLView;
    private RecyclerView rvCamments;
    private RecordingButton ibRecord;

    private CammentsAdapter adapter;

    private SimpleExoPlayer player;
    private ExtractorMediaSource videoSource;
    private DefaultDataSourceFactory dataSourceFactory;
    private DefaultExtractorsFactory extractorsFactory;

    private RecordingHandler recordingHandler;

    private String activeGroupUuid;

    private enum Mode {
        GOING_BACK,
        HIDE,
        SHOW,
        NONE
    }

    private Mode mode = Mode.NONE;

    public CammentOverlay(Context context) {
        super(context);
        init(context);
    }

    public CammentOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CammentOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public CammentOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        Log.d("OVERLAY", "init");
        View.inflate(context, R.layout.cmmsdk_camment_overlay, this);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Camment"));
        extractorsFactory = new DefaultExtractorsFactory();

        player.setPlayWhenReady(true);

        if (getContext() instanceof Activity) {
            PermissionHelper.getInstance().initPermissionHelper((Activity) getContext());
        }
        PermissionHelper.getInstance().setListener(this);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d("OVERLAY", "onSaveInstanceState");
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_SUPER_STATE, super.onSaveInstanceState());
        final Usergroup usergroup = NoSqlHelper.getActiveGroup();
        bundle.putString(ARG_ACTIVE_GROUP_UUID, usergroup.getUuid());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.d("OVERLAY", "onRestoreInstanceState");
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            activeGroupUuid = bundle.getString(ARG_ACTIVE_GROUP_UUID);
            state = bundle.getParcelable(ARG_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Log.d("OVERLAY", "onAttachedToWindow");

        if (TextUtils.isEmpty(activeGroupUuid)) {
            final Usergroup usergroup = NoSqlHelper.getActiveGroup();
            if (TextUtils.isEmpty(usergroup.getUuid())) {
                ApiManager.getInstance().getGroupApi().createEmptyUsergroup();
            } else {
                ApiManager.getInstance().getCammentApi().getUserGroupCamments(adapter);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("OVERLAY", "onDetachedFromWindow");
    }

    @Override
    protected void onFinishInflate() {
        flCamera = findViewById(R.id.fl_camera);

        //cameraGLView = findViewById(R.id.camera_view);
        rvCamments = findViewById(R.id.rv_camments);
        ibRecord = findViewById(R.id.ib_record);

        adapter = new CammentsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvCamments.setLayoutManager(layoutManager);
        rvCamments.setAdapter(adapter);

        ibRecord.setListener(this);

        super.onFinishInflate();
    }

    @Override
    public void onCammentClick(Camment camment, TextureView textureView) {
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.d("onLoadingChanged", "isLoading: " + isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d("onPlayerStateChanged", "playWhenReady: " + playWhenReady);
                Log.d("onPlayerStateChanged", "playbackState: " + playbackState);
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e("onPlayerError", "error", error);
            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });

        //applyMatrix(textureView, 320, 320, 0);
        videoSource = new ExtractorMediaSource(Uri.parse(camment.getUrl()), dataSourceFactory, extractorsFactory, null, null);
        player.prepare(videoSource);

        player.setVideoTextureView(textureView);
    }

    public static void applyMatrix(TextureView textureView, int videoWidth, int videoHeight,
                                   int requiredRotation) {
        int pivotPoint = textureView.getWidth() / 2;
        float ratio = (float) videoWidth / videoHeight;
        float scaleX;
        float scaleY;

        if (requiredRotation > -1) {
            scaleX = ratio > 1 ? ratio : 1;
            scaleY = ratio > 1 ? 1 : 1f / ratio;
        } else {
            scaleX = ratio > 1 ? 1 : 1f / ratio;
            scaleY = ratio > 1 ? ratio : 1;
        }

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, pivotPoint, pivotPoint);
        if (requiredRotation > -1) {
            matrix.postRotate(requiredRotation, pivotPoint, pivotPoint);
        }
        textureView.setTransform(matrix);
    }

    public void setParentViewGroup(ViewGroup parentViewGroup) {
        this.parentViewGroup = parentViewGroup;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean dispatched = false;

        if (parentViewGroup != null) {
            dispatched = parentViewGroup.dispatchTouchEvent(event);
        }

        checkFor2FingerSwipeBack(event, dispatched);

        return true;
    }

    private void checkFor2FingerSwipeBack(MotionEvent event, boolean dispatched) {
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

                stopX = event.getX();
                stopY = event.getY();

                if (Math.abs(startX - stopX) > THRESHOLD) {
                    if (Math.abs(startY - stopY) < THRESHOLD) {
                        if (startX < stopX) {
                            if (event.getPointerCount() == 1) {
                                mode = Mode.HIDE;
                            } else if (event.getPointerCount() == 2) {
                                mode = Mode.GOING_BACK;
                            }
                        } else {
                            if (event.getPointerCount() == 1) {
                                mode = Mode.SHOW;
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
                        Log.d("TOUCH", "GO BACK!");
                        break;
                    case SHOW:
                        if (ibRecord != null) {
                            ibRecord.show();
                        }
                        break;
                    case HIDE:
                        if (ibRecord != null) {
                            ibRecord.hide();
                        }
                        break;
                }
                mode = Mode.NONE;
                break;
        }
    }

    @Override
    public void onPulledDown() {
        if (FacebookHelper.getInstance().isLoggedIn()) {
            //TODO show friends
        } else {
            //TODO check with fragment
            if (getContext() instanceof Activity) {
                FacebookHelper.getInstance().logIn((Activity) getContext());
            }
        }
    }

    @Override
    public void onRecordingStart() {
        PermissionHelper.getInstance().cameraAndMicTask();
    }

    @Override
    public void onRecordingStop() {
        if (recordingHandler != null) {
            recordingHandler.stopRecording();
        }

        cameraGLView.onPause();
        flCamera.setVisibility(GONE);
    }

    @Override
    public void enableRecording() {
        //TODO do real recording here


        if (cameraGLView == null) {
            cameraGLView = new CameraGLView(getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            int dp2 = CommonUtils.dpToPx(getContext(), 2);
            params.setMargins(dp2, dp2, dp2, dp2);
            flCamera.addView(cameraGLView, 0, params);
        } else {
            cameraGLView.onResume();
        }

        flCamera.setVisibility(VISIBLE);
        flCamera.setPivotX(0);
        flCamera.setPivotY(0);
        ObjectAnimator xAnimator = ObjectAnimator.ofFloat(flCamera, "scaleX", 0.0f, 1.0f);
        ObjectAnimator yAnimator = ObjectAnimator.ofFloat(flCamera, "scaleY", 0.0f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(xAnimator, yAnimator);
        set.setDuration(500);
        set.start();

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (recordingHandler == null) {
                    recordingHandler = new RecordingHandler(Executors.newSingleThreadExecutor(), cameraGLView);
                }
                recordingHandler.startRecording();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    @Override
    public void disableRecording() {
        //TODO this may not be needed
    }

}
