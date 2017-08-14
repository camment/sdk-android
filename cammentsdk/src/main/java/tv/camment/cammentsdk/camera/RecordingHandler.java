package tv.camment.cammentsdk.camera;

import android.text.TextUtils;
import android.util.Log;

import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.Usergroup;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.camera.gl_encoder.MediaAudioEncoder;
import tv.camment.cammentsdk.camera.gl_encoder.MediaEncoder;
import tv.camment.cammentsdk.camera.gl_encoder.MediaMuxerWrapper;
import tv.camment.cammentsdk.camera.gl_encoder.MediaVideoEncoder;
import tv.camment.cammentsdk.data.CammentUploadProvider;
import tv.camment.cammentsdk.data.ShowProvider;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CammentUpload;
import tv.camment.cammentsdk.utils.FileUtils;

/**
 * Created by petrushka on 07/08/2017.
 */

public class RecordingHandler extends CammentAsyncClient {

    private MediaMuxerWrapper mediaMuxer;
    private final MediaEncoder.MediaEncoderListener mediaEncoderListener;

    public RecordingHandler(ExecutorService executorService, MediaEncoder.MediaEncoderListener mediaEncoderListener) {
        super(executorService);
        this.mediaEncoderListener = mediaEncoderListener;
    }

    public void startRecording() {
        submitBgTask(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final Camment camment = getNewUploadCamment();

                Log.d("RECORDING", "startRecording " + camment.getUrl());

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
                Log.d("onSuccess", "startRecording");
            }

            @Override
            public void onException(Exception exception) {
                Log.d("onException", "startRecording");
            }
        };
    }

    public void stopRecording(boolean cancelled) {
        Log.d("RECORDING", "stopRecording - check muxer ");

        if (mediaMuxer != null) {
            Log.d("RECORDING", "stopRecording - cancelled " + cancelled);

            submitBgTask(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String cammentUuid = null;
                    if (mediaMuxer != null) {
                        mediaMuxer.stopRecording();
                        cammentUuid = mediaMuxer.getCammentUuid();
                    }
                    mediaMuxer = null;
                    return cammentUuid;
                }
            }, stopRecordingCallback(cancelled));
        }
    }

    private CammentCallback<String> stopRecordingCallback(final boolean cancelled) {
        return new CammentCallback<String>() {
            @Override
            public void onSuccess(String cammentUuid) {
                Log.d("onSuccess", "stopRecording");
                if (!TextUtils.isEmpty(cammentUuid)) {
                    final CammentUpload camment = CammentUploadProvider.getCammentUploadByUuid(cammentUuid);
                    if (!cancelled) {
                        if (camment != null
                                && !TextUtils.isEmpty(camment.getUuid())) {
                            //TODO uncomment
                            AWSManager.getInstance().getS3UploadHelper().uploadCammentFile(camment);
                        }
                    } else {
                        CammentUploadProvider.deleteCammentUploadByUuid(cammentUuid);
                        FileUtils.getInstance().deleteCammentFile(cammentUuid);
                    }
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.d("onException", "stopRecording");
            }
        };
    }

    private Camment getNewUploadCamment() {
        final Usergroup usergroup = UserGroupProvider.getUserGroup();
        final String showUuid = ShowProvider.getShow().getUuid();

        CammentUpload camment = new CammentUpload();
        camment.setUuid(UUID.randomUUID().toString());
        camment.setShowUuid(showUuid);
        camment.setUrl(FileUtils.getInstance().getUploadCammentFile(camment.getUuid()).toString());
        camment.setUserGroupUuid(usergroup.getUuid());

        CammentUploadProvider.insertCammentUpload(camment);

        return camment;
    }
}
