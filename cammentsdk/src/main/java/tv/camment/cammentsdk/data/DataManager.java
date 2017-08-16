package tv.camment.cammentsdk.data;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.utils.FileUtils;
import tv.camment.cammentsdk.views.FbFriendsBottomSheetDialog;


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
        ShowProvider.deleteShows();

        FileUtils.getInstance().deleteAllFiles();
    }

    public void handleFbPermissionsResult() {
        if (FacebookHelper.getInstance().isLoggedIn()) {
            new FbFriendsBottomSheetDialog(CammentSDK.getInstance().getCurrentActivity()).show();
        }
    }

}
