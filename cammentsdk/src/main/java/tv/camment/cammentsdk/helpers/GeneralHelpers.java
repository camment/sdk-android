package tv.camment.cammentsdk.helpers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GeneralHelpers {

    private static GeneralHelpers INSTANCE;

    private final ExecutorService executorService;

    public static GeneralHelpers getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GeneralHelpers();
        }
        return INSTANCE;
    }

    private GeneralHelpers() {
        executorService = Executors.newSingleThreadExecutor();

    }

    public BitmapRetriever getBitmapRetriever() {
        return new BitmapRetriever(executorService);
    }

}
