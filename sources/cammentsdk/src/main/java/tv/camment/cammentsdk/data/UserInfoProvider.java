package tv.camment.cammentsdk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.camment.clientsdk.model.Userinfo;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.data.model.UserState;
import tv.camment.cammentsdk.utils.LogUtils;


public final class UserInfoProvider {

    public static void insertUserInfos(List<Userinfo> userinfos, String groupUuid) {
        if (userinfos == null || userinfos.size() == 0)
            return;

        List<ContentValues> values = new ArrayList<>();
        ContentValues cv;

        for (Userinfo userinfo : userinfos) {
            cv = new ContentValues();

            cv.put(DataContract.UserInfo.userCognitoIdentityId, userinfo.getUserCognitoIdentityId());
            cv.put(DataContract.UserInfo.name, userinfo.getName());
            cv.put(DataContract.UserInfo.picture, userinfo.getPicture());
            cv.put(DataContract.UserInfo.groupUuid, groupUuid);
            cv.put(DataContract.UserInfo.state, TextUtils.isEmpty(userinfo.getState()) ? UserState.ACTIVE.getStringValue() : userinfo.getState());
            cv.put(DataContract.UserInfo.isOnline, userinfo.getIsOnline() != null && userinfo.getIsOnline() ? 1 : 0);
            cv.put(DataContract.UserInfo.activeGroup, userinfo.getActiveGroup());

            values.add(cv);
        }

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .bulkInsert(DataContract.UserInfo.CONTENT_URI, values.toArray(new ContentValues[values.size()]));
    }

    public static void insertUserInfo(Userinfo userinfo, String groupUuid) {
        if (userinfo == null || TextUtils.isEmpty(groupUuid))
            return;

        ContentValues cv = new ContentValues();
        cv.put(DataContract.UserInfo.userCognitoIdentityId, userinfo.getUserCognitoIdentityId());
        cv.put(DataContract.UserInfo.name, userinfo.getName());
        cv.put(DataContract.UserInfo.picture, userinfo.getPicture());
        cv.put(DataContract.UserInfo.groupUuid, groupUuid);
        cv.put(DataContract.UserInfo.state, TextUtils.isEmpty(userinfo.getState()) ? UserState.ACTIVE.getStringValue() : userinfo.getState());
        cv.put(DataContract.UserInfo.isOnline, userinfo.getIsOnline() != null && userinfo.getIsOnline() ? 1 : 0);
        cv.put(DataContract.UserInfo.activeGroup, userinfo.getActiveGroup());

        CammentSDK.getInstance().getApplicationContext().getContentResolver().insert(DataContract.UserInfo.CONTENT_URI, cv);
    }

    static void deleteUserInfos() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.UserInfo.CONTENT_URI, null, null);
    }

    public static void deleteUserInfoByIdentityId(String identityId, String groupUuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserInfo.userCognitoIdentityId + "=? AND " + DataContract.UserInfo.groupUuid + "=?";
        String[] selectionArgs = {identityId, groupUuid};

        int delete = cr.delete(DataContract.UserInfo.CONTENT_URI, where, selectionArgs);
        LogUtils.debug("deleteUserInfoById", identityId + " - " + (delete > 0));
    }

    public static void setUserInGroupAsBlocked(String userUuid, String groupUuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserInfo.userCognitoIdentityId + "=? AND " + DataContract.UserInfo.groupUuid + "=?";
        String[] selectionArgs = {userUuid, groupUuid};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.UserInfo.state, UserState.BLOCKED.getStringValue());

        int update = cr.update(DataContract.UserInfo.CONTENT_URI, cv, where, selectionArgs);
        LogUtils.debug("DATABASE", "setUserInGroupAsBlocked: " + userUuid + " - " + (update > 0));
    }

    public static void setUserInGroupAsUnblocked(String userUuid, String groupUuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserInfo.userCognitoIdentityId + "=? AND " + DataContract.UserInfo.groupUuid + "=?";
        String[] selectionArgs = {userUuid, groupUuid};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.UserInfo.state, UserState.ACTIVE.getStringValue());

        int update = cr.update(DataContract.UserInfo.CONTENT_URI, cv, where, selectionArgs);
        LogUtils.debug("DATABASE", "setUserInGroupAsUnblocked: " + userUuid + " - " + (update > 0));
    }

    public static boolean changeUserOnlineOfflineStatus(String userId, boolean isOnline) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserInfo.userCognitoIdentityId + "=? AND "
                + DataContract.UserInfo.isOnline + "=?";
        String[] selectionArgs = {userId, isOnline ? "0" : "1"};

        ContentValues cv = new ContentValues();

        cv.put(DataContract.UserInfo.isOnline, String.valueOf(isOnline ? 1 : 0));

        int update = cr.update(DataContract.UserInfo.CONTENT_URI, cv, where, selectionArgs);
        LogUtils.debug("DATABASE", "changeUserOnlineOfflineStatus: " + userId + " - " + (update > 0));

        return update > 0;
    }

    public static CUserInfo getUserInfoForGroup(String userId, String groupId) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserInfo.userCognitoIdentityId + "=? AND " + DataContract.UserInfo.groupUuid + "=?";
        String[] selectionArgs = {userId, groupId};

        Cursor cursor = cr.query(DataContract.UserInfo.CONTENT_URI, null, where, selectionArgs, null);

        CUserInfo userInfo = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                userInfo = fromCursor(cursor);
            }

            cursor.close();
        }

        return userInfo;
    }

    public static List<CUserInfo> listFromCursor(Cursor cursor) {
        List<CUserInfo> userInfos = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                CUserInfo userInfo;
                do {
                    userInfo = fromCursor(cursor);
                    userInfos.add(userInfo);
                } while (cursor.moveToNext());
            }
        }
        return userInfos;
    }

    private static CUserInfo fromCursor(Cursor cursor) {
        CUserInfo userInfo = new CUserInfo();
        userInfo.setUserCognitoIdentityId(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.userCognitoIdentityId)));
        userInfo.setName(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.name)));
        userInfo.setPicture(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.picture)));
        userInfo.setGroupUuid(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.groupUuid)));
        userInfo.setState(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.state)));
        userInfo.setIsOnline(cursor.getInt(cursor.getColumnIndex(DataContract.UserInfo.isOnline)) == 1);
        userInfo.setActiveGroup(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.activeGroup)));

        return userInfo;
    }

    public static Loader<Cursor> getUserInfoLoader(String groupUuid) {
        String where = DataContract.UserInfo.groupUuid + "=?";
        String[] selectionArgs = {groupUuid};

        return new CursorLoader(CammentSDK.getInstance().getApplicationContext(), DataContract.UserInfo.CONTENT_URI,
                null, where, selectionArgs, null);
    }

}
