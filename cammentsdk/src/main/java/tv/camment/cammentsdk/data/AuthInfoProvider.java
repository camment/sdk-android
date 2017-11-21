package tv.camment.cammentsdk.data;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.auth.CammentAuthInfo;
import tv.camment.cammentsdk.auth.CammentAuthType;
import tv.camment.cammentsdk.auth.CammentFbAuthInfo;
import tv.camment.cammentsdk.auth.CammentFbUserInfo;
import tv.camment.cammentsdk.auth.CammentUserInfo;
import tv.camment.cammentsdk.data.model.CAllAuthInfo;
import tv.camment.cammentsdk.utils.DateTimeUtils;

public final class AuthInfoProvider {

    private static final String[] AUTH_INFO_PROJECTION = {
            DataContract.AuthInfo._ID,
            DataContract.AuthInfo.name, DataContract.AuthInfo.uuid,
            DataContract.AuthInfo.email, DataContract.AuthInfo.imageUrl,
            DataContract.AuthInfo.token, DataContract.AuthInfo.expires
    };

    public static void insertAllInfo(CammentUserInfo userInfo, CammentAuthInfo authInfo) {
        if (authInfo == null || userInfo == null) {
            return;
        }

        deleteAllAuthInfos();

        ContentValues cv = new ContentValues();

        if (userInfo.getAuthType() == authInfo.getAuthType()) {
            switch (userInfo.getAuthType()) {
                case FACEBOOK:
                    cv.put(DataContract.AuthInfo.authType, userInfo.getAuthType().getIntValue());
                    cv.put(DataContract.AuthInfo.uuid, ((CammentFbUserInfo) userInfo).getFacebookUserId());
                    cv.put(DataContract.AuthInfo.name, userInfo.getName());
                    //cv.put(DataContract.AuthInfo.email, userInfo.getEmail());
                    cv.put(DataContract.AuthInfo.imageUrl, userInfo.getImageUrl());

                    final CammentFbAuthInfo fbAuthInfo = ((CammentFbAuthInfo) authInfo);
                    cv.put(DataContract.AuthInfo.token, fbAuthInfo.getToken());

                    final Date expires = fbAuthInfo.getExpires();
                    cv.put(DataContract.AuthInfo.expires, DateTimeUtils.getUTCTimestampFromDate(expires));
                    break;
            }
        }

        if (cv.size() > 0) {
            CammentSDK.getInstance().getApplicationContext().getContentResolver().insert(DataContract.AuthInfo.CONTENT_URI, cv);
        }
    }

    public static void insertAuthInfo(CammentAuthInfo authInfo) {
        if (authInfo == null) {
            return;
        }

        switch (authInfo.getAuthType()) {
            case FACEBOOK:
                CAllAuthInfo allAuthInfo = getAuthInfoByUuid(((CammentFbAuthInfo) authInfo).getFacebookUserId());
                if (allAuthInfo != null) {
                    allAuthInfo.setAuthInfo(authInfo);
                    insertAllInfo(allAuthInfo.getUserInfo(), allAuthInfo.getAuthInfo());
                }
                break;
        }
    }

    public static void insertUserInfo(CammentUserInfo userInfo) {
        if (userInfo == null) {
            return;
        }

        switch (userInfo.getAuthType()) {
            case FACEBOOK:
                CAllAuthInfo allAuthInfo = getAuthInfoByUuid(((CammentFbUserInfo) userInfo).getFacebookUserId());
                if (allAuthInfo != null) {
                    allAuthInfo.setUserInfo(userInfo);
                    insertAllInfo(allAuthInfo.getUserInfo(), allAuthInfo.getAuthInfo());
                }
                break;
        }
    }

    private static void deleteAllAuthInfos() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.AuthInfo.CONTENT_URI, null, null);
    }

    public static CAllAuthInfo getAuthInfo() {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        Cursor cursor = cr.query(DataContract.UserGroup.CONTENT_URI, AUTH_INFO_PROJECTION, null, null, null);

        CAllAuthInfo allAuthInfo = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                allAuthInfo = fromCursor(cursor);
            }
        }

        return allAuthInfo;
    }


    public static CAllAuthInfo getAuthInfoByUuid(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.AuthInfo.uuid + "=?";
        String[] selectionArgs = {uuid};

        Cursor cursor = cr.query(DataContract.UserGroup.CONTENT_URI, AUTH_INFO_PROJECTION, where, selectionArgs, null);

        CAllAuthInfo allAuthInfo = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                allAuthInfo = fromCursor(cursor);
            }
        }

        return allAuthInfo;
    }

    private static CAllAuthInfo fromCursor(Cursor cursor) {
        final CammentAuthType authType = CammentAuthType.fromInt(cursor.getInt(cursor.getColumnIndex(DataContract.AuthInfo.authType)));

        switch (authType) {
            case FACEBOOK:
                String token = cursor.getString(cursor.getColumnIndex(DataContract.AuthInfo.token));
                Date expires = DateTimeUtils.getUTCDateFromTimestamp(cursor.getLong(cursor.getColumnIndex(DataContract.AuthInfo.expires)));
                String facebookId = cursor.getString(cursor.getColumnIndex(DataContract.AuthInfo.uuid));

                CammentAuthInfo authInfo = new CammentFbAuthInfo(facebookId, token, expires);

                String name = cursor.getString(cursor.getColumnIndex(DataContract.AuthInfo.name));
                String imageUrl = cursor.getString(cursor.getColumnIndex(DataContract.AuthInfo.imageUrl));
                String email = cursor.getString(cursor.getColumnIndex(DataContract.AuthInfo.email));

                CammentUserInfo userInfo = new CammentFbUserInfo(name, imageUrl, facebookId);

                return new CAllAuthInfo(authType, authInfo, userInfo);
        }

        return null;
    }

}
