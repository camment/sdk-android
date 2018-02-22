package tv.camment.cammentsdk.camera;


import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.exoplayer2.BaseRenderer;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.FormatHolder;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.NalUnitUtil;
import com.google.android.exoplayer2.util.TraceUtil;
import com.google.android.exoplayer2.util.Util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class CammentMediaCodecRenderer extends BaseRenderer {

    private static final String TAG = "MediaCodecRenderer";
    private static final long MAX_CODEC_HOTSWAP_TIME_MS = 1000L;
    private static final int RECONFIGURATION_STATE_NONE = 0;
    private static final int RECONFIGURATION_STATE_WRITE_PENDING = 1;
    private static final int RECONFIGURATION_STATE_QUEUE_PENDING = 2;
    private static final int REINITIALIZATION_STATE_NONE = 0;
    private static final int REINITIALIZATION_STATE_SIGNAL_END_OF_STREAM = 1;
    private static final int REINITIALIZATION_STATE_WAIT_END_OF_STREAM = 2;
    private static final int ADAPTATION_WORKAROUND_MODE_NEVER = 0;
    private static final int ADAPTATION_WORKAROUND_MODE_SAME_RESOLUTION = 1;
    private static final int ADAPTATION_WORKAROUND_MODE_ALWAYS = 2;
    private static final byte[] ADAPTATION_WORKAROUND_BUFFER = Util.getBytesFromHexString("0000016742C00BDA259000000168CE0F13200000016588840DCE7118A0002FBF1C31C3275D78");
    private static final int ADAPTATION_WORKAROUND_SLICE_WIDTH_HEIGHT = 32;
    private final MediaCodecSelector mediaCodecSelector;
    private final DrmSessionManager<FrameworkMediaCrypto> drmSessionManager;
    private final boolean playClearSamplesWithoutKeys;
    private final DecoderInputBuffer buffer;
    private final DecoderInputBuffer flagsOnlyBuffer;
    private final FormatHolder formatHolder;
    private final List<Long> decodeOnlyPresentationTimestamps;
    private final MediaCodec.BufferInfo outputBufferInfo;
    private Format format;
    private DrmSession<FrameworkMediaCrypto> drmSession;
    private DrmSession<FrameworkMediaCrypto> pendingDrmSession;
    private MediaCodec codec;
    private MediaCodecInfo codecInfo;
    private int lastCodecInfoIndex = -1;
    private List<MediaCodecInfo> codecInfos;
    private int codecAdaptationWorkaroundMode;
    private boolean codecNeedsDiscardToSpsWorkaround;
    private boolean codecNeedsFlushWorkaround;
    private boolean codecNeedsEosPropagationWorkaround;
    private boolean codecNeedsEosFlushWorkaround;
    private boolean codecNeedsEosOutputExceptionWorkaround;
    private boolean codecNeedsMonoChannelCountWorkaround;
    private boolean codecNeedsAdaptationWorkaroundBuffer;
    private boolean shouldSkipAdaptationWorkaroundOutputBuffer;
    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private long codecHotswapDeadlineMs;
    private int inputIndex;
    private int outputIndex;
    private boolean shouldSkipOutputBuffer;
    private boolean codecReconfigured;
    private int codecReconfigurationState;
    private int codecReinitializationState;
    private boolean codecReceivedBuffers;
    private boolean codecReceivedEos;
    private boolean inputStreamEnded;
    private boolean outputStreamEnded;
    private boolean waitingForKeys;
    private boolean waitingForFirstSyncFrame;
    protected DecoderCounters decoderCounters;

    public CammentMediaCodecRenderer(int trackType, MediaCodecSelector mediaCodecSelector, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys) {
        super(trackType);
        this.mediaCodecSelector = (MediaCodecSelector) Assertions.checkNotNull(mediaCodecSelector);
        this.drmSessionManager = drmSessionManager;
        this.playClearSamplesWithoutKeys = playClearSamplesWithoutKeys;
        this.buffer = new DecoderInputBuffer(0);
        this.flagsOnlyBuffer = DecoderInputBuffer.newFlagsOnlyInstance();
        this.formatHolder = new FormatHolder();
        this.decodeOnlyPresentationTimestamps = new ArrayList();
        this.outputBufferInfo = new MediaCodec.BufferInfo();
        this.codecReconfigurationState = 0;
        this.codecReinitializationState = 0;
    }

    @Override
    public final int supportsMixedMimeTypeAdaptation() {
        return 8;
    }

    @Override
    public final int supportsFormat(Format format) throws ExoPlaybackException {
        try {
            int formatSupport = this.supportsFormat(this.mediaCodecSelector, format);
            if ((formatSupport & 7) > 2 && !isDrmSchemeSupported(this.drmSessionManager, format.drmInitData)) {
                formatSupport = formatSupport & -8 | 2;
            }

            return formatSupport;
        } catch (MediaCodecUtil.DecoderQueryException var3) {
            throw ExoPlaybackException.createForRenderer(var3, this.getIndex());
        }
    }

    protected abstract int supportsFormat(MediaCodecSelector var1, Format var2) throws MediaCodecUtil.DecoderQueryException;

    protected MediaCodecInfo getDecoderInfo(MediaCodecSelector mediaCodecSelector, Format format, boolean requiresSecureDecoder) throws MediaCodecUtil.DecoderQueryException {
        return mediaCodecSelector.getDecoderInfo(format.sampleMimeType, requiresSecureDecoder);
    }

    protected abstract void configureCodec(MediaCodecInfo var1, MediaCodec var2, Format var3, MediaCrypto var4) throws MediaCodecUtil.DecoderQueryException;

    protected final void maybeInitCodec() throws ExoPlaybackException {
        if (this.codec == null && this.format != null) {
            this.drmSession = this.pendingDrmSession;
            String mimeType = this.format.sampleMimeType;
            MediaCrypto wrappedMediaCrypto = null;
            boolean drmSessionRequiresSecureDecoder = false;
            if (this.drmSession != null) {
                FrameworkMediaCrypto mediaCrypto = (FrameworkMediaCrypto) this.drmSession.getMediaCrypto();
                if (mediaCrypto == null) {
                    DrmSession.DrmSessionException drmError = this.drmSession.getError();
                    if (drmError != null) {
                        throw ExoPlaybackException.createForRenderer(drmError, this.getIndex());
                    }

                    return;
                }

                wrappedMediaCrypto = mediaCrypto.getWrappedMediaCrypto();
                drmSessionRequiresSecureDecoder = mediaCrypto.requiresSecureDecoderComponent(mimeType);
            }

            if (this.codecInfo == null) {
                try {
                    this.codecInfo = this.getMediaCodecInfo(this.format.sampleMimeType, drmSessionRequiresSecureDecoder);
                    if (this.codecInfo == null && drmSessionRequiresSecureDecoder) {
                        this.codecInfo = this.getMediaCodecInfo(this.format.sampleMimeType, false);
                        if (this.codecInfo != null) {
                            Log.w("MediaCodecRenderer", "Drm session requires secure decoder for " + mimeType + ", but " + "no secure decoder available. Trying to proceed with " + this.codecInfo.name + ".");
                        }
                    }
                } catch (MediaCodecUtil.DecoderQueryException var10) {
                    this.throwDecoderInitError(new MediaCodecRenderer.DecoderInitializationException(this.format, var10, drmSessionRequiresSecureDecoder, -49998));
                }

                if (this.codecInfo == null) {
                    this.throwDecoderInitError(new MediaCodecRenderer.DecoderInitializationException(this.format, (Throwable) null, drmSessionRequiresSecureDecoder, -49999));
                }
            }

            if (this.shouldInitCodec(this.codecInfo)) {
                String codecName = this.codecInfo.name;
                this.codecAdaptationWorkaroundMode = this.codecAdaptationWorkaroundMode(codecName);
                this.codecNeedsDiscardToSpsWorkaround = codecNeedsDiscardToSpsWorkaround(codecName, this.format);
                this.codecNeedsFlushWorkaround = codecNeedsFlushWorkaround(codecName);
                this.codecNeedsEosPropagationWorkaround = codecNeedsEosPropagationWorkaround(codecName);
                this.codecNeedsEosFlushWorkaround = codecNeedsEosFlushWorkaround(codecName);
                this.codecNeedsEosOutputExceptionWorkaround = codecNeedsEosOutputExceptionWorkaround(codecName);
                this.codecNeedsMonoChannelCountWorkaround = codecNeedsMonoChannelCountWorkaround(codecName, this.format);

                try {
                    long codecInitializingTimestamp = SystemClock.elapsedRealtime();
                    TraceUtil.beginSection("createCodec:" + codecName);
                    this.codec = MediaCodec.createByCodecName(codecName);
                    TraceUtil.endSection();
                    TraceUtil.beginSection("configureCodec");
                    this.configureCodec(this.codecInfo, this.codec, this.format, wrappedMediaCrypto);
                    TraceUtil.endSection();
                    TraceUtil.beginSection("startCodec");
                    this.codec.start();
                    TraceUtil.endSection();
                    long codecInitializedTimestamp = SystemClock.elapsedRealtime();
                    this.onCodecInitialized(codecName, codecInitializedTimestamp, codecInitializedTimestamp - codecInitializingTimestamp);
                    this.inputBuffers = this.codec.getInputBuffers();
                    this.outputBuffers = this.codec.getOutputBuffers();
                } catch (Exception var9) {
                    if (this.codecInfos != null && this.codecInfos.size() > lastCodecInfoIndex + 1) {
                        this.codecInfo = null;
                        if (this.codec != null) {
                            this.codec.release();
                        }
                        this.codec = null;
                        return;
                    } else {
                        this.throwDecoderInitError(new MediaCodecRenderer.DecoderInitializationException(this.format, var9, drmSessionRequiresSecureDecoder, codecName));
                    }
                }

                this.lastCodecInfoIndex = -1;
                this.codecHotswapDeadlineMs = this.getState() == 2 ? SystemClock.elapsedRealtime() + 1000L : -9223372036854775807L;
                this.inputIndex = -1;
                this.outputIndex = -1;
                this.waitingForFirstSyncFrame = true;
                ++this.decoderCounters.decoderInitCount;
            }
        }
    }

    private MediaCodecInfo getMediaCodecInfo(String mimeType, boolean drmSessionRequiresSecureDecoder) throws MediaCodecUtil.DecoderQueryException {
        this.codecInfos = MediaCodecUtil.getDecoderInfos(mimeType, drmSessionRequiresSecureDecoder);
        if (this.codecInfos != null && this.codecInfos.size() > lastCodecInfoIndex + 1) {
            this.codecInfo = this.codecInfos.get(this.lastCodecInfoIndex + 1);
        } else {
            this.codecInfo = null;
        }

        if (this.codecInfo != null) {
            this.lastCodecInfoIndex++;
        }

        Log.d("CMediaCodec", "getMediaCodecInfo " + (codec == null ? "null" : codec.getName()));

        return this.codecInfo;
    }

    private void throwDecoderInitError(MediaCodecRenderer.DecoderInitializationException e) throws ExoPlaybackException {
        throw ExoPlaybackException.createForRenderer(e, this.getIndex());
    }

    protected boolean shouldInitCodec(MediaCodecInfo codecInfo) {
        return true;
    }

    protected final MediaCodec getCodec() {
        return this.codec;
    }

    protected final MediaCodecInfo getCodecInfo() {
        return this.codecInfo;
    }

    protected void onEnabled(boolean joining) throws ExoPlaybackException {
        this.decoderCounters = new DecoderCounters();
    }

    protected void onPositionReset(long positionUs, boolean joining) throws ExoPlaybackException {
        this.inputStreamEnded = false;
        this.outputStreamEnded = false;
        if (this.codec != null) {
            this.flushCodec();
        }

    }

    protected void onDisabled() {
        this.format = null;

        try {
            this.releaseCodec();
        } finally {
            try {
                if (this.drmSession != null) {
                    this.drmSessionManager.releaseSession(this.drmSession);
                }
            } finally {
                try {
                    if (this.pendingDrmSession != null && this.pendingDrmSession != this.drmSession) {
                        this.drmSessionManager.releaseSession(this.pendingDrmSession);
                    }
                } finally {
                    this.drmSession = null;
                    this.pendingDrmSession = null;
                }

            }

        }

    }

    protected void releaseCodec() {
        this.codecHotswapDeadlineMs = -9223372036854775807L;
        this.inputIndex = -1;
        this.outputIndex = -1;
        this.waitingForKeys = false;
        this.shouldSkipOutputBuffer = false;
        this.decodeOnlyPresentationTimestamps.clear();
        this.inputBuffers = null;
        this.outputBuffers = null;
        this.codecInfo = null;
        this.codecReconfigured = false;
        this.codecReceivedBuffers = false;
        this.codecNeedsDiscardToSpsWorkaround = false;
        this.codecNeedsFlushWorkaround = false;
        this.codecAdaptationWorkaroundMode = 0;
        this.codecNeedsEosPropagationWorkaround = false;
        this.codecNeedsEosFlushWorkaround = false;
        this.codecNeedsMonoChannelCountWorkaround = false;
        this.codecNeedsAdaptationWorkaroundBuffer = false;
        this.shouldSkipAdaptationWorkaroundOutputBuffer = false;
        this.codecReceivedEos = false;
        this.codecReconfigurationState = 0;
        this.codecReinitializationState = 0;
        this.buffer.data = null;
        if (this.codec != null) {
            ++this.decoderCounters.decoderReleaseCount;

            try {
                this.codec.stop();
            } finally {
                try {
                    this.codec.release();
                } finally {
                    this.codec = null;
                    if (this.drmSession != null && this.pendingDrmSession != this.drmSession) {
                        try {
                            this.drmSessionManager.releaseSession(this.drmSession);
                        } finally {
                            this.drmSession = null;
                        }
                    }

                }
            }
        }

    }

    protected void onStarted() {
    }

    protected void onStopped() {
    }

    public void render(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
        if (this.outputStreamEnded) {
            this.renderToEndOfStream();
        } else {
            int result;
            if (this.format == null) {
                this.flagsOnlyBuffer.clear();
                result = this.readSource(this.formatHolder, this.flagsOnlyBuffer, true);
                if (result != -5) {
                    if (result == -4) {
                        Assertions.checkState(this.flagsOnlyBuffer.isEndOfStream());
                        this.inputStreamEnded = true;
                        this.processEndOfStream();
                        return;
                    }

                    return;
                }

                this.onInputFormatChanged(this.formatHolder.format);
            }

            this.maybeInitCodec();
            if (this.codec != null) {
                TraceUtil.beginSection("drainAndFeed");

                while (true) {
                    if (!this.drainOutputBuffer(positionUs, elapsedRealtimeUs)) {
                        while (this.feedInputBuffer()) {
                            ;
                        }

                        TraceUtil.endSection();
                        break;
                    }
                }
            } else {
                this.skipSource(positionUs);
                this.flagsOnlyBuffer.clear();
                result = this.readSource(this.formatHolder, this.flagsOnlyBuffer, false);
                if (result == -5) {
                    this.onInputFormatChanged(this.formatHolder.format);
                } else if (result == -4) {
                    Assertions.checkState(this.flagsOnlyBuffer.isEndOfStream());
                    this.inputStreamEnded = true;
                    this.processEndOfStream();
                }
            }

            this.decoderCounters.ensureUpdated();
        }
    }

    protected void flushCodec() throws ExoPlaybackException {
        this.codecHotswapDeadlineMs = -9223372036854775807L;
        this.inputIndex = -1;
        this.outputIndex = -1;
        this.waitingForFirstSyncFrame = true;
        this.waitingForKeys = false;
        this.shouldSkipOutputBuffer = false;
        this.decodeOnlyPresentationTimestamps.clear();
        this.codecNeedsAdaptationWorkaroundBuffer = false;
        this.shouldSkipAdaptationWorkaroundOutputBuffer = false;
        if (this.codecNeedsFlushWorkaround || this.codecNeedsEosFlushWorkaround && this.codecReceivedEos) {
            this.releaseCodec();
            this.maybeInitCodec();
        } else if (this.codecReinitializationState != 0) {
            this.releaseCodec();
            this.maybeInitCodec();
        } else {
            this.codec.flush();
            this.codecReceivedBuffers = false;
        }

        if (this.codecReconfigured && this.format != null) {
            this.codecReconfigurationState = 1;
        }

    }

    private boolean feedInputBuffer() throws ExoPlaybackException {
        if (this.codec != null && this.codecReinitializationState != 2 && !this.inputStreamEnded) {
            if (this.inputIndex < 0) {
                this.inputIndex = this.codec.dequeueInputBuffer(0L);
                if (this.inputIndex < 0) {
                    return false;
                }

                this.buffer.data = this.inputBuffers[this.inputIndex];
                this.buffer.clear();
            }

            if (this.codecReinitializationState == 1) {
                if (!this.codecNeedsEosPropagationWorkaround) {
                    this.codecReceivedEos = true;
                    this.codec.queueInputBuffer(this.inputIndex, 0, 0, 0L, 4);
                    this.inputIndex = -1;
                }

                this.codecReinitializationState = 2;
                return false;
            } else if (this.codecNeedsAdaptationWorkaroundBuffer) {
                this.codecNeedsAdaptationWorkaroundBuffer = false;
                this.buffer.data.put(ADAPTATION_WORKAROUND_BUFFER);
                this.codec.queueInputBuffer(this.inputIndex, 0, ADAPTATION_WORKAROUND_BUFFER.length, 0L, 0);
                this.inputIndex = -1;
                this.codecReceivedBuffers = true;
                return true;
            } else {
                int adaptiveReconfigurationBytes = 0;
                int result;
                if (this.waitingForKeys) {
                    result = -4;
                } else {
                    if (this.codecReconfigurationState == 1) {
                        for (int i = 0; i < this.format.initializationData.size(); ++i) {
                            byte[] data = (byte[]) this.format.initializationData.get(i);
                            this.buffer.data.put(data);
                        }

                        this.codecReconfigurationState = 2;
                    }

                    adaptiveReconfigurationBytes = this.buffer.data.position();
                    result = this.readSource(this.formatHolder, this.buffer, false);
                }

                if (result == -3) {
                    return false;
                } else if (result == -5) {
                    if (this.codecReconfigurationState == 2) {
                        this.buffer.clear();
                        this.codecReconfigurationState = 1;
                    }

                    this.onInputFormatChanged(this.formatHolder.format);
                    return true;
                } else if (this.buffer.isEndOfStream()) {
                    if (this.codecReconfigurationState == 2) {
                        this.buffer.clear();
                        this.codecReconfigurationState = 1;
                    }

                    this.inputStreamEnded = true;
                    if (!this.codecReceivedBuffers) {
                        this.processEndOfStream();
                        return false;
                    } else {
                        try {
                            if (!this.codecNeedsEosPropagationWorkaround) {
                                this.codecReceivedEos = true;
                                this.codec.queueInputBuffer(this.inputIndex, 0, 0, 0L, 4);
                                this.inputIndex = -1;
                            }

                            return false;
                        } catch (MediaCodec.CryptoException var7) {
                            throw ExoPlaybackException.createForRenderer(var7, this.getIndex());
                        }
                    }
                } else if (this.waitingForFirstSyncFrame && !this.buffer.isKeyFrame()) {
                    this.buffer.clear();
                    if (this.codecReconfigurationState == 2) {
                        this.codecReconfigurationState = 1;
                    }

                    return true;
                } else {
                    this.waitingForFirstSyncFrame = false;
                    boolean bufferEncrypted = this.buffer.isEncrypted();
                    this.waitingForKeys = this.shouldWaitForKeys(bufferEncrypted);
                    if (this.waitingForKeys) {
                        return false;
                    } else {
                        if (this.codecNeedsDiscardToSpsWorkaround && !bufferEncrypted) {
                            NalUnitUtil.discardToSps(this.buffer.data);
                            if (this.buffer.data.position() == 0) {
                                return true;
                            }

                            this.codecNeedsDiscardToSpsWorkaround = false;
                        }

                        try {
                            long presentationTimeUs = this.buffer.timeUs;
                            if (this.buffer.isDecodeOnly()) {
                                this.decodeOnlyPresentationTimestamps.add(Long.valueOf(presentationTimeUs));
                            }

                            this.buffer.flip();
                            this.onQueueInputBuffer(this.buffer);
                            if (bufferEncrypted) {
                                MediaCodec.CryptoInfo cryptoInfo = getFrameworkCryptoInfo(this.buffer, adaptiveReconfigurationBytes);
                                this.codec.queueSecureInputBuffer(this.inputIndex, 0, cryptoInfo, presentationTimeUs, 0);
                            } else {
                                this.codec.queueInputBuffer(this.inputIndex, 0, this.buffer.data.limit(), presentationTimeUs, 0);
                            }

                            this.inputIndex = -1;
                            this.codecReceivedBuffers = true;
                            this.codecReconfigurationState = 0;
                            ++this.decoderCounters.inputBufferCount;
                            return true;
                        } catch (MediaCodec.CryptoException var8) {
                            throw ExoPlaybackException.createForRenderer(var8, this.getIndex());
                        }
                    }
                }
            }
        } else {
            return false;
        }
    }

    private static MediaCodec.CryptoInfo getFrameworkCryptoInfo(DecoderInputBuffer buffer, int adaptiveReconfigurationBytes) {
        MediaCodec.CryptoInfo cryptoInfo = buffer.cryptoInfo.getFrameworkCryptoInfoV16();
        if (adaptiveReconfigurationBytes == 0) {
            return cryptoInfo;
        } else {
            if (cryptoInfo.numBytesOfClearData == null) {
                cryptoInfo.numBytesOfClearData = new int[1];
            }

            cryptoInfo.numBytesOfClearData[0] += adaptiveReconfigurationBytes;
            return cryptoInfo;
        }
    }

    private boolean shouldWaitForKeys(boolean bufferEncrypted) throws ExoPlaybackException {
        if (this.drmSession == null || !bufferEncrypted && this.playClearSamplesWithoutKeys) {
            return false;
        } else {
            int drmSessionState = this.drmSession.getState();
            if (drmSessionState == 1) {
                throw ExoPlaybackException.createForRenderer(this.drmSession.getError(), this.getIndex());
            } else {
                return drmSessionState != 4;
            }
        }
    }

    protected void onCodecInitialized(String name, long initializedTimestampMs, long initializationDurationMs) {
    }

    protected void onInputFormatChanged(Format newFormat) throws ExoPlaybackException {
        Format oldFormat = this.format;
        this.format = newFormat;
        boolean drmInitDataChanged = !Util.areEqual(this.format.drmInitData, oldFormat == null ? null : oldFormat.drmInitData);
        if (drmInitDataChanged) {
            if (this.format.drmInitData != null) {
                if (this.drmSessionManager == null) {
                    throw ExoPlaybackException.createForRenderer(new IllegalStateException("Media requires a DrmSessionManager"), this.getIndex());
                }

                this.pendingDrmSession = this.drmSessionManager.acquireSession(Looper.myLooper(), this.format.drmInitData);
                if (this.pendingDrmSession == this.drmSession) {
                    this.drmSessionManager.releaseSession(this.pendingDrmSession);
                }
            } else {
                this.pendingDrmSession = null;
            }
        }

        if (this.pendingDrmSession == this.drmSession && this.codec != null && this.canReconfigureCodec(this.codec, this.codecInfo.adaptive, oldFormat, this.format)) {
            this.codecReconfigured = true;
            this.codecReconfigurationState = 1;
            this.codecNeedsAdaptationWorkaroundBuffer = this.codecAdaptationWorkaroundMode == 2 || this.codecAdaptationWorkaroundMode == 1 && this.format.width == oldFormat.width && this.format.height == oldFormat.height;
        } else if (this.codecReceivedBuffers) {
            this.codecReinitializationState = 1;
        } else {
            this.releaseCodec();
            this.maybeInitCodec();
        }

    }

    protected void onOutputFormatChanged(MediaCodec codec, MediaFormat outputFormat) throws ExoPlaybackException {
    }

    protected void onQueueInputBuffer(DecoderInputBuffer buffer) {
    }

    protected void onProcessedOutputBuffer(long presentationTimeUs) {
    }

    protected boolean canReconfigureCodec(MediaCodec codec, boolean codecIsAdaptive, Format oldFormat, Format newFormat) {
        return false;
    }

    public boolean isEnded() {
        return this.outputStreamEnded;
    }

    public boolean isReady() {
        return this.format != null && !this.waitingForKeys && (this.isSourceReady() || this.outputIndex >= 0 || this.codecHotswapDeadlineMs != -9223372036854775807L && SystemClock.elapsedRealtime() < this.codecHotswapDeadlineMs);
    }

    protected long getDequeueOutputBufferTimeoutUs() {
        return 0L;
    }

    private boolean drainOutputBuffer(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
        if (this.outputIndex < 0) {
            if (this.codecNeedsEosOutputExceptionWorkaround && this.codecReceivedEos) {
                try {
                    this.outputIndex = this.codec.dequeueOutputBuffer(this.outputBufferInfo, this.getDequeueOutputBufferTimeoutUs());
                } catch (IllegalStateException var7) {
                    this.processEndOfStream();
                    if (this.outputStreamEnded) {
                        this.releaseCodec();
                    }

                    return false;
                }
            } else {
                this.outputIndex = this.codec.dequeueOutputBuffer(this.outputBufferInfo, this.getDequeueOutputBufferTimeoutUs());
            }

            if (this.outputIndex < 0) {
                if (this.outputIndex == -2) {
                    this.processOutputFormat();
                    return true;
                }

                if (this.outputIndex == -3) {
                    this.processOutputBuffersChanged();
                    return true;
                }

                if (this.codecNeedsEosPropagationWorkaround && (this.inputStreamEnded || this.codecReinitializationState == 2)) {
                    this.processEndOfStream();
                }

                return false;
            }

            if (this.shouldSkipAdaptationWorkaroundOutputBuffer) {
                this.shouldSkipAdaptationWorkaroundOutputBuffer = false;
                this.codec.releaseOutputBuffer(this.outputIndex, false);
                this.outputIndex = -1;
                return true;
            }

            if ((this.outputBufferInfo.flags & 4) != 0) {
                this.processEndOfStream();
                this.outputIndex = -1;
                return false;
            }

            ByteBuffer outputBuffer = this.outputBuffers[this.outputIndex];
            if (outputBuffer != null) {
                outputBuffer.position(this.outputBufferInfo.offset);
                outputBuffer.limit(this.outputBufferInfo.offset + this.outputBufferInfo.size);
            }

            this.shouldSkipOutputBuffer = this.shouldSkipOutputBuffer(this.outputBufferInfo.presentationTimeUs);
        }

        boolean processedOutputBuffer;
        if (this.codecNeedsEosOutputExceptionWorkaround && this.codecReceivedEos) {
            try {
                processedOutputBuffer = this.processOutputBuffer(positionUs, elapsedRealtimeUs, this.codec, this.outputBuffers[this.outputIndex], this.outputIndex, this.outputBufferInfo.flags, this.outputBufferInfo.presentationTimeUs, this.shouldSkipOutputBuffer);
            } catch (IllegalStateException var8) {
                this.processEndOfStream();
                if (this.outputStreamEnded) {
                    this.releaseCodec();
                }

                return false;
            }
        } else {
            processedOutputBuffer = this.processOutputBuffer(positionUs, elapsedRealtimeUs, this.codec, this.outputBuffers[this.outputIndex], this.outputIndex, this.outputBufferInfo.flags, this.outputBufferInfo.presentationTimeUs, this.shouldSkipOutputBuffer);
        }

        if (processedOutputBuffer) {
            this.onProcessedOutputBuffer(this.outputBufferInfo.presentationTimeUs);
            this.outputIndex = -1;
            return true;
        } else {
            return false;
        }
    }

    private void processOutputFormat() throws ExoPlaybackException {
        MediaFormat format = this.codec.getOutputFormat();
        if (this.codecAdaptationWorkaroundMode != 0 && format.getInteger("width") == 32 && format.getInteger("height") == 32) {
            this.shouldSkipAdaptationWorkaroundOutputBuffer = true;
        } else {
            if (this.codecNeedsMonoChannelCountWorkaround) {
                format.setInteger("channel-count", 1);
            }

            this.onOutputFormatChanged(this.codec, format);
        }
    }

    private void processOutputBuffersChanged() {
        this.outputBuffers = this.codec.getOutputBuffers();
    }

    protected abstract boolean processOutputBuffer(long var1, long var3, MediaCodec var5, ByteBuffer var6, int var7, int var8, long var9, boolean var11) throws ExoPlaybackException;

    protected void renderToEndOfStream() throws ExoPlaybackException {
    }

    private void processEndOfStream() throws ExoPlaybackException {
        if (this.codecReinitializationState == 2) {
            this.releaseCodec();
            this.maybeInitCodec();
        } else {
            this.outputStreamEnded = true;
            this.renderToEndOfStream();
        }

    }

    private boolean shouldSkipOutputBuffer(long presentationTimeUs) {
        int size = this.decodeOnlyPresentationTimestamps.size();

        for (int i = 0; i < size; ++i) {
            if (((Long) this.decodeOnlyPresentationTimestamps.get(i)).longValue() == presentationTimeUs) {
                this.decodeOnlyPresentationTimestamps.remove(i);
                return true;
            }
        }

        return false;
    }

    private static boolean isDrmSchemeSupported(DrmSessionManager drmSessionManager, @Nullable DrmInitData drmInitData) {
        return drmInitData == null ? true : (drmSessionManager == null ? false : drmSessionManager.canAcquireSession(drmInitData));
    }

    private static boolean codecNeedsFlushWorkaround(String name) {
        return Util.SDK_INT < 18 || Util.SDK_INT == 18 && ("OMX.SEC.avc.dec".equals(name) || "OMX.SEC.avc.dec.secure".equals(name)) || Util.SDK_INT == 19 && Util.MODEL.startsWith("SM-G800") && ("OMX.Exynos.avc.dec".equals(name) || "OMX.Exynos.avc.dec.secure".equals(name));
    }

    private int codecAdaptationWorkaroundMode(String name) {
        return Util.SDK_INT <= 24 && "OMX.Exynos.avc.dec.secure".equals(name) && (Util.MODEL.startsWith("SM-T585") || Util.MODEL.startsWith("SM-A520")) ? 2 : (Util.SDK_INT >= 24 || !"OMX.Nvidia.h264.decode".equals(name) && !"OMX.Nvidia.h264.decode.secure".equals(name) || !"flounder".equals(Util.DEVICE) && !"flounder_lte".equals(Util.DEVICE) && !"grouper".equals(Util.DEVICE) && !"tilapia".equals(Util.DEVICE) ? 0 : 1);
    }

    private static boolean codecNeedsDiscardToSpsWorkaround(String name, Format format) {
        return Util.SDK_INT < 21 && format.initializationData.isEmpty() && "OMX.MTK.VIDEO.DECODER.AVC".equals(name);
    }

    private static boolean codecNeedsEosPropagationWorkaround(String name) {
        return Util.SDK_INT <= 17 && ("OMX.rk.video_decoder.avc".equals(name) || "OMX.allwinner.video.decoder.avc".equals(name));
    }

    private static boolean codecNeedsEosFlushWorkaround(String name) {
        return Util.SDK_INT <= 23 && "OMX.google.vorbis.decoder".equals(name) || Util.SDK_INT <= 19 && "hb2000".equals(Util.DEVICE) && ("OMX.amlogic.avc.decoder.awesome".equals(name) || "OMX.amlogic.avc.decoder.awesome.secure".equals(name));
    }

    private static boolean codecNeedsEosOutputExceptionWorkaround(String name) {
        return Util.SDK_INT == 21 && "OMX.google.aac.decoder".equals(name);
    }

    private static boolean codecNeedsMonoChannelCountWorkaround(String name, Format format) {
        return Util.SDK_INT <= 18 && format.channelCount == 1 && "OMX.MTK.AUDIO.DECODER.MP3".equals(name);
    }

}
