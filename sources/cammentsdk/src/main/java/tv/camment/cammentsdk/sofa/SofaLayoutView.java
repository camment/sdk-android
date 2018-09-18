package tv.camment.cammentsdk.sofa;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.camment.clientsdk.model.Sofa;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.ShowMetadata;
import tv.camment.cammentsdk.events.HideSofaInviteProgress;
import tv.camment.cammentsdk.events.PermissionStatusChangedEvent;
import tv.camment.cammentsdk.exoplayer.ExoPlaybackException;
import tv.camment.cammentsdk.exoplayer.PlaybackParameters;
import tv.camment.cammentsdk.exoplayer.Player;
import tv.camment.cammentsdk.exoplayer.Timeline;
import tv.camment.cammentsdk.exoplayer.source.TrackGroupArray;
import tv.camment.cammentsdk.exoplayer.trackselection.TrackSelectionArray;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.utils.CommonUtils;

public class SofaLayoutView extends RelativeLayout implements SofaImageView.Listener {

    private static final int[] headRects = new int[]{116, 331, 528};

    private SofaBgImageView sofaBgImageView;
    private SofaImageView sofaImageView;
    private Button btnContinueToShow;
    private TextView tvInfo;
    private TextView tvCamera;
    private ImageView ivCameraTextShadow;
    private ImageView ivLogo;

    private Sofa sofa;

    private SofaCammentView sofaCammentView;
    private SofaCameraView sofaCameraView;
    private SofaInviteView sofaInviteView;

    private View sofaOverlayView;

    private Listener listener;

    public SofaLayoutView(Context context) {
        super(context);
        init(context);
    }

    public SofaLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SofaLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.cmmsdk_sofa_layout, this);

        setSaveEnabled(true);

        PermissionHelper.getInstance().initPermissionHelper(CammentSDK.getInstance().getCurrentActivity());

        sofaBgImageView = findViewById(R.id.cmmsdk_sofa_bg_image);

        sofaImageView = findViewById(R.id.cmmsdk_iv_sofa);
        sofaImageView.setListener(this);

        ivLogo = findViewById(R.id.cmmsdk_iv_logo);
        ivLogo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(sofa.getTargetUrl())) {
                    getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sofa.getTargetUrl())));
                }
            }
        });

        tvInfo = findViewById(R.id.cmmsdk_tv_info);

        tvCamera = findViewById(R.id.cmmsdk_tv_camera);
        tvCamera.setVisibility(INVISIBLE);

        ivCameraTextShadow = findViewById(R.id.cmmsdk_iv_camera_text_shadow);
        ivCameraTextShadow.setVisibility(INVISIBLE);

        btnContinueToShow = findViewById(R.id.cmmsdk_btn_continue_show);
        btnContinueToShow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClose();
                }
            }
        });

        sofaInviteView = findViewById(R.id.cmmsdk_sofa_invite_view);

        sofaCameraView = findViewById(R.id.cmmsdk_sofa_cammera_view);

        sofaOverlayView = findViewById(R.id.cmmsdk_sofa_overlay_view);

        sofaCammentView = findViewById(R.id.cmmsdk_sofa_camment_view);
        sofaCammentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sofaCammentView.getScaleX() == 1.0f) {
                    sofaCammentView.setScaleX(1.4f);
                    sofaCammentView.setScaleY(1.4f);
                    sofaCammentView.play();

                    if (sofaOverlayView != null) {
                        sofaOverlayView.setVisibility(VISIBLE);
                    }
                } else {
                    sofaCammentView.setScaleX(1.0f);
                    sofaCammentView.setScaleY(1.0f);
                    sofaCammentView.stop();

                    if (sofaOverlayView != null) {
                        sofaOverlayView.setVisibility(GONE);
                    }
                }
            }
        });
        sofaCammentView.setPlayerListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_ENDED:
                        if (sofaCammentView != null) {
                            sofaCammentView.setScaleX(1.0f);
                            sofaCammentView.setScaleY(1.0f);
                        }
                        if (sofaCammentView != null) {
                            sofaCammentView.stop();
                        }
                        if (sofaOverlayView != null) {
                            sofaOverlayView.setVisibility(GONE);
                        }
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_LANDSCAPE:
                tvCamera.setVisibility(INVISIBLE);
                ivCameraTextShadow.setVisibility(INVISIBLE);

                sofaCammentView.setVisibility(INVISIBLE);
                sofaCameraView.setVisibility(INVISIBLE);
                sofaInviteView.setVisibility(INVISIBLE);

                sofaCameraView.resetCameraPreview();
                break;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            ConstraintLayout.LayoutParams tvInfoParams = (ConstraintLayout.LayoutParams) tvInfo.getLayoutParams();
            tvInfoParams.bottomMargin = CommonUtils.dpToPx(getContext(), getMeasuredWidth() < getMeasuredHeight() ? 16 : 24);
            tvInfo.setLayoutParams(tvInfoParams);

            ConstraintLayout.LayoutParams btnContinueParams = (ConstraintLayout.LayoutParams) btnContinueToShow.getLayoutParams();
            btnContinueParams.matchConstraintMinWidth = getMeasuredWidth() < getMeasuredHeight() ? getMeasuredWidth() / 4 : getMeasuredWidth() / 5;
            btnContinueParams.matchConstraintMinHeight = Math.round(btnContinueToShow.getTextSize() * 3f);
            btnContinueToShow.setLayoutParams(btnContinueParams);

            if (sofa != null && !TextUtils.isEmpty(sofa.getBackgroundImage())) {
                sofaBgImageView.setImageUrl(sofa.getBackgroundImage());
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        EventBus.getDefault().unregister(this);

        super.onDetachedFromWindow();
    }

    @SuppressWarnings("unused")
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @SuppressWarnings("unused")
    public void setSofaData(Sofa sofa) {
        CammentSDK.getInstance().setShowMetadata(new ShowMetadata(sofa.getShowId(), sofa.getInvitationText()));

        this.sofa = sofa;

        displaySofaData();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        if (sofa != null) {
            Parcelable superState = super.onSaveInstanceState();

            SavedState ss = new SavedState(superState);

            ss.showId = sofa.getShowId();
            ss.backgroundImage = sofa.getBackgroundImage();
            ss.screenText = sofa.getScreenText();
            ss.invitationText = sofa.getInvitationText();
            ss.id = sofa.getId();
            ss.influencerCamment = sofa.getInfluencerCamment();
            ss.brandLogo = sofa.getBrandLogo();
            ss.targetUrl = sofa.getTargetUrl();

            return ss;
        } else {
            return super.onSaveInstanceState();
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        Sofa sofa = new Sofa();
        sofa.setShowId(ss.showId);
        sofa.setBackgroundImage(ss.backgroundImage);
        sofa.setScreenText(ss.screenText);
        sofa.setInvitationText(ss.invitationText);
        sofa.setId(ss.id);
        sofa.setInfluencerCamment(ss.influencerCamment);
        sofa.setBrandLogo(ss.brandLogo);
        sofa.setTargetUrl(ss.targetUrl);

        this.sofa = sofa;

        displaySofaData();
    }


    private void displaySofaData() {
        if (!TextUtils.isEmpty(sofa.getBackgroundImage())) {
            sofaBgImageView.setImageUrl(sofa.getBackgroundImage());
        }

        if (!TextUtils.isEmpty(sofa.getScreenText())) {
            tvInfo.setText(sofa.getScreenText());
        }

        if (!TextUtils.isEmpty(sofa.getInfluencerCamment())) {
            sofaCammentView.setCammentUrl(sofa.getInfluencerCamment());
        }

        if (!TextUtils.isEmpty(sofa.getBrandLogo())) {
            Glide.with(getContext()).load(sofa.getBrandLogo()).into(ivLogo);
        }
    }

    @Override
    public void onAddHeads(float[] values, float left, float top) {
        FrameLayout fl;
        ConstraintLayout.LayoutParams flParams;

        int squareSize = Math.round(140 * values[0]);

        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                fl = sofaCammentView;
            } else if (i == 1) {
                fl = sofaCameraView;
            } else {
                fl = sofaInviteView;
            }

            flParams = (ConstraintLayout.LayoutParams) fl.getLayoutParams();
            flParams.width = squareSize;
            flParams.height = squareSize;
            flParams.setMargins(Math.round(left + headRects[i] * values[0]), Math.round(top), 0, 0);

            fl.setLayoutParams(flParams);
        }

        sofaCammentView.setVisibility(VISIBLE);
        sofaCameraView.setVisibility(VISIBLE);
        sofaInviteView.setVisibility(VISIBLE);

        sofaCammentView.displayPlayIcon();

        displayCameraText();
    }

    private void displayCameraText() {
        if (PermissionHelper.getInstance().hasPermissions()) {
            tvCamera.setVisibility(GONE);
            ivCameraTextShadow.setVisibility(GONE);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvCamera.getLayoutParams();
                    params.topMargin = sofaCameraView.getBottom() + CommonUtils.dpToPx(getContext(), 8);
                    params.width = getMeasuredWidth() < getMeasuredHeight() ? sofaInviteView.getRight() - sofaCameraView.getLeft() : sofaInviteView.getRight() - sofaCammentView.getLeft();
                    params.width += CommonUtils.dpToPx(getContext(), 16);
                    params.height = Math.round(sofaCameraView.getHeight() * 0.8f);
                    tvCamera.setLayoutParams(params);

                    tvCamera.setVisibility(VISIBLE);
                    ivCameraTextShadow.setVisibility(VISIBLE);
                }
            });
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PermissionStatusChangedEvent event) {
        displayCameraText();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(HideSofaInviteProgress event) {
        sofaInviteView.hideProgressView();
    }

    public interface Listener {

        void onClose();

    }

    static class SavedState extends BaseSavedState {
        String showId;
        String backgroundImage;
        String screenText;
        String invitationText;
        String id;
        String influencerCamment;
        String brandLogo;
        String targetUrl;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.showId = in.readString();
            this.backgroundImage = in.readString();
            this.screenText = in.readString();
            this.invitationText = in.readString();
            this.id = in.readString();
            this.influencerCamment = in.readString();
            this.brandLogo = in.readString();
            this.targetUrl = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(showId);
            out.writeString(backgroundImage);
            out.writeString(screenText);
            out.writeString(invitationText);
            out.writeString(id);
            out.writeString(influencerCamment);
            out.writeString(brandLogo);
            out.writeString(targetUrl);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

}
