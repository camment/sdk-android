package tv.camment.cammentsdk.helpers;


import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.CammentSnackBarDialog;
import tv.camment.cammentsdk.R;

public final class SnackbarQueueHelper {

    public static final int SHORT = 3500;
    public static final int LONG = 5500;

    private static SnackbarQueueHelper INSTANCE;

    private Queue<Snackbar> queue;

    public static SnackbarQueueHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (SnackbarQueueHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SnackbarQueueHelper();
                }
            }
        }
        return INSTANCE;
    }

    private SnackbarQueueHelper() {
        queue = new LinkedBlockingDeque<>();
    }

    public synchronized void addSnackbar(Snackbar snackbar) {
        if (queue != null) {
            queue.add(snackbar);
        }

        showSnackbarIfNeeded(false);
    }

    private synchronized void showSnackbarIfNeeded(boolean forceDisplay) {
        if (queue != null
                && queue.size() > 0) {
            if (forceDisplay || queue.size() == 1) {
                displaySnackbar(queue.peek());
            }
        }
    }

    private synchronized void displaySnackbar(Snackbar snackbar) {
        Activity activity = CammentSDK.getInstance().getCurrentActivity();
        if (activity instanceof FragmentActivity) {
            CammentSnackBarDialog cammentSnackBarDialog = null;

            switch (snackbar.snackbarType) {
                case YOU_JOINED_GROUP:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(activity.getString(R.string.cmmsdk_joined_private_chat));
                    break;
                case USER_ONLINE:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(String.format(activity.getString(R.string.cmmsdk_user_came_online), snackbar.msgVar));
                    break;
                case USER_OFFLINE:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(String.format(activity.getString(R.string.cmmsdk_user_went_offline), snackbar.msgVar));
                    break;
                case SYNCING_WITH_HOST:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(String.format(activity.getString(R.string.cmmsdk_we_are_syncing_you), snackbar.msgVar));
                    break;
                case ME_HOST_NOW:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(activity.getString(R.string.cmmsdk_you_host_now));
                    break;
                case USER_HOST_NOW:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(String.format(activity.getString(R.string.cmmsdk_someone_host_now), snackbar.msgVar));
                    break;
                case VIDEO_PAUSED:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(activity.getString(R.string.cmmsdk_paused_video));
                    break;
                case CODEC_ISSUE:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(activity.getString(R.string.cmmsdk_video_codec_issue));
                    break;
                case USER_JOINED_GROUP:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(String.format(activity.getString(R.string.cmmsdk_user_has_joined_title), snackbar.msgVar));
                    break;
                case USER_LEFT_GROUP:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(String.format(activity.getString(R.string.cmmsdk_user_left_group), snackbar.msgVar));
                    break;
                case USER_BLOCKED:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(String.format(activity.getString(R.string.cmmsdk_user_blocked), snackbar.msgVar));
                    break;
                case USER_UNBLOCKED:
                    cammentSnackBarDialog = CammentSnackBarDialog.createMsgInstance(String.format(activity.getString(R.string.cmmsdk_user_unblocked), snackbar.msgVar));
                    break;
            }

            if (cammentSnackBarDialog != null) {
                FragmentManager supportFragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();

                cammentSnackBarDialog.show(supportFragmentManager, snackbar.snackbarType.getStringValue(), snackbar.duration);
            }
        }
    }

    public synchronized void removeSnackbarFromQueue() {
        if (queue != null
                && queue.size() > 0) {
            queue.remove();
        }

        showSnackbarIfNeeded(true);
    }

    public static final class Snackbar {
        private final SnackbarType snackbarType;
        private long duration;
        private String msgVar;

        public Snackbar(SnackbarType snackbarType, long duration) {
            this.snackbarType = snackbarType;
            this.duration = duration;
        }

        public void setMsgVar(String msgVar) {
            this.msgVar = msgVar;
        }
    }

}
