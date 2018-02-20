package tv.camment.cammentsdk.camera;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.opengl.EGLContext;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import tv.camment.cammentsdk.events.MediaCodecFailureEvent;

final class MediaVideoEncoder extends MediaEncoder {
    private static final String TAG = "MediaVideoEncoder";

    private static final String MIME_TYPE = "video/avc";

    private static final int DEFAULT_FRAME_RATE = 25;
    private static final int DEFAULT_IFRAME_INTERVAL = 10;
    private static final float BPP = 0.25f;

    private final int mWidth;
    private final int mHeight;
    private RenderHandler mRenderHandler;
    private Surface mSurface;

    MediaVideoEncoder(final MediaMuxerWrapper muxer, final MediaEncoderListener listener, final int width, final int height) {
        super(muxer, listener);
        mWidth = width;
        mHeight = height;
        mRenderHandler = RenderHandler.createHandler(TAG);
    }

    boolean frameAvailableSoon(final float[] tex_matrix, final float[] mvp_matrix) {
        boolean result;
        if (result = super.frameAvailableSoon())
            mRenderHandler.draw(tex_matrix, mvp_matrix);
        return result;
    }

    @Override
    public boolean frameAvailableSoon() {
        boolean result;
        if (result = super.frameAvailableSoon())
            mRenderHandler.draw(null);
        return result;
    }

    @Override
    protected void prepare() throws IOException {
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;

        final MediaCodecInfo videoCodecInfo = selectVideoCodec(MIME_TYPE);
        if (videoCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }

        final MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);    // API >= 18
        format.setInteger(MediaFormat.KEY_BIT_RATE, calcBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, DEFAULT_IFRAME_INTERVAL);

        try {
            mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            Log.d("MediaCodec", "Failed to create and configure encoder by type, will try to select by name.", e);
            mMediaCodec = createAndConfigureMediaCodecByName(format);
        }
        // get Surface for encoder input
        // this method only can call between #configure and #start
        if (mMediaCodec == null) {
            Log.e("MediaCodec", "Failed to create and configure VIDEO encoder. Device SW/HW is not sufficient for video decoding and encoding.");
            if (mListener != null) {
                try {
                    mListener.onStopped(this);
                    EventBus.getDefault().post(new MediaCodecFailureEvent());
                } catch (final Exception e) {
                    Log.e(TAG, "stopped:", e);
                }
            }
        }

        if (mMediaCodec != null) {
            mSurface = mMediaCodec.createInputSurface();    // API >= 18
            mMediaCodec.start();
            if (mListener != null) {
                try {
                    mListener.onPrepared(this);
                } catch (final Exception e) {
                    Log.e(TAG, "prepare:", e);
                }
            }
        }
    }

    private MediaCodec createAndConfigureMediaCodecByName(MediaFormat format) {
        int codecCount = MediaCodecList.getCodecCount();

        MediaCodecInfo mediaCodecInfo;
        for (int i = 0; i < codecCount; i++) {
            mediaCodecInfo = MediaCodecList.getCodecInfoAt(i);
            if (mediaCodecInfo != null && mediaCodecInfo.isEncoder()) {
                final String[] supportedTypes = mediaCodecInfo.getSupportedTypes();
                if (supportedTypes != null) {
                    for (String supportedType : supportedTypes) {
                        if (TextUtils.equals(supportedType, MIME_TYPE)) {
                            final MediaCodecInfo.CodecCapabilities videoCap = mediaCodecInfo.getCapabilitiesForType(MIME_TYPE);
                            if (videoCap != null) {
                                final int colorFormat = selectColorFormat(mediaCodecInfo, MIME_TYPE);
                                if (colorFormat > 0) {
                                    try {
                                        mMediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
                                    } catch (Exception e) {
                                        Log.d("MediaCodec", "Failed to create encoder by name " + mediaCodecInfo.getName(), e);
                                        continue;
                                    }
                                    try {
                                        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                                    } catch (Exception e) {
                                        Log.d("MediaCodec", "Failed to configure encoder by name " + mediaCodecInfo.getName(), e);
                                        mMediaCodec.release();
                                        mMediaCodec = null;
                                        continue;
                                    }
                                    return mMediaCodec;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    void setEglContext(final EGLContext shared_context, final int tex_id) {
        mRenderHandler.setEglContext(shared_context, tex_id, mSurface, true);
    }

    @Override
    protected void release() {
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mRenderHandler != null) {
            mRenderHandler.release();
            mRenderHandler = null;
        }
        super.release();
    }

    private int calcBitRate() {
        final int bitrate = (int) (BPP * DEFAULT_FRAME_RATE * mWidth * mHeight);
        Log.i(TAG, String.format("bitrate=%5.2f[Mbps]", bitrate / 1024f / 1024f));
        return bitrate;
    }

    /**
     * select the first codec that match a specific MIME type
     *
     * @param mimeType
     * @return null if no codec matched
     */
    private static MediaCodecInfo selectVideoCodec(final String mimeType) {
        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {    // skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color format
            final String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    final int format = selectColorFormat(codecInfo, mimeType);
                    if (format > 0) {
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * select color format available on specific codec and we can use.
     *
     * @return 0 if no colorFormat is matched
     */
    private static int selectColorFormat(final MediaCodecInfo codecInfo, final String mimeType) {
        int result = 0;
        final MediaCodecInfo.CodecCapabilities caps;
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            caps = codecInfo.getCapabilitiesForType(mimeType);
        } finally {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
            colorFormat = caps.colorFormats[i];
            if (isRecognizedViewoFormat(colorFormat)) {
                if (result == 0)
                    result = colorFormat;
                break;
            }
        }
        if (result == 0)
            Log.e(TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return result;
    }

    /**
     * color formats that we can use in this class
     */
    private static int[] recognizedFormats;

    static {
        recognizedFormats = new int[]{
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };
    }

    private static boolean isRecognizedViewoFormat(final int colorFormat) {
        final int n = recognizedFormats != null ? recognizedFormats.length : 0;
        for (int i = 0; i < n; i++) {
            if (recognizedFormats[i] == colorFormat) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void signalEndOfInputStream() {
        if (mMediaCodec != null) {
            mMediaCodec.signalEndOfInputStream();    // API >= 18
        }
        mIsEOS = true;
    }

}
