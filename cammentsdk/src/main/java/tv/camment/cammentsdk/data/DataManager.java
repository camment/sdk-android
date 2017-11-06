package tv.camment.cammentsdk.data;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.helpers.FacebookHelper;
import tv.camment.cammentsdk.utils.FileUtils;


public final class DataManager {

    private static DataManager INSTANCE;

    public static DataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    private DataManager() {

    }

    public void clearDataForUserGroupChange(boolean deactivateGroups) {
        UserGroupProvider.setAllAsNotActive();
        FileUtils.getInstance().deleteAllFiles();

        UserInfoProvider.deleteUserInfos();
    }

    public void handleFbPermissionsResult() {
        if (FacebookHelper.getInstance().isLoggedIn()
                && FacebookHelper.getInstance().showShareOptions()) {
            ApiManager.getInstance().getGroupApi().createEmptyUsergroupIfNeededAndGetDeeplink();
        }
    }

}
