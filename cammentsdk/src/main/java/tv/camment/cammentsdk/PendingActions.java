package tv.camment.cammentsdk;


import java.util.LinkedList;
import java.util.Queue;

public final class PendingActions {

    private static PendingActions INSTANCE;

    private Queue<Action> actionQueue;

    public static PendingActions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PendingActions();
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
                break;
            case CHECK_DEEPLINK:
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

    enum Action {
        SHOW_SHARING_OPTIONS,
        CHECK_DEEPLINK,
    }
}
