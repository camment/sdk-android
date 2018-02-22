package tv.camment.cammentsdk.camera;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class CammentDefaultRenderersFactory implements RenderersFactory {
    public static final long DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS = 5000L;
    public static final int EXTENSION_RENDERER_MODE_OFF = 0;
    public static final int EXTENSION_RENDERER_MODE_ON = 1;
    public static final int EXTENSION_RENDERER_MODE_PREFER = 2;
    private static final String TAG = "CammentDefaultRenderersFactory";
    protected static final int MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY = 50;
    private final Context context;
    private final DrmSessionManager<FrameworkMediaCrypto> drmSessionManager;
    private final int extensionRendererMode;
    private final long allowedVideoJoiningTimeMs;

    public CammentDefaultRenderersFactory(Context context) {
        this(context, (DrmSessionManager) null);
    }

    public CammentDefaultRenderersFactory(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        this(context, drmSessionManager, 0);
    }

    public CammentDefaultRenderersFactory(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, int extensionRendererMode) {
        this(context, drmSessionManager, extensionRendererMode, 5000L);
    }

    public CammentDefaultRenderersFactory(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, int extensionRendererMode, long allowedVideoJoiningTimeMs) {
        this.context = context;
        this.drmSessionManager = drmSessionManager;
        this.extensionRendererMode = extensionRendererMode;
        this.allowedVideoJoiningTimeMs = allowedVideoJoiningTimeMs;
    }

    public Renderer[] createRenderers(Handler eventHandler, VideoRendererEventListener videoRendererEventListener, AudioRendererEventListener audioRendererEventListener, TextRenderer.Output textRendererOutput, com.google.android.exoplayer2.metadata.MetadataRenderer.Output metadataRendererOutput) {
        ArrayList<Renderer> renderersList = new ArrayList();
        this.buildVideoRenderers(this.context, this.drmSessionManager, this.allowedVideoJoiningTimeMs, eventHandler, videoRendererEventListener, this.extensionRendererMode, renderersList);
        this.buildAudioRenderers(this.context, this.drmSessionManager, this.buildAudioProcessors(), eventHandler, audioRendererEventListener, this.extensionRendererMode, renderersList);
        this.buildTextRenderers(this.context, textRendererOutput, eventHandler.getLooper(), this.extensionRendererMode, renderersList);
        this.buildMetadataRenderers(this.context, metadataRendererOutput, eventHandler.getLooper(), this.extensionRendererMode, renderersList);
        this.buildMiscellaneousRenderers(this.context, eventHandler, this.extensionRendererMode, renderersList);
        return (Renderer[]) renderersList.toArray(new Renderer[renderersList.size()]);
    }

    protected void buildVideoRenderers(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, long allowedVideoJoiningTimeMs, Handler eventHandler, VideoRendererEventListener eventListener, int extensionRendererMode, ArrayList<Renderer> out) {
        out.add(new CammentMediaCodecVideoRenderer(context, MediaCodecSelector.DEFAULT, allowedVideoJoiningTimeMs, drmSessionManager, false, eventHandler, eventListener, 50));
        if (extensionRendererMode != 0) {
            int extensionRendererIndex = out.size();
            if (extensionRendererMode == 2) {
                --extensionRendererIndex;
            }

            try {
                Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer");
                Constructor<?> constructor = clazz.getConstructor(new Class[]{Boolean.TYPE, Long.TYPE, Handler.class, VideoRendererEventListener.class, Integer.TYPE});
                Renderer renderer = (Renderer) constructor.newInstance(new Object[]{Boolean.valueOf(true), Long.valueOf(allowedVideoJoiningTimeMs), eventHandler, eventListener, Integer.valueOf(50)});
                out.add(extensionRendererIndex++, renderer);
                Log.i("DefaultRenderersFactory", "Loaded LibvpxVideoRenderer.");
            } catch (ClassNotFoundException var13) {
                ;
            } catch (Exception var14) {
                throw new RuntimeException(var14);
            }

        }
    }

    protected void buildAudioRenderers(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AudioProcessor[] audioProcessors, Handler eventHandler, AudioRendererEventListener eventListener, int extensionRendererMode, ArrayList<Renderer> out) {
        out.add(new MediaCodecAudioRenderer(MediaCodecSelector.DEFAULT, drmSessionManager, true, eventHandler, eventListener, AudioCapabilities.getCapabilities(context), audioProcessors));
        if (extensionRendererMode != 0) {
            int extensionRendererIndex = out.size();
            if (extensionRendererMode == 2) {
                --extensionRendererIndex;
            }

            Class clazz;
            Constructor constructor;
            Renderer renderer;
            try {
                clazz = Class.forName("com.google.android.exoplayer2.ext.opus.LibopusAudioRenderer");
                constructor = clazz.getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class});
                renderer = (Renderer) constructor.newInstance(new Object[]{eventHandler, eventListener, audioProcessors});
                out.add(extensionRendererIndex++, renderer);
                Log.i("DefaultRenderersFactory", "Loaded LibopusAudioRenderer.");
            } catch (ClassNotFoundException var16) {
                ;
            } catch (Exception var17) {
                throw new RuntimeException(var17);
            }

            try {
                clazz = Class.forName("com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer");
                constructor = clazz.getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class});
                renderer = (Renderer) constructor.newInstance(new Object[]{eventHandler, eventListener, audioProcessors});
                out.add(extensionRendererIndex++, renderer);
                Log.i("DefaultRenderersFactory", "Loaded LibflacAudioRenderer.");
            } catch (ClassNotFoundException var14) {
                ;
            } catch (Exception var15) {
                throw new RuntimeException(var15);
            }

            try {
                clazz = Class.forName("com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer");
                constructor = clazz.getConstructor(new Class[]{Handler.class, AudioRendererEventListener.class, AudioProcessor[].class});
                renderer = (Renderer) constructor.newInstance(new Object[]{eventHandler, eventListener, audioProcessors});
                out.add(extensionRendererIndex++, renderer);
                Log.i("DefaultRenderersFactory", "Loaded FfmpegAudioRenderer.");
            } catch (ClassNotFoundException var12) {
                ;
            } catch (Exception var13) {
                throw new RuntimeException(var13);
            }

        }
    }

    protected void buildTextRenderers(Context context, TextRenderer.Output output, Looper outputLooper, int extensionRendererMode, ArrayList<Renderer> out) {
        out.add(new TextRenderer(output, outputLooper));
    }

    protected void buildMetadataRenderers(Context context, com.google.android.exoplayer2.metadata.MetadataRenderer.Output output, Looper outputLooper, int extensionRendererMode, ArrayList<Renderer> out) {
        out.add(new MetadataRenderer(output, outputLooper));
    }

    protected void buildMiscellaneousRenderers(Context context, Handler eventHandler, int extensionRendererMode, ArrayList<Renderer> out) {
    }

    protected AudioProcessor[] buildAudioProcessors() {
        return new AudioProcessor[0];
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ExtensionRendererMode {
    }
}

