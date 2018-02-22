package tv.camment.cammentsdk.camera;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.TraceUtil;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.DummySurface;
import com.google.android.exoplayer2.video.VideoFrameReleaseTimeHelper;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.nio.ByteBuffer;


public final class CammentMediaCodecVideoRenderer extends CammentMediaCodecRenderer {
    private static final String TAG = "CammentMediaCodecVideoRenderer";
    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";
    private static final String KEY_CROP_TOP = "crop-top";
    private static final int[] STANDARD_LONG_EDGE_VIDEO_PX = new int[]{1920, 1600, 1440, 1280, 960, 854, 640, 540, 480};
    private static final int MAX_PENDING_OUTPUT_STREAM_OFFSET_COUNT = 10;
    private final Context context;
    private final VideoFrameReleaseTimeHelper frameReleaseTimeHelper;
    private final VideoRendererEventListener.EventDispatcher eventDispatcher;
    private final long allowedJoiningTimeMs;
    private final int maxDroppedFramesToNotify;
    private final boolean deviceNeedsAutoFrcWorkaround;
    private final long[] pendingOutputStreamOffsetsUs;
    private Format[] streamFormats;
    private CammentMediaCodecVideoRenderer.CodecMaxValues codecMaxValues;
    private boolean codecNeedsSetOutputSurfaceWorkaround;
    private Surface surface;
    private Surface dummySurface;
    private int scalingMode;
    private boolean renderedFirstFrame;
    private long joiningDeadlineMs;
    private long droppedFrameAccumulationStartTimeMs;
    private int droppedFrames;
    private int consecutiveDroppedFrameCount;
    private int pendingRotationDegrees;
    private float pendingPixelWidthHeightRatio;
    private int currentWidth;
    private int currentHeight;
    private int currentUnappliedRotationDegrees;
    private float currentPixelWidthHeightRatio;
    private int reportedWidth;
    private int reportedHeight;
    private int reportedUnappliedRotationDegrees;
    private float reportedPixelWidthHeightRatio;
    private boolean tunneling;
    private int tunnelingAudioSessionId;
    CammentMediaCodecVideoRenderer.OnFrameRenderedListenerV23 tunnelingOnFrameRenderedListener;
    private long outputStreamOffsetUs;
    private int pendingOutputStreamOffsetCount;

    public CammentMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector) {
        this(context, mediaCodecSelector, 0L);
    }

    public CammentMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs) {
        this(context, mediaCodecSelector, allowedJoiningTimeMs, (Handler) null, (VideoRendererEventListener) null, -1);
    }

    public CammentMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, Handler eventHandler, VideoRendererEventListener eventListener, int maxDroppedFrameCountToNotify) {
        this(context, mediaCodecSelector, allowedJoiningTimeMs, (DrmSessionManager) null, false, eventHandler, eventListener, maxDroppedFrameCountToNotify);
    }

    public CammentMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys, Handler eventHandler, VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
        super(2, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys);
        this.allowedJoiningTimeMs = allowedJoiningTimeMs;
        this.maxDroppedFramesToNotify = maxDroppedFramesToNotify;
        this.context = context.getApplicationContext();
        this.frameReleaseTimeHelper = new VideoFrameReleaseTimeHelper(context);
        this.eventDispatcher = new VideoRendererEventListener.EventDispatcher(eventHandler, eventListener);
        this.deviceNeedsAutoFrcWorkaround = deviceNeedsAutoFrcWorkaround();
        this.pendingOutputStreamOffsetsUs = new long[10];
        this.outputStreamOffsetUs = -9223372036854775807L;
        this.joiningDeadlineMs = -9223372036854775807L;
        this.currentWidth = -1;
        this.currentHeight = -1;
        this.currentPixelWidthHeightRatio = -1.0F;
        this.pendingPixelWidthHeightRatio = -1.0F;
        this.scalingMode = 1;
        this.clearReportedVideoSize();
    }

    protected int supportsFormat(MediaCodecSelector mediaCodecSelector, Format format) throws MediaCodecUtil.DecoderQueryException {
        String mimeType = format.sampleMimeType;
        if (!MimeTypes.isVideo(mimeType)) {
            return 0;
        } else {
            boolean requiresSecureDecryption = false;
            DrmInitData drmInitData = format.drmInitData;
            if (drmInitData != null) {
                for (int i = 0; i < drmInitData.schemeDataCount; ++i) {
                    requiresSecureDecryption |= drmInitData.get(i).requiresSecureDecryption;
                }
            }

            MediaCodecInfo decoderInfo = mediaCodecSelector.getDecoderInfo(mimeType, requiresSecureDecryption);
            if (decoderInfo == null) {
                return 1;
            } else {
                boolean decoderCapable = decoderInfo.isCodecSupported(format.codecs);
                if (decoderCapable && format.width > 0 && format.height > 0) {
                    if (Util.SDK_INT >= 21) {
                        decoderCapable = decoderInfo.isVideoSizeAndRateSupportedV21(format.width, format.height, (double) format.frameRate);
                    } else {
                        decoderCapable = format.width * format.height <= MediaCodecUtil.maxH264DecodableFrameSize();
                        if (!decoderCapable) {
                            Log.d("MediaCodecVideoRenderer", "FalseCheck [legacyFrameSize, " + format.width + "x" + format.height + "] [" + Util.DEVICE_DEBUG_INFO + "]");
                        }
                    }
                }

                int adaptiveSupport = decoderInfo.adaptive ? 16 : 8;
                int tunnelingSupport = decoderInfo.tunneling ? 32 : 0;
                int formatSupport = decoderCapable ? 4 : 3;
                return adaptiveSupport | tunnelingSupport | formatSupport;
            }
        }
    }

    protected void onEnabled(boolean joining) throws ExoPlaybackException {
        super.onEnabled(joining);
        this.tunnelingAudioSessionId = this.getConfiguration().tunnelingAudioSessionId;
        this.tunneling = this.tunnelingAudioSessionId != 0;
        this.eventDispatcher.enabled(this.decoderCounters);
        this.frameReleaseTimeHelper.enable();
    }

    protected void onStreamChanged(Format[] formats, long offsetUs) throws ExoPlaybackException {
        this.streamFormats = formats;
        if (this.outputStreamOffsetUs == -9223372036854775807L) {
            this.outputStreamOffsetUs = offsetUs;
        } else {
            if (this.pendingOutputStreamOffsetCount == this.pendingOutputStreamOffsetsUs.length) {
                Log.w("MediaCodecVideoRenderer", "Too many stream changes, so dropping offset: " + this.pendingOutputStreamOffsetsUs[this.pendingOutputStreamOffsetCount - 1]);
            } else {
                ++this.pendingOutputStreamOffsetCount;
            }

            this.pendingOutputStreamOffsetsUs[this.pendingOutputStreamOffsetCount - 1] = offsetUs;
        }

        super.onStreamChanged(formats, offsetUs);
    }

    protected void onPositionReset(long positionUs, boolean joining) throws ExoPlaybackException {
        super.onPositionReset(positionUs, joining);
        this.clearRenderedFirstFrame();
        this.consecutiveDroppedFrameCount = 0;
        if (this.pendingOutputStreamOffsetCount != 0) {
            this.outputStreamOffsetUs = this.pendingOutputStreamOffsetsUs[this.pendingOutputStreamOffsetCount - 1];
            this.pendingOutputStreamOffsetCount = 0;
        }

        if (joining) {
            this.setJoiningDeadlineMs();
        } else {
            this.joiningDeadlineMs = -9223372036854775807L;
        }

    }

    public boolean isReady() {
        if (super.isReady() && (this.renderedFirstFrame || this.dummySurface != null && this.surface == this.dummySurface || this.getCodec() == null || this.tunneling)) {
            this.joiningDeadlineMs = -9223372036854775807L;
            return true;
        } else if (this.joiningDeadlineMs == -9223372036854775807L) {
            return false;
        } else if (SystemClock.elapsedRealtime() < this.joiningDeadlineMs) {
            return true;
        } else {
            this.joiningDeadlineMs = -9223372036854775807L;
            return false;
        }
    }

    protected void onStarted() {
        super.onStarted();
        this.droppedFrames = 0;
        this.droppedFrameAccumulationStartTimeMs = SystemClock.elapsedRealtime();
    }

    protected void onStopped() {
        this.joiningDeadlineMs = -9223372036854775807L;
        this.maybeNotifyDroppedFrames();
        super.onStopped();
    }

    protected void onDisabled() {
        this.currentWidth = -1;
        this.currentHeight = -1;
        this.currentPixelWidthHeightRatio = -1.0F;
        this.pendingPixelWidthHeightRatio = -1.0F;
        this.outputStreamOffsetUs = -9223372036854775807L;
        this.pendingOutputStreamOffsetCount = 0;
        this.clearReportedVideoSize();
        this.clearRenderedFirstFrame();
        this.frameReleaseTimeHelper.disable();
        this.tunnelingOnFrameRenderedListener = null;
        this.tunneling = false;

        try {
            super.onDisabled();
        } finally {
            this.decoderCounters.ensureUpdated();
            this.eventDispatcher.disabled(this.decoderCounters);
        }

    }

    public void handleMessage(int messageType, Object message) throws ExoPlaybackException {
        if (messageType == 1) {
            this.setSurface((Surface) message);
        } else if (messageType == 4) {
            this.scalingMode = ((Integer) message).intValue();
            MediaCodec codec = this.getCodec();
            if (codec != null) {
                setVideoScalingMode(codec, this.scalingMode);
            }
        } else {
            super.handleMessage(messageType, message);
        }

    }

    private void setSurface(Surface surface) throws ExoPlaybackException {
        if (surface == null) {
            if (this.dummySurface != null) {
                surface = this.dummySurface;
            } else {
                MediaCodecInfo codecInfo = this.getCodecInfo();
                if (codecInfo != null && this.shouldUseDummySurface(codecInfo.secure)) {
                    this.dummySurface = DummySurface.newInstanceV17(this.context, codecInfo.secure);
                    surface = this.dummySurface;
                }
            }
        }

        if (this.surface != surface) {
            this.surface = surface;
            int state = this.getState();
            if (state == 1 || state == 2) {
                MediaCodec codec = this.getCodec();
                if (Util.SDK_INT >= 23 && codec != null && surface != null && !this.codecNeedsSetOutputSurfaceWorkaround) {
                    setOutputSurfaceV23(codec, surface);
                } else {
                    this.releaseCodec();
                    this.maybeInitCodec();
                }
            }

            if (surface != null && surface != this.dummySurface) {
                this.maybeRenotifyVideoSizeChanged();
                this.clearRenderedFirstFrame();
                if (state == 2) {
                    this.setJoiningDeadlineMs();
                }
            } else {
                this.clearReportedVideoSize();
                this.clearRenderedFirstFrame();
            }
        } else if (surface != null && surface != this.dummySurface) {
            this.maybeRenotifyVideoSizeChanged();
            this.maybeRenotifyRenderedFirstFrame();
        }

    }

    protected boolean shouldInitCodec(MediaCodecInfo codecInfo) {
        return this.surface != null || this.shouldUseDummySurface(codecInfo.secure);
    }

    protected void configureCodec(MediaCodecInfo codecInfo, MediaCodec codec, Format format, MediaCrypto crypto) throws MediaCodecUtil.DecoderQueryException {
        this.codecMaxValues = this.getCodecMaxValues(codecInfo, format, this.streamFormats);
        MediaFormat mediaFormat = this.getMediaFormat(format, this.codecMaxValues, this.deviceNeedsAutoFrcWorkaround, this.tunnelingAudioSessionId);
        if (this.surface == null) {
            Assertions.checkState(this.shouldUseDummySurface(codecInfo.secure));
            if (this.dummySurface == null) {
                this.dummySurface = DummySurface.newInstanceV17(this.context, codecInfo.secure);
            }

            this.surface = this.dummySurface;
        }

        codec.configure(mediaFormat, this.surface, crypto, 0);
        if (Util.SDK_INT >= 23 && this.tunneling) {
            this.tunnelingOnFrameRenderedListener = new CammentMediaCodecVideoRenderer.OnFrameRenderedListenerV23(codec);
        }

    }

    protected void releaseCodec() {
        try {
            super.releaseCodec();
        } finally {
            if (this.dummySurface != null) {
                if (this.surface == this.dummySurface) {
                    this.surface = null;
                }

                this.dummySurface.release();
                this.dummySurface = null;
            }

        }

    }

    protected void onCodecInitialized(String name, long initializedTimestampMs, long initializationDurationMs) {
        this.eventDispatcher.decoderInitialized(name, initializedTimestampMs, initializationDurationMs);
        this.codecNeedsSetOutputSurfaceWorkaround = codecNeedsSetOutputSurfaceWorkaround(name);
    }

    protected void onInputFormatChanged(Format newFormat) throws ExoPlaybackException {
        super.onInputFormatChanged(newFormat);
        this.eventDispatcher.inputFormatChanged(newFormat);
        this.pendingPixelWidthHeightRatio = getPixelWidthHeightRatio(newFormat);
        this.pendingRotationDegrees = getRotationDegrees(newFormat);
    }

    protected void onQueueInputBuffer(DecoderInputBuffer buffer) {
        if (Util.SDK_INT < 23 && this.tunneling) {
            this.maybeNotifyRenderedFirstFrame();
        }

    }

    protected void onOutputFormatChanged(MediaCodec codec, MediaFormat outputFormat) {
        boolean hasCrop = outputFormat.containsKey("crop-right") && outputFormat.containsKey("crop-left") && outputFormat.containsKey("crop-bottom") && outputFormat.containsKey("crop-top");
        this.currentWidth = hasCrop ? outputFormat.getInteger("crop-right") - outputFormat.getInteger("crop-left") + 1 : outputFormat.getInteger("width");
        this.currentHeight = hasCrop ? outputFormat.getInteger("crop-bottom") - outputFormat.getInteger("crop-top") + 1 : outputFormat.getInteger("height");
        this.currentPixelWidthHeightRatio = this.pendingPixelWidthHeightRatio;
        if (Util.SDK_INT >= 21) {
            if (this.pendingRotationDegrees == 90 || this.pendingRotationDegrees == 270) {
                int rotatedHeight = this.currentWidth;
                this.currentWidth = this.currentHeight;
                this.currentHeight = rotatedHeight;
                this.currentPixelWidthHeightRatio = 1.0F / this.currentPixelWidthHeightRatio;
            }
        } else {
            this.currentUnappliedRotationDegrees = this.pendingRotationDegrees;
        }

        setVideoScalingMode(codec, this.scalingMode);
    }

    protected boolean canReconfigureCodec(MediaCodec codec, boolean codecIsAdaptive, Format oldFormat, Format newFormat) {
        return areAdaptationCompatible(codecIsAdaptive, oldFormat, newFormat) && newFormat.width <= this.codecMaxValues.width && newFormat.height <= this.codecMaxValues.height && getMaxInputSize(newFormat) <= this.codecMaxValues.inputSize;
    }

    protected boolean processOutputBuffer(long positionUs, long elapsedRealtimeUs, MediaCodec codec, ByteBuffer buffer, int bufferIndex, int bufferFlags, long bufferPresentationTimeUs, boolean shouldSkip) {
        while (this.pendingOutputStreamOffsetCount != 0 && bufferPresentationTimeUs >= this.pendingOutputStreamOffsetsUs[0]) {
            this.outputStreamOffsetUs = this.pendingOutputStreamOffsetsUs[0];
            --this.pendingOutputStreamOffsetCount;
            System.arraycopy(this.pendingOutputStreamOffsetsUs, 1, this.pendingOutputStreamOffsetsUs, 0, this.pendingOutputStreamOffsetCount);
        }

        long presentationTimeUs = bufferPresentationTimeUs - this.outputStreamOffsetUs;
        if (shouldSkip) {
            this.skipOutputBuffer(codec, bufferIndex, presentationTimeUs);
            return true;
        } else {
            long earlyUs = bufferPresentationTimeUs - positionUs;
            if (this.surface == this.dummySurface) {
                if (isBufferLate(earlyUs)) {
                    this.skipOutputBuffer(codec, bufferIndex, presentationTimeUs);
                    return true;
                } else {
                    return false;
                }
            } else if (!this.renderedFirstFrame) {
                if (Util.SDK_INT >= 21) {
                    this.renderOutputBufferV21(codec, bufferIndex, presentationTimeUs, System.nanoTime());
                } else {
                    this.renderOutputBuffer(codec, bufferIndex, presentationTimeUs);
                }

                return true;
            } else if (this.getState() != 2) {
                return false;
            } else {
                long elapsedSinceStartOfLoopUs = SystemClock.elapsedRealtime() * 1000L - elapsedRealtimeUs;
                earlyUs -= elapsedSinceStartOfLoopUs;
                long systemTimeNs = System.nanoTime();
                long unadjustedFrameReleaseTimeNs = systemTimeNs + earlyUs * 1000L;
                long adjustedReleaseTimeNs = this.frameReleaseTimeHelper.adjustReleaseTime(bufferPresentationTimeUs, unadjustedFrameReleaseTimeNs);
                earlyUs = (adjustedReleaseTimeNs - systemTimeNs) / 1000L;
                if (this.shouldDropOutputBuffer(earlyUs, elapsedRealtimeUs)) {
                    this.dropOutputBuffer(codec, bufferIndex, presentationTimeUs);
                    return true;
                } else {
                    if (Util.SDK_INT >= 21) {
                        if (earlyUs < 50000L) {
                            this.renderOutputBufferV21(codec, bufferIndex, presentationTimeUs, adjustedReleaseTimeNs);
                            return true;
                        }
                    } else if (earlyUs < 30000L) {
                        if (earlyUs > 11000L) {
                            try {
                                Thread.sleep((earlyUs - 10000L) / 1000L);
                            } catch (InterruptedException var25) {
                                Thread.currentThread().interrupt();
                            }
                        }

                        this.renderOutputBuffer(codec, bufferIndex, presentationTimeUs);
                        return true;
                    }

                    return false;
                }
            }
        }
    }

    protected boolean shouldDropOutputBuffer(long earlyUs, long elapsedRealtimeUs) {
        return isBufferLate(earlyUs);
    }

    protected void skipOutputBuffer(MediaCodec codec, int index, long presentationTimeUs) {
        TraceUtil.beginSection("skipVideoBuffer");
        codec.releaseOutputBuffer(index, false);
        TraceUtil.endSection();
        ++this.decoderCounters.skippedOutputBufferCount;
    }

    protected void dropOutputBuffer(MediaCodec codec, int index, long presentationTimeUs) {
        TraceUtil.beginSection("dropVideoBuffer");
        codec.releaseOutputBuffer(index, false);
        TraceUtil.endSection();
        ++this.decoderCounters.droppedOutputBufferCount;
        ++this.droppedFrames;
        ++this.consecutiveDroppedFrameCount;
        this.decoderCounters.maxConsecutiveDroppedOutputBufferCount = Math.max(this.consecutiveDroppedFrameCount, this.decoderCounters.maxConsecutiveDroppedOutputBufferCount);
        if (this.droppedFrames == this.maxDroppedFramesToNotify) {
            this.maybeNotifyDroppedFrames();
        }

    }

    protected void renderOutputBuffer(MediaCodec codec, int index, long presentationTimeUs) {
        this.maybeNotifyVideoSizeChanged();
        TraceUtil.beginSection("releaseOutputBuffer");
        codec.releaseOutputBuffer(index, true);
        TraceUtil.endSection();
        ++this.decoderCounters.renderedOutputBufferCount;
        this.consecutiveDroppedFrameCount = 0;
        this.maybeNotifyRenderedFirstFrame();
    }

    @TargetApi(21)
    protected void renderOutputBufferV21(MediaCodec codec, int index, long presentationTimeUs, long releaseTimeNs) {
        this.maybeNotifyVideoSizeChanged();
        TraceUtil.beginSection("releaseOutputBuffer");
        codec.releaseOutputBuffer(index, releaseTimeNs);
        TraceUtil.endSection();
        ++this.decoderCounters.renderedOutputBufferCount;
        this.consecutiveDroppedFrameCount = 0;
        this.maybeNotifyRenderedFirstFrame();
    }

    private boolean shouldUseDummySurface(boolean codecIsSecure) {
        return Util.SDK_INT >= 23 && !this.tunneling && (!codecIsSecure || DummySurface.isSecureSupported(this.context));
    }

    private void setJoiningDeadlineMs() {
        this.joiningDeadlineMs = this.allowedJoiningTimeMs > 0L ? SystemClock.elapsedRealtime() + this.allowedJoiningTimeMs : -9223372036854775807L;
    }

    private void clearRenderedFirstFrame() {
        this.renderedFirstFrame = false;
        if (Util.SDK_INT >= 23 && this.tunneling) {
            MediaCodec codec = this.getCodec();
            if (codec != null) {
                this.tunnelingOnFrameRenderedListener = new CammentMediaCodecVideoRenderer.OnFrameRenderedListenerV23(codec);
            }
        }

    }

    void maybeNotifyRenderedFirstFrame() {
        if (!this.renderedFirstFrame) {
            this.renderedFirstFrame = true;
            this.eventDispatcher.renderedFirstFrame(this.surface);
        }

    }

    private void maybeRenotifyRenderedFirstFrame() {
        if (this.renderedFirstFrame) {
            this.eventDispatcher.renderedFirstFrame(this.surface);
        }

    }

    private void clearReportedVideoSize() {
        this.reportedWidth = -1;
        this.reportedHeight = -1;
        this.reportedPixelWidthHeightRatio = -1.0F;
        this.reportedUnappliedRotationDegrees = -1;
    }

    private void maybeNotifyVideoSizeChanged() {
        if ((this.currentWidth != -1 || this.currentHeight != -1) && (this.reportedWidth != this.currentWidth || this.reportedHeight != this.currentHeight || this.reportedUnappliedRotationDegrees != this.currentUnappliedRotationDegrees || this.reportedPixelWidthHeightRatio != this.currentPixelWidthHeightRatio)) {
            this.eventDispatcher.videoSizeChanged(this.currentWidth, this.currentHeight, this.currentUnappliedRotationDegrees, this.currentPixelWidthHeightRatio);
            this.reportedWidth = this.currentWidth;
            this.reportedHeight = this.currentHeight;
            this.reportedUnappliedRotationDegrees = this.currentUnappliedRotationDegrees;
            this.reportedPixelWidthHeightRatio = this.currentPixelWidthHeightRatio;
        }

    }

    private void maybeRenotifyVideoSizeChanged() {
        if (this.reportedWidth != -1 || this.reportedHeight != -1) {
            this.eventDispatcher.videoSizeChanged(this.reportedWidth, this.reportedHeight, this.reportedUnappliedRotationDegrees, this.reportedPixelWidthHeightRatio);
        }

    }

    private void maybeNotifyDroppedFrames() {
        if (this.droppedFrames > 0) {
            long now = SystemClock.elapsedRealtime();
            long elapsedMs = now - this.droppedFrameAccumulationStartTimeMs;
            this.eventDispatcher.droppedFrames(this.droppedFrames, elapsedMs);
            this.droppedFrames = 0;
            this.droppedFrameAccumulationStartTimeMs = now;
        }

    }

    private static boolean isBufferLate(long earlyUs) {
        return earlyUs < -30000L;
    }

    @TargetApi(23)
    private static void setOutputSurfaceV23(MediaCodec codec, Surface surface) {
        codec.setOutputSurface(surface);
    }

    @TargetApi(21)
    private static void configureTunnelingV21(MediaFormat mediaFormat, int tunnelingAudioSessionId) {
        mediaFormat.setFeatureEnabled("tunneled-playback", true);
        mediaFormat.setInteger("audio-session-id", tunnelingAudioSessionId);
    }

    protected CammentMediaCodecVideoRenderer.CodecMaxValues getCodecMaxValues(MediaCodecInfo codecInfo, Format format, Format[] streamFormats) throws MediaCodecUtil.DecoderQueryException {
        int maxWidth = format.width;
        int maxHeight = format.height;
        int maxInputSize = getMaxInputSize(format);
        if (streamFormats.length == 1) {
            return new CammentMediaCodecVideoRenderer.CodecMaxValues(maxWidth, maxHeight, maxInputSize);
        } else {
            boolean haveUnknownDimensions = false;
            Format[] var8 = streamFormats;
            int var9 = streamFormats.length;

            for (int var10 = 0; var10 < var9; ++var10) {
                Format streamFormat = var8[var10];
                if (areAdaptationCompatible(codecInfo.adaptive, format, streamFormat)) {
                    haveUnknownDimensions |= streamFormat.width == -1 || streamFormat.height == -1;
                    maxWidth = Math.max(maxWidth, streamFormat.width);
                    maxHeight = Math.max(maxHeight, streamFormat.height);
                    maxInputSize = Math.max(maxInputSize, getMaxInputSize(streamFormat));
                }
            }

            if (haveUnknownDimensions) {
                Log.w("MediaCodecVideoRenderer", "Resolutions unknown. Codec max resolution: " + maxWidth + "x" + maxHeight);
                Point codecMaxSize = getCodecMaxSize(codecInfo, format);
                if (codecMaxSize != null) {
                    maxWidth = Math.max(maxWidth, codecMaxSize.x);
                    maxHeight = Math.max(maxHeight, codecMaxSize.y);
                    maxInputSize = Math.max(maxInputSize, getMaxInputSize(format.sampleMimeType, maxWidth, maxHeight));
                    Log.w("MediaCodecVideoRenderer", "Codec max resolution adjusted to: " + maxWidth + "x" + maxHeight);
                }
            }

            return new CammentMediaCodecVideoRenderer.CodecMaxValues(maxWidth, maxHeight, maxInputSize);
        }
    }

    @SuppressLint({"InlinedApi"})
    protected MediaFormat getMediaFormat(Format format, CammentMediaCodecVideoRenderer.CodecMaxValues codecMaxValues, boolean deviceNeedsAutoFrcWorkaround, int tunnelingAudioSessionId) {
        MediaFormat frameworkMediaFormat = format.getFrameworkMediaFormatV16();
        frameworkMediaFormat.setInteger("max-width", codecMaxValues.width);
        frameworkMediaFormat.setInteger("max-height", codecMaxValues.height);
        if (codecMaxValues.inputSize != -1) {
            frameworkMediaFormat.setInteger("max-input-size", codecMaxValues.inputSize);
        }

        if (deviceNeedsAutoFrcWorkaround) {
            frameworkMediaFormat.setInteger("auto-frc", 0);
        }

        if (tunnelingAudioSessionId != 0) {
            configureTunnelingV21(frameworkMediaFormat, tunnelingAudioSessionId);
        }

        return frameworkMediaFormat;
    }

    private static Point getCodecMaxSize(MediaCodecInfo codecInfo, Format format) throws MediaCodecUtil.DecoderQueryException {
        boolean isVerticalVideo = format.height > format.width;
        int formatLongEdgePx = isVerticalVideo ? format.height : format.width;
        int formatShortEdgePx = isVerticalVideo ? format.width : format.height;
        float aspectRatio = (float) formatShortEdgePx / (float) formatLongEdgePx;
        int[] var6 = STANDARD_LONG_EDGE_VIDEO_PX;
        int var7 = var6.length;

        for (int var8 = 0; var8 < var7; ++var8) {
            int longEdgePx = var6[var8];
            int shortEdgePx = (int) ((float) longEdgePx * aspectRatio);
            if (longEdgePx <= formatLongEdgePx || shortEdgePx <= formatShortEdgePx) {
                return null;
            }

            if (Util.SDK_INT >= 21) {
                Point alignedSize = codecInfo.alignVideoSizeV21(isVerticalVideo ? shortEdgePx : longEdgePx, isVerticalVideo ? longEdgePx : shortEdgePx);
                float frameRate = format.frameRate;
                if (codecInfo.isVideoSizeAndRateSupportedV21(alignedSize.x, alignedSize.y, (double) frameRate)) {
                    return alignedSize;
                }
            } else {
                longEdgePx = Util.ceilDivide(longEdgePx, 16) * 16;
                shortEdgePx = Util.ceilDivide(shortEdgePx, 16) * 16;
                if (longEdgePx * shortEdgePx <= MediaCodecUtil.maxH264DecodableFrameSize()) {
                    return new Point(isVerticalVideo ? shortEdgePx : longEdgePx, isVerticalVideo ? longEdgePx : shortEdgePx);
                }
            }
        }

        return null;
    }

    private static int getMaxInputSize(Format format) {
        if (format.maxInputSize == -1) {
            return getMaxInputSize(format.sampleMimeType, format.width, format.height);
        } else {
            int totalInitializationDataSize = 0;
            int initializationDataCount = format.initializationData.size();

            for (int i = 0; i < initializationDataCount; ++i) {
                totalInitializationDataSize += ((byte[]) format.initializationData.get(i)).length;
            }

            return format.maxInputSize + totalInitializationDataSize;
        }
    }

    private static int getMaxInputSize(String sampleMimeType, int width, int height) {
        if (width != -1 && height != -1) {
            byte var6 = -1;
            switch (sampleMimeType.hashCode()) {
                case -1664118616:
                    if (sampleMimeType.equals("video/3gpp")) {
                        var6 = 0;
                    }
                    break;
                case -1662541442:
                    if (sampleMimeType.equals("video/hevc")) {
                        var6 = 4;
                    }
                    break;
                case 1187890754:
                    if (sampleMimeType.equals("video/mp4v-es")) {
                        var6 = 1;
                    }
                    break;
                case 1331836730:
                    if (sampleMimeType.equals("video/avc")) {
                        var6 = 2;
                    }
                    break;
                case 1599127256:
                    if (sampleMimeType.equals("video/x-vnd.on2.vp8")) {
                        var6 = 3;
                    }
                    break;
                case 1599127257:
                    if (sampleMimeType.equals("video/x-vnd.on2.vp9")) {
                        var6 = 5;
                    }
            }

            int maxPixels;
            byte minCompressionRatio;
            switch (var6) {
                case 0:
                case 1:
                    maxPixels = width * height;
                    minCompressionRatio = 2;
                    break;
                case 2:
                    if ("BRAVIA 4K 2015".equals(Util.MODEL)) {
                        return -1;
                    }

                    maxPixels = Util.ceilDivide(width, 16) * Util.ceilDivide(height, 16) * 16 * 16;
                    minCompressionRatio = 2;
                    break;
                case 3:
                    maxPixels = width * height;
                    minCompressionRatio = 2;
                    break;
                case 4:
                case 5:
                    maxPixels = width * height;
                    minCompressionRatio = 4;
                    break;
                default:
                    return -1;
            }

            return maxPixels * 3 / (2 * minCompressionRatio);
        } else {
            return -1;
        }
    }

    private static void setVideoScalingMode(MediaCodec codec, int scalingMode) {
        codec.setVideoScalingMode(scalingMode);
    }

    private static boolean deviceNeedsAutoFrcWorkaround() {
        return Util.SDK_INT <= 22 && "foster".equals(Util.DEVICE) && "NVIDIA".equals(Util.MANUFACTURER);
    }

    private static boolean codecNeedsSetOutputSurfaceWorkaround(String name) {
        return ("deb".equals(Util.DEVICE) || "flo".equals(Util.DEVICE)) && "OMX.qcom.video.decoder.avc".equals(name) || "tcl_eu".equals(Util.DEVICE) && "OMX.MTK.VIDEO.DECODER.AVC".equals(name);
    }

    private static boolean areAdaptationCompatible(boolean codecIsAdaptive, Format first, Format second) {
        return first.sampleMimeType.equals(second.sampleMimeType) && getRotationDegrees(first) == getRotationDegrees(second) && (codecIsAdaptive || first.width == second.width && first.height == second.height);
    }

    private static float getPixelWidthHeightRatio(Format format) {
        return format.pixelWidthHeightRatio == -1.0F ? 1.0F : format.pixelWidthHeightRatio;
    }

    private static int getRotationDegrees(Format format) {
        return format.rotationDegrees == -1 ? 0 : format.rotationDegrees;
    }

    @TargetApi(23)
    private final class OnFrameRenderedListenerV23 implements MediaCodec.OnFrameRenderedListener {
        private OnFrameRenderedListenerV23(MediaCodec codec) {
            codec.setOnFrameRenderedListener(this, new Handler());
        }

        public void onFrameRendered(@NonNull MediaCodec codec, long presentationTimeUs, long nanoTime) {
            if (this == CammentMediaCodecVideoRenderer.this.tunnelingOnFrameRenderedListener) {
                CammentMediaCodecVideoRenderer.this.maybeNotifyRenderedFirstFrame();
            }
        }
    }

    protected static final class CodecMaxValues {
        public final int width;
        public final int height;
        public final int inputSize;

        public CodecMaxValues(int width, int height, int inputSize) {
            this.width = width;
            this.height = height;
            this.inputSize = inputSize;
        }

    }
}
