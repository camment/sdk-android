package tv.camment.cammentsdk.data;

import com.camment.clientsdk.DevcammentClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tv.camment.cammentsdk.api.CammentApi;
import tv.camment.cammentsdk.api.GroupApi;
import tv.camment.cammentsdk.api.InvitationApi;
import tv.camment.cammentsdk.api.ShowApi;
import tv.camment.cammentsdk.api.UserApi;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.utils.FileUtils;


public class DataManager {

    private static DataManager INSTANCE;

    public static DataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    private DataManager() {

    }

    public void clearDataForUserGroupChange() {
        UserGroupProvider.deleteUserGroups();
        CammentProvider.deleteCamments();
        CammentUploadProvider.deleteCammentUploads();
        ShowProvider.deleteShows();

        FileUtils.getInstance().deleteAllFiles();
    }

}
