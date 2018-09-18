package tv.camment.cammentsdk.camera;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import tv.camment.cammentsdk.exoplayer.DefaultRenderersFactory;
import tv.camment.cammentsdk.exoplayer.Renderer;
import tv.camment.cammentsdk.exoplayer.RenderersFactory;
import tv.camment.cammentsdk.exoplayer.audio.AudioCapabilities;
import tv.camment.cammentsdk.exoplayer.audio.AudioProcessor;
import tv.camment.cammentsdk.exoplayer.audio.AudioRendererEventListener;
import tv.camment.cammentsdk.exoplayer.audio.MediaCodecAudioRenderer;
import tv.camment.cammentsdk.exoplayer.drm.DrmSessionManager;
import tv.camment.cammentsdk.exoplayer.drm.FrameworkMediaCrypto;
import tv.camment.cammentsdk.exoplayer.mediacodec.MediaCodecSelector;
import tv.camment.cammentsdk.exoplayer.metadata.MetadataOutput;
import tv.camment.cammentsdk.exoplayer.metadata.MetadataRenderer;
import tv.camment.cammentsdk.exoplayer.text.TextOutput;
import tv.camment.cammentsdk.exoplayer.text.TextRenderer;
import tv.camment.cammentsdk.exoplayer.trackselection.TrackSelector;
import tv.camment.cammentsdk.exoplayer.video.VideoRendererEventListener;

public final class CDefaultRenderersFactory implements RenderersFactory {

    /**
     * The default maximum duration for which a video renderer can attempt to seamlessly join an
     * ongoing playback.
     */
    public static final long DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS = 5000;

    /**
     * Modes for using extension renderers.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EXTENSION_RENDERER_MODE_OFF, EXTENSION_RENDERER_MODE_ON,
            EXTENSION_RENDERER_MODE_PREFER})
    public @interface ExtensionRendererMode {
    }

    /**
     * Do not allow use of extension renderers.
     */
    public static final int EXTENSION_RENDERER_MODE_OFF = 0;
    /**
     * Allow use of extension renderers. Extension renderers are indexed after core renderers of the
     * same type. A {@link TrackSelector} that prefers the first suitable renderer will therefore
     * prefer to use a core renderer to an extension renderer in the case that both are able to play
     * a given track.
     */
    public static final int EXTENSION_RENDERER_MODE_ON = 1;
    /**
     * Allow use of extension renderers. Extension renderers are indexed before core renderers of the
     * same type. A {@link TrackSelector} that prefers the first suitable renderer will therefore
     * prefer to use an extension renderer to a core renderer in the case that both are able to play
     * a given track.
     */
    public static final int EXTENSION_RENDERER_MODE_PREFER = 2;

    private static final String TAG = "CRenderersFactory";

    protected static final int MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY = 50;

    private final Context context;
    @Nullable
    private final DrmSessionManager<FrameworkMediaCrypto> drmSessionManager;
    private final @DefaultRenderersFactory.ExtensionRendererMode
    int extensionRendererMode;
    private final long allowedVideoJoiningTimeMs;

    /**
     * @param context A {@link Context}.
     */
    public CDefaultRenderersFactory(Context context) {
        this(context, null);
    }

    /**
     * @param context           A {@link Context}.
     * @param drmSessionManager An optional {@link DrmSessionManager}. May be null if DRM protected
     *                          playbacks are not required.
     */
    public CDefaultRenderersFactory(Context context,
                                    @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        this(context, drmSessionManager, EXTENSION_RENDERER_MODE_OFF);
    }

    /**
     * @param context               A {@link Context}.
     * @param drmSessionManager     An optional {@link DrmSessionManager}. May be null if DRM protected
     *                              playbacks are not required.
     * @param extensionRendererMode The extension renderer mode, which determines if and how
     *                              available extension renderers are used. Note that extensions must be included in the
     *                              application build for them to be considered available.
     */
    public CDefaultRenderersFactory(Context context,
                                    @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                    @CDefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode) {
        this(context, drmSessionManager, extensionRendererMode, DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
    }

    /**
     * @param context                   A {@link Context}.
     * @param drmSessionManager         An optional {@link DrmSessionManager}. May be null if DRM protected
     *                                  playbacks are not required.
     * @param extensionRendererMode     The extension renderer mode, which determines if and how
     *                                  available extension renderers are used. Note that extensions must be included in the
     *                                  application build for them to be considered available.
     * @param allowedVideoJoiningTimeMs The maximum duration for which video renderers can attempt
     *                                  to seamlessly join an ongoing playback.
     */
    public CDefaultRenderersFactory(Context context,
                                    @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                    @CDefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode, long allowedVideoJoiningTimeMs) {
        this.context = context;
        this.drmSessionManager = drmSessionManager;
        this.extensionRendererMode = extensionRendererMode;
        this.allowedVideoJoiningTimeMs = allowedVideoJoiningTimeMs;
    }

    @Override
    public Renderer[] createRenderers(Handler eventHandler,
                                      VideoRendererEventListener videoRendererEventListener,
                                      AudioRendererEventListener audioRendererEventListener,
                                      TextOutput textRendererOutput, MetadataOutput metadataRendererOutput) {
        ArrayList<Renderer> renderersList = new ArrayList<>();
        buildVideoRenderers(context, drmSessionManager, allowedVideoJoiningTimeMs,
                eventHandler, videoRendererEventListener, extensionRendererMode, renderersList);
        buildAudioRenderers(context, drmSessionManager, buildAudioProcessors(),
                eventHandler, audioRendererEventListener, extensionRendererMode, renderersList);
        buildTextRenderers(context, textRendererOutput, eventHandler.getLooper(),
                extensionRendererMode, renderersList);
        buildMetadataRenderers(context, metadataRendererOutput, eventHandler.getLooper(),
                extensionRendererMode, renderersList);
        buildMiscellaneousRenderers(context, eventHandler, extensionRendererMode, renderersList);
        return renderersList.toArray(new Renderer[renderersList.size()]);
    }

    /**
     * Builds video renderers for use by the player.
     *
     * @param context                   The {@link Context} associated with the player.
     * @param drmSessionManager         An optional {@link DrmSessionManager}. May be null if the player
     *                                  will not be used for DRM protected playbacks.
     * @param allowedVideoJoiningTimeMs The maximum duration in milliseconds for which video
     *                                  renderers can attempt to seamlessly join an ongoing playback.
     * @param eventHandler              A handler associated with the main thread's looper.
     * @param eventListener             An event listener.
     * @param extensionRendererMode     The extension renderer mode.
     * @param out                       An array to which the built renderers should be appended.
     */
    protected void buildVideoRenderers(Context context,
                                       @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                       long allowedVideoJoiningTimeMs, Handler eventHandler,
                                       VideoRendererEventListener eventListener, @CDefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode,
                                       ArrayList<Renderer> out) {
        out.add(new CMediaCodecVideoRenderer(context, MediaCodecSelector.DEFAULT,
                allowedVideoJoiningTimeMs, drmSessionManager, false, eventHandler, eventListener,
                MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY));

        if (extensionRendererMode == EXTENSION_RENDERER_MODE_OFF) {
            return;
        }
        int extensionRendererIndex = out.size();
        if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) {
            extensionRendererIndex--;
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            // LINT.IfChange
            Class<?> clazz = Class.forName("tv.camment.cammentsdk.exoplayer.ext.vp9.LibvpxVideoRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            boolean.class,
                            long.class,
                            android.os.Handler.class,
                            tv.camment.cammentsdk.exoplayer.video.VideoRendererEventListener.class,
                            int.class);
            // LINT.ThenChange(../../../../../../../proguard-rules.txt)
            Renderer renderer =
                    (Renderer)
                            constructor.newInstance(
                                    true,
                                    allowedVideoJoiningTimeMs,
                                    eventHandler,
                                    eventListener,
                                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
            out.add(extensionRendererIndex++, renderer);
            Log.i(TAG, "Loaded LibvpxVideoRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating VP9 extension", e);
        }
    }

    /**
     * Builds audio renderers for use by the player.
     *
     * @param context               The {@link Context} associated with the player.
     * @param drmSessionManager     An optional {@link DrmSessionManager}. May be null if the player
     *                              will not be used for DRM protected playbacks.
     * @param audioProcessors       An array of {@link AudioProcessor}s that will process PCM audio
     *                              buffers before output. May be empty.
     * @param eventHandler          A handler to use when invoking event listeners and outputs.
     * @param eventListener         An event listener.
     * @param extensionRendererMode The extension renderer mode.
     * @param out                   An array to which the built renderers should be appended.
     */
    protected void buildAudioRenderers(Context context,
                                       @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                       AudioProcessor[] audioProcessors, Handler eventHandler,
                                       AudioRendererEventListener eventListener, @CDefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode,
                                       ArrayList<Renderer> out) {
        out.add(new MediaCodecAudioRenderer(MediaCodecSelector.DEFAULT, drmSessionManager, true,
                eventHandler, eventListener, AudioCapabilities.getCapabilities(context), audioProcessors));

        if (extensionRendererMode == EXTENSION_RENDERER_MODE_OFF) {
            return;
        }
        int extensionRendererIndex = out.size();
        if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) {
            extensionRendererIndex--;
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            // LINT.IfChange
            Class<?> clazz = Class.forName("tv.camment.cammentsdk.exoplayer.ext.opus.LibopusAudioRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            android.os.Handler.class,
                            tv.camment.cammentsdk.exoplayer.audio.AudioRendererEventListener.class,
                            tv.camment.cammentsdk.exoplayer.audio.AudioProcessor[].class);
            // LINT.ThenChange(../../../../../../../proguard-rules.txt)
            Renderer renderer =
                    (Renderer) constructor.newInstance(eventHandler, eventListener, audioProcessors);
            out.add(extensionRendererIndex++, renderer);
            Log.i(TAG, "Loaded LibopusAudioRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating Opus extension", e);
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            // LINT.IfChange
            Class<?> clazz = Class.forName("tv.camment.cammentsdk.exoplayer.ext.flac.LibflacAudioRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            android.os.Handler.class,
                            tv.camment.cammentsdk.exoplayer.audio.AudioRendererEventListener.class,
                            tv.camment.cammentsdk.exoplayer.audio.AudioProcessor[].class);
            // LINT.ThenChange(../../../../../../../proguard-rules.txt)
            Renderer renderer =
                    (Renderer) constructor.newInstance(eventHandler, eventListener, audioProcessors);
            out.add(extensionRendererIndex++, renderer);
            Log.i(TAG, "Loaded LibflacAudioRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating FLAC extension", e);
        }

        try {
            // Full class names used for constructor args so the LINT rule triggers if any of them move.
            // LINT.IfChange
            Class<?> clazz =
                    Class.forName("tv.camment.cammentsdk.exoplayer.ext.ffmpeg.FfmpegAudioRenderer");
            Constructor<?> constructor =
                    clazz.getConstructor(
                            android.os.Handler.class,
                            tv.camment.cammentsdk.exoplayer.audio.AudioRendererEventListener.class,
                            tv.camment.cammentsdk.exoplayer.audio.AudioProcessor[].class);
            // LINT.ThenChange(../../../../../../../proguard-rules.txt)
            Renderer renderer =
                    (Renderer) constructor.newInstance(eventHandler, eventListener, audioProcessors);
            out.add(extensionRendererIndex++, renderer);
            Log.i(TAG, "Loaded FfmpegAudioRenderer.");
        } catch (ClassNotFoundException e) {
            // Expected if the app was built without the extension.
        } catch (Exception e) {
            // The extension is present, but instantiation failed.
            throw new RuntimeException("Error instantiating FFmpeg extension", e);
        }
    }

    /**
     * Builds text renderers for use by the player.
     *
     * @param context               The {@link Context} associated with the player.
     * @param output                An output for the renderers.
     * @param outputLooper          The looper associated with the thread on which the output should be
     *                              called.
     * @param extensionRendererMode The extension renderer mode.
     * @param out                   An array to which the built renderers should be appended.
     */
    protected void buildTextRenderers(Context context, TextOutput output, Looper outputLooper,
                                      @CDefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode,
                                      ArrayList<Renderer> out) {
        out.add(new TextRenderer(output, outputLooper));
    }

    /**
     * Builds metadata renderers for use by the player.
     *
     * @param context               The {@link Context} associated with the player.
     * @param output                An output for the renderers.
     * @param outputLooper          The looper associated with the thread on which the output should be
     *                              called.
     * @param extensionRendererMode The extension renderer mode.
     * @param out                   An array to which the built renderers should be appended.
     */
    protected void buildMetadataRenderers(Context context, MetadataOutput output, Looper outputLooper,
                                          @CDefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode, ArrayList<Renderer> out) {
        out.add(new MetadataRenderer(output, outputLooper));
    }

    /**
     * Builds any miscellaneous renderers used by the player.
     *
     * @param context               The {@link Context} associated with the player.
     * @param eventHandler          A handler to use when invoking event listeners and outputs.
     * @param extensionRendererMode The extension renderer mode.
     * @param out                   An array to which the built renderers should be appended.
     */
    protected void buildMiscellaneousRenderers(Context context, Handler eventHandler,
                                               @CDefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode, ArrayList<Renderer> out) {
        // Do nothing.
    }

    /**
     * Builds an array of {@link AudioProcessor}s that will process PCM audio before output.
     */
    protected AudioProcessor[] buildAudioProcessors() {
        return new AudioProcessor[0];
    }

}
