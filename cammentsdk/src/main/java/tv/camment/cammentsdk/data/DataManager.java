package tv.camment.cammentsdk.data;

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

    public void clearDataForUserGroupChange() {
        UserGroupProvider.setAllAsNotActive();
        FileUtils.getInstance().deleteAllFiles();

        UserInfoProvider.deleteUserInfos();
    }

    public void clearDataForLogOut() {
        FileUtils.getInstance().deleteAllFiles();

        CammentProvider.deleteCamments();
        UserGroupProvider.deleteUserGroups();
        UserInfoProvider.deleteUserInfos();
    }

}
