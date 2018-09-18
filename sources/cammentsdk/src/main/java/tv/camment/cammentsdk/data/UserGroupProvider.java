package tv.camment.cammentsdk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.camment.clientsdk.model.Usergroup;
import com.camment.clientsdk.model.Userinfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.auth.CammentUserInfo;
import tv.camment.cammentsdk.data.model.CUserGroup;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.utils.DateTimeUtils;


public final class UserGroupProvider {

    public static void insertUserGroup(Usergroup usergroup, boolean changeGroup, String showUuid) {
        if (usergroup == null)
            return;

        Usergroup activeUserGroup = getActiveUserGroup();
        if (activeUserGroup != null && changeGroup) {
            CammentProvider.deleteCammentsByGroupUuid(activeUserGroup.getUuid());
            setActive(activeUserGroup.getUuid(), false);
        }

        ContentValues cv = new ContentValues();
        cv.put(DataContract.UserGroup.uuid, usergroup.getUuid());
        cv.put(DataContract.UserGroup.userCognitoIdentityId, usergroup.getUserCognitoIdentityId());
        cv.put(DataContract.UserGroup.timestamp, DateTimeUtils.getTimestampFromIsoDateString(usergroup.getTimestamp()));

        CUserGroup userGroupByUuid = getUserGroupByUuid(usergroup.getUuid());
        int active = userGroupByUuid != null && userGroupByUuid.isActive() ? 1 : 0;
        cv.put(DataContract.UserGroup.active, changeGroup ? 1 : active);

        cv.put(DataContract.UserGroup.hostId, usergroup.getHostId());
        cv.put(DataContract.UserGroup.showUuid, showUuid);
        cv.put(DataContract.UserGroup.isPublic, usergroup.getIsPublic() != null && usergroup.getIsPublic() ? 1 : 0);

        CammentSDK.getInstance().getApplicationContext().getContentResolver().insert(DataContract.UserGroup.CONTENT_URI, cv);

        UserInfoProvider.insertUserInfos(usergroup.getUsers(), usergroup.getUuid());

        if (changeGroup) {
            EventBus.getDefault().post(new UserGroupChangeEvent());
        }
    }

    public static void insertUserGroups(List<Usergroup> usergroups, String showUuid) {
        if (usergroups == null || usergroups.size() == 0)
            return;

        final Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        final String activeUserGroupUuid = activeUserGroup != null ? activeUserGroup.getUuid() : "";

        List<ContentValues> values = new ArrayList<>();
        ContentValues cv;

        for (Usergroup usergroup : usergroups) {
            cv = new ContentValues();

            if (usergroup.getIsPublic() && TextUtils.isEmpty(usergroup.getHostId())) {
                continue;
            }

            cv.put(DataContract.UserGroup.uuid, usergroup.getUuid());
            cv.put(DataContract.UserGroup.userCognitoIdentityId, usergroup.getUserCognitoIdentityId());
            cv.put(DataContract.UserGroup.timestamp, DateTimeUtils.getTimestampFromIsoDateString(usergroup.getTimestamp()));
            cv.put(DataContract.UserGroup.active, TextUtils.equals(activeUserGroupUuid, usergroup.getUuid()) ? 1 : 0);
            cv.put(DataContract.UserGroup.hostId, usergroup.getHostId());
            cv.put(DataContract.UserGroup.showUuid, showUuid);
            cv.put(DataContract.UserGroup.isPublic, usergroup.getIsPublic() != null && usergroup.getIsPublic() ? 1 : 0);

            values.add(cv);

            UserInfoProvider.insertUserInfos(usergroup.getUsers(), usergroup.getUuid());
        }

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .bulkInsert(DataContract.UserGroup.CONTENT_URI, values.toArray(new ContentValues[values.size()]));
    }

    static void deleteUserGroups() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.UserGroup.CONTENT_URI, null, null);
    }

    static void deleteUserGroupsByShowUuid(String showUuid) {
        String where = DataContract.UserGroup.showUuid + "=?";
        String[] selectionArgs = {showUuid};

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.UserGroup.CONTENT_URI, where, selectionArgs);
    }

    public static void deleteUserGroupByUuid(String groupUuid) {
        String where = DataContract.UserGroup.uuid + "=?";
        String[] selectionArgs = {groupUuid};

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.UserGroup.CONTENT_URI, where, selectionArgs);
    }

    private static CUserGroup getUserGroupByUuid(String groupUuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserGroup.uuid + "=?";
        String[] selectionArgs = {groupUuid};

        Cursor cursor = cr.query(DataContract.UserGroup.CONTENT_URI, null, where, selectionArgs, null);

        CUserGroup usergroup = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                usergroup = fromCursor(cursor);
            }

            cursor.close();
        }

        return usergroup;
    }

    public static Usergroup getActiveUserGroup() {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserGroup.active + "=?";
        String[] selectionArgs = {String.valueOf(1)};

        Cursor cursor = cr.query(DataContract.UserGroup.CONTENT_URI, null, where, selectionArgs, null);

        Usergroup usergroup = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                usergroup = fromCursor(cursor);
            }

            cursor.close();
        }

        return usergroup;
    }

    public static CUserGroup getActiveUserGroupWithUserInfo() {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserGroup.active + "=?";
        String[] selectionArgs = {String.valueOf(1)};

        Cursor cursor = cr.query(DataContract.UserGroupInfo.CONTENT_URI, null, where, selectionArgs, null);

        listFromCursorWithInfo(cursor);

        List<CUserGroup> userGroups = listFromCursorWithInfo(cursor);

        CUserGroup usergroup = null;

        if (userGroups != null && userGroups.size() > 0) {
            int index = 0;
            int removed = 0;
            for (Iterator<CUserGroup> iterator = userGroups.iterator(); iterator.hasNext(); ) {
                CUserGroup userGroup = iterator.next();

                if (index > 0) {
                    CUserGroup prevGroup = userGroups.get(index - 1 - removed);

                    if (TextUtils.equals(userGroup.getUuid(), prevGroup.getUuid())) {
                        List<Userinfo> prevGroupUsers = new ArrayList<>(prevGroup.getUsers());
                        if (userGroup.getUsers() != null) {
                            prevGroupUsers.addAll(userGroup.getUsers());
                        }
                        prevGroup.setUsers(prevGroupUsers);
                        iterator.remove();
                        removed++;
                    }
                }

                index++;
            }

            if (userGroups.size() == 1) {
                usergroup = userGroups.get(0);
            }
        }
        return usergroup;
    }

    public static void setAllAsNotActive() {
        DbHelper dbHelper = new DbHelper(CammentSDK.getInstance().getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db != null
                && db.isOpen()) {
            db.execSQL("UPDATE " + DataContract.Tables.USER_GROUP + " SET " + DataContract.UserGroup.active + " = 0");

            db.close();
        }
    }

    public static void setActive(String groupUuid, boolean active) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserGroup.uuid + "=?";
        String[] selectionArgs = {groupUuid};

        ContentValues cv = new ContentValues();

        cv.put(DataContract.UserGroup.active, String.valueOf(active ? 1 : 0));

        cr.update(DataContract.UserGroup.CONTENT_URI, cv, where, selectionArgs);
    }

    public static void setHostId(String groupUuid, String hostId) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.UserGroup.uuid + "=?";
        String[] selectionArgs = {groupUuid};

        ContentValues cv = new ContentValues();

        cv.put(DataContract.UserGroup.hostId, hostId);

        cr.update(DataContract.UserGroup.CONTENT_URI, cv, where, selectionArgs);
    }

    private static CUserGroup fromCursorWithInfo(Cursor cursor) {
        CUserGroup usergroup = new CUserGroup();
        usergroup.setUuid(cursor.getString(cursor.getColumnIndex(DataContract.UserGroup.uuid)));
        usergroup.setUserCognitoIdentityId(cursor.
                getString(cursor.getColumnIndex(DataContract.UserGroup.userCognitoIdentityId)));
        usergroup.setLongTimestamp(cursor.getLong(cursor.getColumnIndex(DataContract.UserGroup.timestamp)));
        usergroup.setActive(cursor.getInt(cursor.getColumnIndex(DataContract.UserGroup.active)) == 1);
        usergroup.setHostId(cursor.getString(cursor.getColumnIndex(DataContract.UserGroup.hostId)));
        usergroup.setShowId(cursor.getString(cursor.getColumnIndex(DataContract.UserGroup.showUuid)));
        usergroup.setIsPublic(cursor.getInt(cursor.getColumnIndex(DataContract.UserGroup.isPublic)) == 1);

        CUserInfo userInfo = new CUserInfo();
        userInfo.setUserCognitoIdentityId(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.userCognitoIdentityId)));
        userInfo.setName(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.name)));
        userInfo.setPicture(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.picture)));
        userInfo.setGroupUuid(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.groupUuid)));
        userInfo.setState(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.state)));
        userInfo.setIsOnline(cursor.getInt(cursor.getColumnIndex(DataContract.UserInfo.isOnline)) == 1);
        userInfo.setActiveGroup(cursor.getString(cursor.getColumnIndex(DataContract.UserInfo.activeGroup)));

        if (AuthHelper.getInstance().isLoggedIn()
                && TextUtils.isEmpty(userInfo.getName())) {
            String identityId = IdentityPreferences.getInstance().getIdentityId();
            String oldIdentityId = IdentityPreferences.getInstance().getOldIdentityId();
            if (TextUtils.equals(identityId, userInfo.getUserCognitoIdentityId())
                    || TextUtils.equals(oldIdentityId, userInfo.getUserCognitoIdentityId())) {
                userInfo.setUserCognitoIdentityId(identityId);

                CammentUserInfo cammentUserInfo = CammentSDK.getInstance().getAppAuthIdentityProvider().getUserInfo();
                if (cammentUserInfo != null) {
                    userInfo.setName(cammentUserInfo.getName());
                    userInfo.setPicture(cammentUserInfo.getImageUrl());
                }
            }
        }

        usergroup.setUsers(Collections.<Userinfo>singletonList(userInfo));

        return usergroup;
    }

    public static List<CUserGroup> listFromCursorWithInfo(Cursor cursor) {
        List<CUserGroup> usergroups = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                CUserGroup usergroup;
                do {
                    usergroup = fromCursorWithInfo(cursor);
                    if (usergroup != null) {
                        usergroups.add(usergroup);
                    }
                } while (cursor.moveToNext());
            }
        }
        return usergroups;
    }

    public static Loader<Cursor> getUserGroupLoaderByShowUuid(String showUuid) {
        String where = DataContract.UserGroup.showUuid + "=?";
        String[] selectionArgs = {showUuid};

        return new CursorLoader(CammentSDK.getInstance().getApplicationContext(), DataContract.UserGroupInfo.CONTENT_URI,
                null, where, selectionArgs,
                DataContract.UserGroup.timestamp + " DESC");
    }

    private static CUserGroup fromCursor(Cursor cursor) {
        CUserGroup usergroup = new CUserGroup();
        usergroup.setUuid(cursor.getString(cursor.getColumnIndex(DataContract.UserGroup.uuid)));
        usergroup.setUserCognitoIdentityId(cursor.
                getString(cursor.getColumnIndex(DataContract.UserGroup.userCognitoIdentityId)));
        usergroup.setLongTimestamp(cursor.getLong(cursor.getColumnIndex(DataContract.UserGroup.timestamp)));
        usergroup.setActive(cursor.getInt(cursor.getColumnIndex(DataContract.UserGroup.active)) == 1);
        usergroup.setHostId(cursor.getString(cursor.getColumnIndex(DataContract.UserGroup.hostId)));
        usergroup.setShowId(cursor.getString(cursor.getColumnIndex(DataContract.UserGroup.showUuid)));
        usergroup.setIsPublic(cursor.getInt(cursor.getColumnIndex(DataContract.UserGroup.isPublic)) == 1);

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

    public static CUserGroup oneFromCursor(Cursor cursor) {
        CUserGroup usergroup = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                usergroup = fromCursor(cursor);
            }
        }
        return usergroup;
    }

    public static Loader<Cursor> getActiveUserGroupLoader() {
        String where = DataContract.UserGroup.active + "=?";
        String[] selectionArgs = {"1"};

        return new CursorLoader(CammentSDK.getInstance().getApplicationContext(), DataContract.UserGroup.CONTENT_URI,
                null, where, selectionArgs, DataContract.UserGroup.timestamp + " DESC");
    }

}