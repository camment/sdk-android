/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.camment.cammentsdk.exoplayer.source;

import java.io.IOException;

import tv.camment.cammentsdk.exoplayer.C;
import tv.camment.cammentsdk.exoplayer.Format;
import tv.camment.cammentsdk.exoplayer.FormatHolder;
import tv.camment.cammentsdk.exoplayer.SeekParameters;
import tv.camment.cammentsdk.exoplayer.decoder.DecoderInputBuffer;
import tv.camment.cammentsdk.exoplayer.trackselection.TrackSelection;
import tv.camment.cammentsdk.exoplayer.util.Assertions;
import tv.camment.cammentsdk.exoplayer.util.MimeTypes;

/**
 * Wraps a {@link MediaPeriod} and clips its {@link SampleStream}s to provide a subsequence of their
 * samples.
 */
public final class ClippingMediaPeriod implements MediaPeriod, MediaPeriod.Callback {

    /**
     * The {@link MediaPeriod} wrapped by this clipping media period.
     */
    public final MediaPeriod mediaPeriod;

    private MediaPeriod.Callback callback;
    private ClippingSampleStream[] sampleStreams;
    private long pendingInitialDiscontinuityPositionUs;
    /* package */ long startUs;
    /* package */ long endUs;

    /**
     * Creates a new clipping media period that provides a clipped view of the specified
     * {@link MediaPeriod}'s sample streams.
     * <p>
     * The clipping start/end positions must be specified by calling {@link #setClipping(long, long)}
     * on the playback thread before preparation completes.
     * <p>
     * If the start point is guaranteed to be a key frame, pass {@code false} to {@code
     * enableInitialPositionDiscontinuity} to suppress an initial discontinuity when the period is
     * first read from.
     *
     * @param mediaPeriod                The media period to clip.
     * @param enableInitialDiscontinuity Whether the initial discontinuity should be enabled.
     */
    public ClippingMediaPeriod(MediaPeriod mediaPeriod, boolean enableInitialDiscontinuity) {
        this.mediaPeriod = mediaPeriod;
        sampleStreams = new ClippingSampleStream[0];
        pendingInitialDiscontinuityPositionUs = enableInitialDiscontinuity ? 0 : C.TIME_UNSET;
        startUs = C.TIME_UNSET;
        endUs = C.TIME_UNSET;
    }

    /**
     * Sets the clipping start/end times for this period, in microseconds.
     *
     * @param startUs The clipping start time, in microseconds.
     * @param endUs   The clipping end time, in microseconds, or {@link C#TIME_END_OF_SOURCE} to
     *                indicate the end of the period.
     */
    public void setClipping(long startUs, long endUs) {
        this.startUs = startUs;
        this.endUs = endUs;
    }

    @Override
    public void prepare(MediaPeriod.Callback callback, long positionUs) {
        this.callback = callback;
        mediaPeriod.prepare(this, startUs + positionUs);
    }

    @Override
    public void maybeThrowPrepareError() throws IOException {
        mediaPeriod.maybeThrowPrepareError();
    }

    @Override
    public TrackGroupArray getTrackGroups() {
        return mediaPeriod.getTrackGroups();
    }

    @Override
    public long selectTracks(TrackSelection[] selections, boolean[] mayRetainStreamFlags,
                             SampleStream[] streams, boolean[] streamResetFlags, long positionUs) {
        sampleStreams = new ClippingSampleStream[streams.length];
        SampleStream[] childStreams = new SampleStream[streams.length];
        for (int i = 0; i < streams.length; i++) {
            sampleStreams[i] = (ClippingSampleStream) streams[i];
            childStreams[i] = sampleStreams[i] != null ? sampleStreams[i].childStream : null;
        }
        long enablePositionUs = mediaPeriod.selectTracks(selections, mayRetainStreamFlags,
                childStreams, streamResetFlags, positionUs + startUs) - startUs;
        pendingInitialDiscontinuityPositionUs = isPendingInitialDiscontinuity() && positionUs == 0
                && shouldKeepInitialDiscontinuity(startUs, selections) ? enablePositionUs : C.TIME_UNSET;
        Assertions.checkState(enablePositionUs == positionUs
                || (enablePositionUs >= 0
                && (endUs == C.TIME_END_OF_SOURCE || startUs + enablePositionUs <= endUs)));
        for (int i = 0; i < streams.length; i++) {
            if (childStreams[i] == null) {
                sampleStreams[i] = null;
            } else if (streams[i] == null || sampleStreams[i].childStream != childStreams[i]) {
                sampleStreams[i] = new ClippingSampleStream(childStreams[i]);
            }
            streams[i] = sampleStreams[i];
        }
        return enablePositionUs;
    }

    @Override
    public void discardBuffer(long positionUs, boolean toKeyframe) {
        mediaPeriod.discardBuffer(positionUs + startUs, toKeyframe);
    }

    @Override
    public void reevaluateBuffer(long positionUs) {
        mediaPeriod.reevaluateBuffer(positionUs + startUs);
    }

    @Override
    public long readDiscontinuity() {
        if (isPendingInitialDiscontinuity()) {
            long initialDiscontinuityUs = pendingInitialDiscontinuityPositionUs;
            pendingInitialDiscontinuityPositionUs = C.TIME_UNSET;
            // Always read an initial discontinuity from the child, and use it if set.
            long childDiscontinuityUs = readDiscontinuity();
            return childDiscontinuityUs != C.TIME_UNSET ? childDiscontinuityUs : initialDiscontinuityUs;
        }
        long discontinuityUs = mediaPeriod.readDiscontinuity();
        if (discontinuityUs == C.TIME_UNSET) {
            return C.TIME_UNSET;
        }
        Assertions.checkState(discontinuityUs >= startUs);
        Assertions.checkState(endUs == C.TIME_END_OF_SOURCE || discontinuityUs <= endUs);
        return discontinuityUs - startUs;
    }

    @Override
    public long getBufferedPositionUs() {
        long bufferedPositionUs = mediaPeriod.getBufferedPositionUs();
        if (bufferedPositionUs == C.TIME_END_OF_SOURCE
                || (endUs != C.TIME_END_OF_SOURCE && bufferedPositionUs >= endUs)) {
            return C.TIME_END_OF_SOURCE;
        }
        return Math.max(0, bufferedPositionUs - startUs);
    }

    @Override
    public long seekToUs(long positionUs) {
        pendingInitialDiscontinuityPositionUs = C.TIME_UNSET;
        for (ClippingSampleStream sampleStream : sampleStreams) {
            if (sampleStream != null) {
                sampleStream.clearSentEos();
            }
        }
        long offsetPositionUs = positionUs + startUs;
        long seekUs = mediaPeriod.seekToUs(offsetPositionUs);
        Assertions.checkState(
                seekUs == offsetPositionUs
                        || (seekUs >= startUs && (endUs == C.TIME_END_OF_SOURCE || seekUs <= endUs)));
        return seekUs - startUs;
    }

    @Override
    public long getAdjustedSeekPositionUs(long positionUs, SeekParameters seekParameters) {
        if (positionUs == startUs) {
            // Never adjust seeks to the start of the clipped view.
            return 0;
        }
        long offsetPositionUs = positionUs + startUs;
        SeekParameters clippedSeekParameters = clipSeekParameters(offsetPositionUs, seekParameters);
        return mediaPeriod.getAdjustedSeekPositionUs(offsetPositionUs, clippedSeekParameters) - startUs;
    }

    @Override
    public long getNextLoadPositionUs() {
        long nextLoadPositionUs = mediaPeriod.getNextLoadPositionUs();
        if (nextLoadPositionUs == C.TIME_END_OF_SOURCE
                || (endUs != C.TIME_END_OF_SOURCE && nextLoadPositionUs >= endUs)) {
            return C.TIME_END_OF_SOURCE;
        }
        return nextLoadPositionUs - startUs;
    }

    @Override
    public boolean continueLoading(long positionUs) {
        return mediaPeriod.continueLoading(positionUs + startUs);
    }

    // MediaPeriod.Callback implementation.

    @Override
    public void onPrepared(MediaPeriod mediaPeriod) {
        Assertions.checkState(startUs != C.TIME_UNSET && endUs != C.TIME_UNSET);
        callback.onPrepared(this);
    }

    @Override
    public void onContinueLoadingRequested(MediaPeriod source) {
        callback.onContinueLoadingRequested(this);
    }

    /* package */ boolean isPendingInitialDiscontinuity() {
        return pendingInitialDiscontinuityPositionUs != C.TIME_UNSET;
    }

    private SeekParameters clipSeekParameters(long offsetPositionUs, SeekParameters seekParameters) {
        long toleranceBeforeMs = Math.min(offsetPositionUs - startUs, seekParameters.toleranceBeforeUs);
        long toleranceAfterMs =
                endUs == C.TIME_END_OF_SOURCE
                        ? seekParameters.toleranceAfterUs
                        : Math.min(endUs - offsetPositionUs, seekParameters.toleranceAfterUs);
        if (toleranceBeforeMs == seekParameters.toleranceBeforeUs
                && toleranceAfterMs == seekParameters.toleranceAfterUs) {
            return seekParameters;
        } else {
            return new SeekParameters(toleranceBeforeMs, toleranceAfterMs);
        }
    }

    private static boolean shouldKeepInitialDiscontinuity(long startUs, TrackSelection[] selections) {
        // If the clipping start position is non-zero, the clipping sample streams will adjust
        // timestamps on buffers they read from the unclipped sample streams. These adjusted buffer
        // timestamps can be negative, because sample streams provide buffers starting at a key-frame,
        // which may be before the clipping start point. When the renderer reads a buffer with a
        // negative timestamp, its offset timestamp can jump backwards compared to the last timestamp
        // read in the previous period. Renderer implementations may not allow this, so we signal a
        // discontinuity which resets the renderers before they read the clipping sample stream.
        // However, for audio-only track selections we assume to have random access seek behaviour and
        // do not need an initial discontinuity to reset the renderer.
        if (startUs != 0) {
            for (TrackSelection trackSelection : selections) {
                if (trackSelection != null) {
                    Format selectedFormat = trackSelection.getSelectedFormat();
                    if (!MimeTypes.isAudio(selectedFormat.sampleMimeType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Wraps a {@link SampleStream} and clips its samples.
     */
    private final class ClippingSampleStream implements SampleStream {

        public final SampleStream childStream;

        private boolean sentEos;

        public ClippingSampleStream(SampleStream childStream) {
            this.childStream = childStream;
        }

        public void clearSentEos() {
            sentEos = false;
        }

        @Override
        public boolean isReady() {
            return !isPendingInitialDiscontinuity() && childStream.isReady();
        }

        @Override
        public void maybeThrowError() throws IOException {
            childStream.maybeThrowError();
        }

        @Override
        public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer,
                            boolean requireFormat) {
            if (isPendingInitialDiscontinuity()) {
                return C.RESULT_NOTHING_READ;
            }
            if (sentEos) {
                buffer.setFlags(C.BUFFER_FLAG_END_OF_STREAM);
                return C.RESULT_BUFFER_READ;
            }
            int result = childStream.readData(formatHolder, buffer, requireFormat);
            if (result == C.RESULT_FORMAT_READ) {
                Format format = formatHolder.format;
                if (format.encoderDelay != Format.NO_VALUE || format.encoderPadding != Format.NO_VALUE) {
                    // Clear gapless playback metadata if the start/end points don't match the media.
                    int encoderDelay = startUs != 0 ? 0 : format.encoderDelay;
                    int encoderPadding = endUs != C.TIME_END_OF_SOURCE ? 0 : format.encoderPadding;
                    formatHolder.format = format.copyWithGaplessInfo(encoderDelay, encoderPadding);
                }
                return C.RESULT_FORMAT_READ;
            }
            if (endUs != C.TIME_END_OF_SOURCE
                    && ((result == C.RESULT_BUFFER_READ && buffer.timeUs >= endUs)
                    || (result == C.RESULT_NOTHING_READ
                    && getBufferedPositionUs() == C.TIME_END_OF_SOURCE))) {
                buffer.clear();
                buffer.setFlags(C.BUFFER_FLAG_END_OF_STREAM);
                sentEos = true;
                return C.RESULT_BUFFER_READ;
            }
            if (result == C.RESULT_BUFFER_READ && !buffer.isEndOfStream()) {
                buffer.timeUs -= startUs;
            }
            return result;
        }

        @Override
        public int skipData(long positionUs) {
            if (isPendingInitialDiscontinuity()) {
                return C.RESULT_NOTHING_READ;
            }
            return childStream.skipData(startUs + positionUs);
        }

    }

}
