package tv.camment.cammentsdk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.camment.clientsdk.model.Usergroup;
import com.camment.clientsdk.model.UsergroupListItem;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.data.model.CUserGroup;


public final class UserGroupProvider {

    private static final String[] USER_GROUP_PROJECTION = {
            DataContract.UserGroup._ID,
            DataContract.UserGroup.uuid,
            DataContract.UserGroup.timestamp,
            DataContract.UserGroup.active,
            DataContract.UserGroup.userCognitoIdentityId};

    public static void insertUserGroup(Usergroup usergroup) {
        if (usergroup == null)
            return;

        ContentValues cv = new ContentValues();
        cv.put(DataContract.UserGroup.uuid, usergroup.getUuid());
        cv.put(DataContract.UserGroup.userCognitoIdentityId, usergroup.getUserCognitoIdentityId());
        cv.put(DataContract.UserGroup.timestamp, usergroup.getTimestamp());
        cv.put(DataContract.UserGroup.active, 1);

        CammentSDK.getInstance().getApplicationContext().getContentResolver().insert(DataContract.UserGroup.CONTENT_URI, cv);

        CammentSDK.getInstance().connectToIoT();
    }

    public static void insertUserGroups(List<UsergroupListItem> usergroups) {
        if (usergroups == null || usergroups.size() == 0)
            return;

        final Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        final String activeUserGroupUuid = activeUserGroup != null ? activeUserGroup.getUuid() : "";

        List<ContentValues> values = new ArrayList<>();
        ContentValues cv;

        for (UsergroupListItem usergroup : usergroups) {
            cv = new ContentValues();

            cv.put(DataContract.UserGroup.uuid, usergroup.getGroupUuid());
            cv.put(DataContract.UserGroup.timestamp, usergroup.getTimestamp());
            cv.put(DataContract.UserGroup.active, TextUtils.equals(activeUserGroupUuid, usergroup.getGroupUuid()) ? 1 : 0);

            values.add(cv);
        }

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .bulkInsert(DataContract.UserGroup.CONTENT_URI, values.toArray(new ContentValues[values.size()]));
    }

    static void deleteUserGroups() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.UserGroup.CONTENT_URI, null, null);
    }

    public static Usergroup getActiveUserGroup() {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserGroup.active + "=?";
        String[] selectionArgs = {String.valueOf(1)};

        Cursor cursor = cr.query(DataContract.UserGroup.CONTENT_URI, USER_GROUP_PROJECTION, where, selectionArgs, null);

        Usergroup usergroup = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                usergroup = fromCursor(cursor);
            }

            cursor.close();
        }

        return usergroup;
    }

    public static void setActive(String groupUuid, boolean active) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserGroup.uuid + "=?";
        String[] selectionArgs = {groupUuid};

        ContentValues cv = new ContentValues();

        cv.put(DataContract.UserGroup.active, String.valueOf(active ? 1 : 0));

        cr.update(DataContract.UserGroup.CONTENT_URI, cv, where, selectionArgs);
    }

    private static CUserGroup fromCursor(Cursor cursor) {
        CUserGroup usergroup = new CUserGroup();
        usergroup.setUuid(cursor.getString(cursor.getColumnIndex(DataContract.UserGroup.uuid)));
        usergroup.setUserCognitoIdentityId(cursor.
                getString(cursor.getColumnIndex(DataContract.UserGroup.userCognitoIdentityId)));
        usergroup.setTimestamp(cursor.getString(cursor.getColumnIndex(DataContract.UserGroup.timestamp)));
        usergroup.setActive(cursor.getInt(cursor.getColumnIndex(DataContract.UserGroup.active)) == 1);

        return usergroup;
    }

    public static List<CUserGroup> listFromCursor(Cursor cursor) {
        List<CUserGroup> usergroups = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                CUserGroup usergroup;
                do {
                    usergroup = fromCursor(cursor);
                    usergroups.add(usergroup);
                } while (cursor.moveToNext());
            }
        }
        return usergroups;
    }

    public static Loader<Cursor> getUserGroupLoader() {
        return new CursorLoader(CammentSDK.getInstance().getApplicationContext(), DataContract.UserGroup.CONTENT_URI,
                null, null, null, DataContract.UserGroup.timestamp + " DESC");
    }

}
