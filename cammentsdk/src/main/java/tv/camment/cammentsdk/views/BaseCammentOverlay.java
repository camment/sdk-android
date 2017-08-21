package tv.camment.cammentsdk.views;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
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

import com.camment.clientsdk.model.Camment;
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
import tv.camment.cammentsdk.camera.RecordingHandler;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.DataContract;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.utils.AnimationUtils;
import tv.camment.cammentsdk.utils.CommonUtils;
import tv.camment.cammentsdk.utils.FileUtils;


public class BaseCammentOverlay extends RelativeLayout
        implements
        CammentsAdapter.ActionListener,
        RecordingButton.ActionsListener,
        LoaderManager.LoaderCallbacks<Cursor>, CameraGLView.OnPreviewStartedListener {

    private static final String ARG_SUPER_STATE = "arg_super_state";
    private static final String ARG_ACTIVE_GROUP_UUID = "arg_active_group";

    private static final int THRESHOLD_X = 100;
    private static final int THRESHOLD_Y = 150;

    private float startX;
    private float startY;

    protected ViewGroup parentViewGroup;
    protected CammentAudioListener cammentAudioListener;

    private SquareFrameLayout flCamera;
    private CameraGLView cameraGLView;
    private View vRecordIndicator;
    private CammentRecyclerView rvCamments;
    private RecordingButton ibRecord;

    private CammentsAdapter adapter;

    private SimpleExoPlayer player;
    private ExtractorMediaSource videoSource;
    private DefaultDataSourceFactory dataSourceFactory;
    private DefaultExtractorsFactory extractorsFactory;

    private RecordingHandler recordingHandler;

    private ExoPlayer.EventListener exoEventListener;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = DataContract.Camment.recorded + "=?";
        String[] selectionArgs = {"1"};

        return new CursorLoader(CammentSDK.getInstance().getApplicationContext(), DataContract.Camment.CONTENT_URI,
                null, selection, selectionArgs, DataContract.Camment.timestamp + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("Loader", "onLoadFinished");
        List<CCamment> camments = CammentProvider.listFromCursor(data);
        adapter.setData(camments);
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

    public BaseCammentOverlay(Context context) {
        super(context);
        init(context);
    }

    public BaseCammentOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseCammentOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public BaseCammentOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        Log.d("OVERLAY", "init");
        //TODO check xml for landscape layout
        View.inflate(context, R.layout.cmmsdk_camment_overlay, this);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Camment"));
        extractorsFactory = new DefaultExtractorsFactory();

        player.setPlayWhenReady(true);

        PermissionHelper.getInstance().initPermissionHelper(CammentSDK.getInstance().getCurrentActivity());
    }

//    @Override
//    protected Parcelable onSaveInstanceState() {
//        Log.d("OVERLAY", "onSaveInstanceState");
//        Bundle bundle = new Bundle();
//        bundle.putParcelable(ARG_SUPER_STATE, super.onSaveInstanceState());
//        final Usergroup usergroup = UserGroupProvider.getUserGroup();
//        bundle.putString(ARG_ACTIVE_GROUP_UUID, activeGroupUuid);
//        return bundle;
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        Log.d("OVERLAY", "onRestoreInstanceState");
//        if (state instanceof Bundle) {
//            Bundle bundle = (Bundle) state;
//            activeGroupUuid = bundle.getString(ARG_ACTIVE_GROUP_UUID);
//            state = bundle.getParcelable(ARG_SUPER_STATE);
//        }
//        super.onRestoreInstanceState(state);
//    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Log.d("OVERLAY", "onAttachedToWindow");

        if (getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) getContext()).getSupportLoaderManager().initLoader(1, null, this);
        }

//        if (TextUtils.isEmpty(activeGroupUuid)) {
//            final Usergroup usergroup = UserGroupProvider.getUserGroup();
//            if (usergroup == null || TextUtils.isEmpty(usergroup.getUuid())) {
//                ApiManager.getInstance().getGroupApi().createEmptyUsergroup();
//            } else {
//                ApiManager.getInstance().getCammentApi().getUserGroupCamments();
//            }
//        }
    }

    @Override
    protected void onFinishInflate() {
        flCamera = findViewById(R.id.fl_camera);
        vRecordIndicator = findViewById(R.id.v_record_indicator);

        rvCamments = findViewById(R.id.rv_camments);
        ibRecord = findViewById(R.id.ib_record);

        adapter = new CammentsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvCamments.setLayoutManager(layoutManager);
        rvCamments.setAdapter(adapter);
        rvCamments.setItemAnimator(null);

        ibRecord.setListener(this);

        super.onFinishInflate();
    }

    @Override
    public void onCammentClick(CammentViewHolder cammentViewHolder, Camment camment, TextureView textureView) {
        rvCamments.showSmallThumbnailsForAllChildren();

        if (exoEventListener != null) {
            player.removeListener(exoEventListener);
        }

        if (player.getPlaybackState() == ExoPlayer.STATE_IDLE
                || player.getPlaybackState() == ExoPlayer.STATE_ENDED) {

            cammentViewHolder.setItemViewScale(cammentViewHolder.getItemViewScale() == 0.5f ? 1.0f : 0.5f);

            exoEventListener = new CammentPlayerEventListener(cammentAudioListener, cammentViewHolder);
            player.addListener(exoEventListener);

            player.setVideoTextureView(textureView);
            videoSource = new ExtractorMediaSource(FileUtils.getInstance().getVideoUri(camment), dataSourceFactory, extractorsFactory, null, null);
            player.prepare(videoSource);
        } else {
            if (cammentAudioListener != null) {
                cammentAudioListener.onCammentPlaybackEnded();
            }
            player.stop();
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
                        if (rvCamments != null) {
                            rvCamments.show();
                        }
                        break;
                    case HIDE:
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
        if (FacebookHelper.getInstance().isLoggedIn()) {
            new FbFriendsBottomSheetDialog(getContext()).show();
        } else {
            if (getContext() instanceof Activity) {
                FacebookHelper.getInstance().logIn((Activity) getContext());
            }
        }
    }

    @Override
    public void onRecordingStart() {
        if (PermissionHelper.getInstance().hasPermissions()) {
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

    private Animator.AnimatorListener cameraViewDisappearAnimatorListener = new Animator.AnimatorListener() {
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

    private Animator.AnimatorListener cameraViewAppearAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            flCamera.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            Log.d("recording", "onAnimationEnd");
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            Log.d("delayed", "cancel");
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

}
