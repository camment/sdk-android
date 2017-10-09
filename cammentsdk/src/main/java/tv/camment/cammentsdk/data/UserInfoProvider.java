package tv.camment.cammentsdk.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.camment.clientsdk.model.Userinfo;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.data.model.CUserInfo;


public final class UserInfoProvider {

    private static final String[] USER_INFO_PROJECTION = {
            DataContract.UserInfo._ID,
            DataContract.UserInfo.userCognitoIdentityId,
            DataContract.UserInfo.name,
            DataContract.UserInfo.groupUuid,
            DataContract.UserInfo.picture};

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

            values.add(cv);
        }

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .bulkInsert(DataContract.UserInfo.CONTENT_URI, values.toArray(new ContentValues[values.size()]));
    }

    static void deleteUserInfos() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.UserInfo.CONTENT_URI, null, null);
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

        return userInfo;
    }

    public static Loader<Cursor> getUserInfoLoader() {
        return new CursorLoader(CammentSDK.getInstance().getApplicationContext(), DataContract.UserInfo.CONTENT_URI,
                null, null, null, null);
    }

}
