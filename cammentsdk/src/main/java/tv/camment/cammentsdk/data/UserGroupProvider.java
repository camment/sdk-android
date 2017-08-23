package tv.camment.cammentsdk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.camment.clientsdk.model.Usergroup;

import tv.camment.cammentsdk.CammentSDK;


public final class UserGroupProvider {

    private static final String[] USER_GROUP_PROJECTION = {
            DataContract.UserGroup._ID,
            DataContract.UserGroup.uuid,
            DataContract.UserGroup.timestamp,
            DataContract.UserGroup.userCognitoIdentityId};

    public static void insertUserGroup(Usergroup usergroup) {
        if (usergroup == null)
            return;

        deleteUserGroups();

        ContentValues cv = new ContentValues();
        cv.put(DataContract.UserGroup.uuid, usergroup.getUuid());
        cv.put(DataContract.UserGroup.userCognitoIdentityId, usergroup.getUserCognitoIdentityId());
        cv.put(DataContract.UserGroup.timestamp, usergroup.getTimestamp());

        CammentSDK.getInstance().getApplicationContext().getContentResolver().insert(DataContract.UserGroup.CONTENT_URI, cv);
    }

    static void deleteUserGroups() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.UserGroup.CONTENT_URI, null, null);
    }

    public static Usergroup getUserGroup() {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        Cursor cursor = cr.query(DataContract.UserGroup.CONTENT_URI, USER_GROUP_PROJECTION, null, null, null);

        Usergroup usergroup = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                usergroup = fromCursor(cursor);
            }

            cursor.close();
        }

        return usergroup;
    }

    private static Usergroup fromCursor(Cursor cursor) {
        Usergroup usergroup = new Usergroup();
        usergroup.setUuid(cursor.getString(cursor.getColumnIndex(DataContract.UserGroup.uuid)));
        usergroup.setUserCognitoIdentityId(cursor.
                getString(cursor.getColumnIndex(DataContract.UserGroup.userCognitoIdentityId)));
        usergroup.setTimestamp(cursor.getString(cursor.getColumnIndex(DataContract.UserGroup.timestamp)));

        return usergroup;
    }

}
