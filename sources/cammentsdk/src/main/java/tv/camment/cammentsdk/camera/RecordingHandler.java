package tv.camment.cammentsdk.camera;

import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.views.CammentAudioListener;

public final class RecordingHandler extends BaseRecordingHandler {

    public RecordingHandler(ExecutorService executorService, MediaEncoder.MediaEncoderListener mediaEncoderListener) {
        super(executorService, mediaEncoderListener);
    }

    public void startRecording() {
        super.startRecording();
    }

    public void stopRecording(boolean cancelled, CammentAudioListener cammentAudioListener) {
        super.stopRecording(cancelled, cammentAudioListener);
    }
}
