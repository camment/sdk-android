package tv.camment.cammentsdk.utils;


import android.util.Log;

import java.io.File;

import tv.camment.cammentsdk.CammentSDK;

public class FileUtils {

    private static final String ROOT_DIR = CammentSDK.getInstance().getApplicationContext().getExternalFilesDir(null)
            + File.separator;
    private static final String UPLOADS_DIR = ROOT_DIR + "uploads" + File.separator;


    private static FileUtils instance = new FileUtils();

    public static FileUtils getInstance() {
        return instance;
    }

    public String getUploadCammentPath(String cammentUuid) {
        return UPLOADS_DIR + cammentUuid + ".mp4";
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getUploadCammentFile(String cammentUuid) {
        final File dir = new File(UPLOADS_DIR);
        dir.mkdirs();
        if (dir.canWrite()) {
            Log.d("FileUtils", "canWrite");
            return new File(dir, cammentUuid + ".mp4");
        }
        return null;
    }

}
