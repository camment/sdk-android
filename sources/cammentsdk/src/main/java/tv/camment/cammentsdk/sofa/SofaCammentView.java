package tv.camment.cammentsdk.sofa;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.camera.CDefaultRenderersFactory;
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
import tv.camment.cammentsdk.utils.ExoCacheUtils;

public class SofaCammentView extends FrameLayout {

    private TextureView textureView;
    private ImageView ivPlay;

    private SimpleExoPlayer player;
    private DefaultDataSourceFactory dataSourceFactory;
    private CacheDataSourceFactory cacheDataSourceFactory;
    private DefaultExtractorsFactory extractorsFactory;

    public SofaCammentView(Context context) {
        this(context, null);
    }

    public SofaCammentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.cmmsdk_sofa_camment_view, this);

        textureView = findViewById(R.id.cmmsdk_texture_view);
        ivPlay = findViewById(R.id.cmmsdk_iv_play);

        initPlayer();
    }

    private void initPlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        RenderersFactory renderersFactory = new CDefaultRenderersFactory(getContext());

        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

        dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "Camment"));

        cacheDataSourceFactory = new CacheDataSourceFactory(ExoCacheUtils.getInstance().getCache(), dataSourceFactory);
        extractorsFactory = new DefaultExtractorsFactory();

        player.setPlayWhenReady(false);
        player.setVideoTextureView(textureView);
    }

    public void setCammentUrl(String cammentUrl) {
        ExtractorMediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(cammentUrl));
        player.prepare(videoSource);
    }

    public void setPlayerListener(Player.EventListener exoEventListener) {
        if (player != null && exoEventListener != null) {
            player.addListener(exoEventListener);
        }
    }

    public void play() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }

        if (ivPlay != null) {
            ivPlay.setVisibility(GONE);
        }
    }

    public void stop() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.seekTo(0);
        }

        if (ivPlay != null) {
            ivPlay.setVisibility(VISIBLE);
        }
    }

    public void displayPlayIcon() {
        post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams ivPlayParams = (FrameLayout.LayoutParams) ivPlay.getLayoutParams();
                ivPlayParams.width = getMeasuredHeight() / 4;
                ivPlayParams.height = getMeasuredHeight() / 4;
                ivPlayParams.setMargins(ivPlayParams.width / 3, 0, 0, ivPlayParams.width / 3);
                ivPlay.setLayoutParams(ivPlayParams);
            }
        });
    }

}
