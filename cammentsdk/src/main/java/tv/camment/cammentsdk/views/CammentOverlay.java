package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.CammentList;
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

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.R;


public class CammentOverlay extends RelativeLayout implements CammentsAdapter.ActionListener {

    private static final int THRESHOLD = 100;

    private float startX;
    private float stopX;
    private float startY;
    private float stopY;

    private ViewGroup parentViewGroup;

    private CameraGLView cameraGLView;
    private RecyclerView rvCamments;
    private ImageButton ibRecord;

    private CammentsAdapter adapter;

    private SimpleExoPlayer player;
    private ExtractorMediaSource videoSource;
    private DefaultDataSourceFactory dataSourceFactory;
    private DefaultExtractorsFactory extractorsFactory;

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
        View.inflate(context, R.layout.cmmsdk_camment_overlay, this);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Camment"));
        extractorsFactory = new DefaultExtractorsFactory();

        player.setPlayWhenReady(true);
    }

    @Override
    protected void onFinishInflate() {
        cameraGLView = findViewById(R.id.camera_view);
        rvCamments = findViewById(R.id.rv_camments);
        ibRecord = findViewById(R.id.ib_record);

        adapter = new CammentsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvCamments.setLayoutManager(layoutManager);
        rvCamments.setAdapter(adapter);

        //TODO remove
        CammentList cammentList = new CammentList();
        List<Camment> camments = new ArrayList<>();
        Camment camment = new Camment();
        for (int i = 0; i < 5; i++) {
            camment.setThumbnail("https://images-production.global.ssl.fastly.net/uploads/posts/image/66942/corduroy-the-worlds-oldest-living-cat-square.jpg");
            camment.setUrl("http://www.sample-videos.com/video/mp4/480/big_buck_bunny_480p_1mb.mp4");
            camments.add(camment);
        }
        cammentList.setItems(camments);
        adapter.setData(cammentList);

        super.onFinishInflate();
    }

    @Override
    public void onCammentClick(Camment camment, TextureView textureView) {
        player.setVideoTextureView(textureView);
        videoSource = new ExtractorMediaSource(Uri.parse(camment.getUrl()), dataSourceFactory, extractorsFactory, null, null);
        player.prepare(videoSource);
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
                        Log.d("TOUCH", "SHOW!");
                        break;
                    case HIDE:
                        Log.d("TOUCH", "HIDE!");
                        break;
                }
                mode = Mode.NONE;
                break;
        }
    }


}
