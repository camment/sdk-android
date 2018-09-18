package tv.camment.cammentsdk.helpers;


import android.os.Handler;
import android.text.TextUtils;

import com.camment.clientsdk.model.Usergroup;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.aws.messages.NeedPlayerStateMessage;
import tv.camment.cammentsdk.aws.messages.PlayerStateMessage;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.events.CheckDisplayedCammentsEvent;
import tv.camment.cammentsdk.views.CammentPlayerListener;

public final class SyncHelper {

    private static final int UPDATE_PERIOD = 60000; //60 seconds
    private static final int SYNC_PERIOD = 500; //0.5s
    private static final int CAMMENT_PERIOD = 1000; //1s

    private static SyncHelper INSTANCE;

    private static Handler handler;
    private static Handler positionHandler;
    private static Handler cammentHandler;

    public static SyncHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (SyncHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SyncHelper();
                }
            }
        }
        return INSTANCE;
    }

    private SyncHelper() {
        handler = new Handler();
        positionHandler = new Handler();
        cammentHandler = new Handler();
    }

    public void onPlaybackPaused(int currentPositionMillis) {
        if (CammentSDK.getInstance().isSyncEnabled()) {
            sendPositionUpdate(currentPositionMillis, false);

            endPeriodicCammentCheck();
        }
    }

    public void onPlaybackStarted(int currentPositionMillis) {
        if (CammentSDK.getInstance().isSyncEnabled()) {

            sendPositionUpdate(currentPositionMillis, true);

            Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
            if (activeUserGroup != null
                    && !TextUtils.isEmpty(activeUserGroup.getUuid())) {
                startPeriodicCammentCheck();
            }
        }
    }

    public void onPlaybackPositionChanged(final int currentPositionMillis, final boolean isPlaying) {
        if (CammentSDK.getInstance().isSyncEnabled()) {
            if (positionHandler != null) {
                positionHandler.removeCallbacksAndMessages(null);

                positionHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendPositionUpdate(currentPositionMillis, isPlaying);
                    }
                }, SYNC_PERIOD);
            }
        }
    }

    public void sendNeedPositionUpdate() {
        if (CammentSDK.getInstance().isSyncEnabled()) {
            if (shouldSendNeedPlayerStateMsg()) {
                Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

                NeedPlayerStateMessage.Body body = new NeedPlayerStateMessage.Body(activeUserGroup.getUuid());

                ApiManager.getInstance().getSyncApi()
                        .sendIotMessage(MessageType.NEED_PLAYER_STATE.getStringValue(), new Gson().toJson(body));
            }
        }
    }

    public void sendPositionUpdate(int currentPositionMillis, boolean isPlaying) {
        if (CammentSDK.getInstance().isSyncEnabled()) {
            if (shouldSendPlayerStateMsg()) {
                Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

                PlayerStateMessage.Body body = new PlayerStateMessage.Body(activeUserGroup.getUuid(), isPlaying, roundedPosition(currentPositionMillis));

                ApiManager.getInstance().getSyncApi()
                        .sendIotMessage(MessageType.PLAYER_STATE.getStringValue(), new Gson().toJson(body));
            }
        }
    }

    private boolean shouldSendPlayerStateMsg() {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup == null
                || TextUtils.isEmpty(activeUserGroup.getUuid()))
            return false;

        String identityId = IdentityPreferences.getInstance().getIdentityId();

        return TextUtils.equals(identityId, activeUserGroup.getHostId());
    }

    private boolean shouldSendNeedPlayerStateMsg() {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup == null
                || TextUtils.isEmpty(activeUserGroup.getUuid()))
            return false;

        String identityId = IdentityPreferences.getInstance().getIdentityId();

        return !TextUtils.equals(identityId, activeUserGroup.getHostId());
    }

    private int roundedPosition(int millis) {
        return Math.round(millis / 1000f);
    }

    public void startPeriodicPositionUpdate() {
        if (CammentSDK.getInstance().isSyncEnabled()) {
            endPeriodicPositionUpdate();

            if (handler != null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CammentPlayerListener cammentPlayerListener = CammentSDK.getInstance().getCammentPlayerListener();
                        if (cammentPlayerListener != null) {
                            sendPositionUpdate(cammentPlayerListener.getCurrentPosition(), cammentPlayerListener.isPlaying());
                            startPeriodicPositionUpdate();
                        }
                    }
                }, UPDATE_PERIOD);
            }
        }
    }

    public void endPeriodicPositionUpdate() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void startPeriodicCammentCheck() {
        if (CammentSDK.getInstance().isSyncEnabled()) {
            endPeriodicCammentCheck();

            cammentHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post(new CheckDisplayedCammentsEvent());
                    startPeriodicCammentCheck();
                }
            }, CAMMENT_PERIOD);
        }
    }

    public void endPeriodicCammentCheck() {
        if (cammentHandler != null) {
            cammentHandler.removeCallbacksAndMessages(null);
        }
    }

    public void cleanAllHandlers() {
        endPeriodicCammentCheck();
        endPeriodicPositionUpdate();

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void restartHandlersIfNeeded() {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup != null && !TextUtils.isEmpty(activeUserGroup.getUuid())) {
            startPeriodicCammentCheck();

            String identityId = IdentityPreferences.getInstance().getIdentityId();
            if (TextUtils.equals(activeUserGroup.getHostId(), identityId)) {
                startPeriodicPositionUpdate();
            }
        }
    }

}
