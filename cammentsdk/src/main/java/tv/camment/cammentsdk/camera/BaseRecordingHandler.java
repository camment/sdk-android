package tv.camment.cammentsdk.camera;

import android.text.TextUtils;
import android.util.Log;

import com.camment.clientsdk.model.Camment;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.utils.FileUtils;
import tv.camment.cammentsdk.views.CammentAudioListener;

abstract class BaseRecordingHandler extends CammentAsyncClient {

    private MediaMuxerWrapper mediaMuxer;
    private final MediaEncoder.MediaEncoderListener mediaEncoderListener;

    BaseRecordingHandler(ExecutorService executorService, MediaEncoder.MediaEncoderListener mediaEncoderListener) {
        super(executorService);
        this.mediaEncoderListener = mediaEncoderListener;
    }

    void startRecording() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final Camment camment = getNewUploadCamment();

                mediaMuxer = new MediaMuxerWrapper(camment.getUuid());

                new MediaVideoEncoder(mediaMuxer, mediaEncoderListener, SDKConfig.CAMMENT_SIZE, SDKConfig.CAMMENT_SIZE);

                new MediaAudioEncoder(mediaMuxer, mediaEncoderListener);

                mediaMuxer.prepare();
                mediaMuxer.startRecording();
                return new Object();
            }
        }, startRecordingCallback());
    }

    private CammentCallback<Object> startRecordingCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "startRecording", exception);
            }
        };
    }

    void stopRecording(boolean cancelled, CammentAudioListener cammentAudioListener) {
        if (mediaMuxer != null) {
            submitBgTask(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String cammentUuid = null;
                    if (mediaMuxer != null) {
                        mediaMuxer.stopRecording();
                        cammentUuid = mediaMuxer.getCammentUuid();
                    }
                    return cammentUuid;
                }
            }, stopRecordingCallback(cancelled, cammentAudioListener));
        }
    }

    private CammentCallback<String> stopRecordingCallback(final boolean cancelled,
                                                          final CammentAudioListener cammentAudioListener) {
        return new CammentCallback<String>() {
            @Override
            public void onSuccess(String cammentUuid) {
                if (!TextUtils.isEmpty(cammentUuid)) {
                    final CCamment camment = CammentProvider.getCammentByUuid(cammentUuid);
                    if (!cancelled) {
                        if (cammentAudioListener != null) {
                            cammentAudioListener.onCammentRecordingEnded();
                        }

                        if (camment != null
                                && !TextUtils.isEmpty(camment.getUuid())) {
                            ApiManager.getInstance().getGroupApi()
                                    .createEmptyUsergroupIfNeededAndUploadCamment(camment);
                        }
                    } else {
                        CammentProvider.deleteCammentByUuid(cammentUuid);
                        FileUtils.getInstance().deleteCammentFile(cammentUuid);
                    }
                }
            }

            @Override
            public void onException(Exception e) {
                Log.e("onException", "stopRecording", e);
            }
        };
    }

    private Camment getNewUploadCamment() {
        final String showUuid = GeneralPreferences.getInstance().getActiveShowUuid();

        CCamment camment = new CCamment();
        camment.setUuid(UUID.randomUUID().toString());
        camment.setShowUuid(showUuid);
        camment.setUrl(FileUtils.getInstance().getUploadCammentFile(camment.getUuid()).toString());
        camment.setRecorded(false);
        camment.setTimestamp(System.currentTimeMillis());

        CammentProvider.insertCamment(camment);

        return camment;
    }
}
