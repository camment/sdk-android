package tv.camment.cammentsdk.utils;


import android.net.Uri;

import com.camment.clientsdk.model.Camment;

import java.io.File;

import tv.camment.cammentsdk.CammentSDK;


public final class FileUtils {

    private static final String ROOT_DIR = CammentSDK.getInstance().getApplicationContext().getFilesDir().getPath();
    private static final String UPLOADS_DIR = ROOT_DIR + File.separator + "uploads" + File.separator;

    private static FileUtils instance = new FileUtils();

    public static FileUtils getInstance() {
        return instance;
    }

    public String getUploadCammentPath(String cammentUuid) {
        return UPLOADS_DIR + cammentUuid + ".mp4";
    }

    public boolean isLocalVideoAvailable(String cammentUuid) {
        final File file = new File(getUploadCammentPath(cammentUuid));
        return file.exists();
    }

    public Uri getVideoUri(Camment camment) {
        return isLocalVideoAvailable(camment.getUuid())
                ? Uri.parse(getUploadCammentPath(camment.getUuid()))
                : Uri.parse(camment.getUrl());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getUploadCammentFile(String cammentUuid) {
        final File dir = new File(UPLOADS_DIR);
        dir.mkdirs();
        if (dir.canWrite()) {
            return new File(dir, cammentUuid + ".mp4");
        }
        return null;
    }

    public String getRootDirectory() {
        return ROOT_DIR;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteCammentFile(String uuid) {
        String filePath = UPLOADS_DIR + uuid + ".mp4";

        File file = new File(filePath);
        if (file.isFile()) {
            file.delete();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteAllFiles() {
        File dir = new File(UPLOADS_DIR);
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

}
