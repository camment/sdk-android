package tv.camment.cammentsdk;


import java.util.LinkedList;
import java.util.Queue;

import tv.camment.cammentsdk.api.ApiManager;

public final class PendingActions {

    private static PendingActions INSTANCE;

    private Queue<Action> actionQueue;

    public static PendingActions getInstance() {
        if (INSTANCE == null) {
            synchronized (PendingActions.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PendingActions();
                }
            }
        }
        return INSTANCE;
    }

    private PendingActions() {
        actionQueue = new LinkedList<>();
    }

    public void addAction(Action action) {
        actionQueue.add(action);
    }

    public void executePendingActionsIfNeeded() {
        if (actionQueue != null
                && actionQueue.size() > 0) {
            processAction(actionQueue.element());
        }
    }

    private void processAction(Action action) {
        switch (action) {
            case SHOW_SHARING_OPTIONS:
                ApiManager.getInstance().getGroupApi().createEmptyUsergroupIfNeededAndGetDeeplink();
                break;
            case HANDLE_DEEPLINK:
                CammentSDK.getInstance().handleDeeplink();
                break;
        }
        removeProcessedAction();
    }

    private void removeProcessedAction() {
        if (actionQueue != null
                && actionQueue.size() > 0) {
            actionQueue.remove();
        }

        executePendingActionsIfNeeded();
    }

    public enum Action {
        SHOW_SHARING_OPTIONS,
        HANDLE_DEEPLINK,
    }

}
