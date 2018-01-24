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
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.utils.LogUtils;


public final class UserInfoProvider {

    private static final String[] USER_INFO_PROJECTION = {
            DataContract.UserInfo._ID,
            DataContract.UserInfo.userCognitoIdentityId,
            DataContract.UserInfo.name,
            DataContract.UserInfo.groupUuid,
            DataContract.UserInfo.picture,
            DataContract.UserInfo.state};

    public static void insertUserInfos(List<Userinfo> userinfos, String groupUuid) {
        if (userinfos == null || userinfos.size() == 0)
            return;

        List<ContentValues> values = new ArrayList<>();
        ContentValues cv;

        for (Userinfo userinfo : userinfos) {
            if (TextUtils.equals(userinfo.getUserCognitoIdentityId(), IdentityPreferences.getInstance().getIdentityId())) {
                continue;
            }
            cv = new ContentValues();

            cv.put(DataContract.UserInfo.userCognitoIdentityId, userinfo.getUserCognitoIdentityId());
            cv.put(DataContract.UserInfo.name, userinfo.getName());
            cv.put(DataContract.UserInfo.picture, userinfo.getPicture());
            cv.put(DataContract.UserInfo.groupUuid, groupUuid);
            cv.put(DataContract.UserInfo.state, userinfo.getState());

            values.add(cv);
        }

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .bulkInsert(DataContract.UserInfo.CONTENT_URI, values.toArray(new ContentValues[values.size()]));
    }

    public static void insertUserInfo(Userinfo userinfo, String groupUuid) {
        if (userinfo == null || TextUtils.isEmpty(groupUuid))
            return;

        if (TextUtils.equals(userinfo.getUserCognitoIdentityId(), IdentityPreferences.getInstance().getIdentityId())) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(DataContract.UserInfo.userCognitoIdentityId, userinfo.getUserCognitoIdentityId());
        cv.put(DataContract.UserInfo.name, userinfo.getName());
        cv.put(DataContract.UserInfo.picture, userinfo.getPicture());
        cv.put(DataContract.UserInfo.groupUuid, groupUuid);
        cv.put(DataContract.UserInfo.state, userinfo.getState());

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

        return userInfo;
    }

    public static int getConnectedUsersCountByGroupUuid(String groupUuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserInfo.groupUuid + "=?";
        String[] selectionArgs = {groupUuid};

        Cursor cursor = cr.query(DataContract.UserInfo.CONTENT_URI, USER_INFO_PROJECTION, where, selectionArgs, null);

        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }

    public static Loader<Cursor> getUserInfoLoader(String groupUuid) {
        String where = DataContract.UserInfo.groupUuid + "=?";
        String[] selectionArgs = {groupUuid};

        return new CursorLoader(CammentSDK.getInstance().getApplicationContext(), DataContract.UserInfo.CONTENT_URI,
                null, where, selectionArgs, null);
    }

}
