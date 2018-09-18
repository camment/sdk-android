package tv.camment.cammentsdk.camera;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.utils.FileUtils;

final class MediaMuxerWrapper {
    private static final String TAG = "MediaMuxerWrapper";

    private String mCammentUuid;

    private final MediaMuxer mMediaMuxer;    // API >= 18
    private int mEncoderCount, mStatredCount;
    private boolean mIsStarted;
    private MediaEncoder mVideoEncoder, mAudioEncoder;

    /**
     * Constructor
     *
     * @throws IOException
     */
    MediaMuxerWrapper(String cammentUuid) throws IOException {
        mCammentUuid = cammentUuid;
        String mOutputPath = FileUtils.getInstance().getUploadCammentPath(cammentUuid);
        mMediaMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mEncoderCount = mStatredCount = 0;
        mIsStarted = false;
    }

    String getCammentUuid() {
        return mCammentUuid;
    }

    void prepare() throws IOException {
        if (mVideoEncoder != null)
            mVideoEncoder.prepare();
        if (mAudioEncoder != null)
            mAudioEncoder.prepare();
    }

    void startRecording() {
        CammentProvider.setStartTimestamp(mCammentUuid, System.currentTimeMillis());
        if (mVideoEncoder != null)
            mVideoEncoder.startRecording();
        if (mAudioEncoder != null)
            mAudioEncoder.startRecording();
    }

    void stopRecording() {
        CammentProvider.setEndTimestamp(mCammentUuid, System.currentTimeMillis());
        if (mVideoEncoder != null)
            mVideoEncoder.stopRecording();
        mVideoEncoder = null;
        if (mAudioEncoder != null)
            mAudioEncoder.stopRecording();
        mAudioEncoder = null;
        mCammentUuid = null;
    }

    synchronized boolean isStarted() {
        return mIsStarted;
    }

//**********************************************************************
//**********************************************************************

    /**
     * assign encoder to this calss. this is called from encoder.
     *
     * @param encoder instance of MediaVideoEncoder or MediaAudioEncoder
     */
    void addEncoder(final MediaEncoder encoder) {
        if (encoder instanceof MediaVideoEncoder) {
            if (mVideoEncoder != null)
                throw new IllegalArgumentException("Video encoder already added.");
            mVideoEncoder = encoder;
        } else if (encoder instanceof MediaAudioEncoder) {
            if (mAudioEncoder != null)
                throw new IllegalArgumentException("Video encoder already added.");
            mAudioEncoder = encoder;
        } else
            throw new IllegalArgumentException("unsupported encoder");
        mEncoderCount = (mVideoEncoder != null ? 1 : 0) + (mAudioEncoder != null ? 1 : 0);
    }

    /**
     * request start recording from encoder
     *
     * @return true when muxer is ready to write
     */
    synchronized boolean start() {
        mStatredCount++;
        if ((mEncoderCount > 0) && (mStatredCount == mEncoderCount)) {
            mMediaMuxer.start();
            mIsStarted = true;
            notifyAll();
        }
        return mIsStarted;
    }

    /**
     * request stop recording from encoder when encoder received EOS
     */
    synchronized void stop() {
        mStatredCount--;
        if ((mEncoderCount > 0) && (mStatredCount <= 0)) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mIsStarted = false;
        }
    }

    /**
     * assign encoder to muxer
     *
     * @param format
     * @return minus value indicate error
     */
    synchronized int addTrack(final MediaFormat format) {
        if (mIsStarted)
            throw new IllegalStateException("muxer already started");
        return mMediaMuxer.addTrack(format);
    }

    /**
     * write encoded data to muxer
     *
     * @param trackIndex
     * @param byteBuf
     * @param bufferInfo
     */
    synchronized void writeSampleData(final int trackIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo) {
        if (mStatredCount > 0)
            mMediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }

}
