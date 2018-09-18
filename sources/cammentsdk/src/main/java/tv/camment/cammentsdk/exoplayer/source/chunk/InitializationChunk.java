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
package tv.camment.cammentsdk.exoplayer.source.chunk;

import java.io.IOException;

import tv.camment.cammentsdk.exoplayer.C;
import tv.camment.cammentsdk.exoplayer.Format;
import tv.camment.cammentsdk.exoplayer.extractor.DefaultExtractorInput;
import tv.camment.cammentsdk.exoplayer.extractor.Extractor;
import tv.camment.cammentsdk.exoplayer.extractor.ExtractorInput;
import tv.camment.cammentsdk.exoplayer.upstream.DataSource;
import tv.camment.cammentsdk.exoplayer.upstream.DataSpec;
import tv.camment.cammentsdk.exoplayer.util.Assertions;
import tv.camment.cammentsdk.exoplayer.util.Util;

/**
 * A {@link Chunk} that uses an {@link Extractor} to decode initialization data for single track.
 */
public final class InitializationChunk extends Chunk {

    private final ChunkExtractorWrapper extractorWrapper;

    private volatile int bytesLoaded;
    private volatile boolean loadCanceled;

    /**
     * @param dataSource           The source from which the data should be loaded.
     * @param dataSpec             Defines the data to be loaded.
     * @param trackFormat          See {@link #trackFormat}.
     * @param trackSelectionReason See {@link #trackSelectionReason}.
     * @param trackSelectionData   See {@link #trackSelectionData}.
     * @param extractorWrapper     A wrapped extractor to use for parsing the initialization data.
     */
    public InitializationChunk(DataSource dataSource, DataSpec dataSpec, Format trackFormat,
                               int trackSelectionReason, Object trackSelectionData,
                               ChunkExtractorWrapper extractorWrapper) {
        super(dataSource, dataSpec, C.DATA_TYPE_MEDIA_INITIALIZATION, trackFormat, trackSelectionReason,
                trackSelectionData, C.TIME_UNSET, C.TIME_UNSET);
        this.extractorWrapper = extractorWrapper;
    }

    @Override
    public long bytesLoaded() {
        return bytesLoaded;
    }

    // Loadable implementation.

    @Override
    public void cancelLoad() {
        loadCanceled = true;
    }

    @Override
    public boolean isLoadCanceled() {
        return loadCanceled;
    }

    @SuppressWarnings("NonAtomicVolatileUpdate")
    @Override
    public void load() throws IOException, InterruptedException {
        DataSpec loadDataSpec = dataSpec.subrange(bytesLoaded);
        try {
            // Create and open the input.
            ExtractorInput input = new DefaultExtractorInput(dataSource,
                    loadDataSpec.absoluteStreamPosition, dataSource.open(loadDataSpec));
            if (bytesLoaded == 0) {
                extractorWrapper.init(null);
            }
            // Load and decode the initialization data.
            try {
                Extractor extractor = extractorWrapper.extractor;
                int result = Extractor.RESULT_CONTINUE;
                while (result == Extractor.RESULT_CONTINUE && !loadCanceled) {
                    result = extractor.read(input, null);
                }
                Assertions.checkState(result != Extractor.RESULT_SEEK);
            } finally {
                bytesLoaded = (int) (input.getPosition() - dataSpec.absoluteStreamPosition);
            }
        } finally {
            Util.closeQuietly(dataSource);
        }
    }

}
