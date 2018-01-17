package tv.camment.cammentsdk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;

import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.Usergroup;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.data.model.ChatItem;
import tv.camment.cammentsdk.helpers.IdentityPreferences;


public final class CammentProvider {

    private static final String[] CAMMENT_PROJECTION = {
            DataContract.Camment._ID,
            DataContract.Camment.uuid,
            DataContract.Camment.url,
            DataContract.Camment.showUuid,
            DataContract.Camment.thumbnail,
            DataContract.Camment.userCognitoIdentityId,
            DataContract.Camment.userGroupUuid,
            DataContract.Camment.transferId,
            DataContract.Camment.recorded,
            DataContract.Camment.deleted,
            DataContract.Camment.seen,
            DataContract.Camment.sent,
            DataContract.Camment.received,
            DataContract.Camment.timestamp,
            DataContract.Camment.startTimestamp,
            DataContract.Camment.endTimestamp};

    public static void insertCamment(CCamment camment) {
        if (camment == null)
            return;

        final CCamment cammentByUuid = getCammentByUuid(camment.getUuid());

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.uuid, camment.getUuid());
        cv.put(DataContract.Camment.url, camment.getUrl());
        cv.put(DataContract.Camment.showUuid, camment.getShowUuid());
        cv.put(DataContract.Camment.thumbnail, camment.getThumbnail());
        cv.put(DataContract.Camment.userCognitoIdentityId, camment.getUserCognitoIdentityId());
        cv.put(DataContract.Camment.userGroupUuid, camment.getUserGroupUuid());

        long timestamp = camment.getTimestamp() == 0
                ? System.currentTimeMillis()
                : cammentByUuid == null ? camment.getTimestamp() : cammentByUuid.getTimestamp();

        cv.put(DataContract.Camment.timestamp, timestamp);
        cv.put(DataContract.Camment.transferId, camment.getTransferId());
        cv.put(DataContract.Camment.recorded, camment.isRecorded() ? 1 : 0);

        int deleted = cammentByUuid == null ? (camment.isDeleted() ? 1 : 0) : (cammentByUuid.isDeleted() ? 1 : 0);
        cv.put(DataContract.Camment.deleted, deleted);

        int seen = cammentByUuid == null ? (camment.isSeen() ? 1 : 0) : (cammentByUuid.isSeen() ? 1 : 0);
        cv.put(DataContract.Camment.seen, seen);

        int sent = cammentByUuid == null ? (camment.isSent() ? 1 : 0) : (cammentByUuid.isSent() ? 1 : 0);
        cv.put(DataContract.Camment.sent, sent);

        String identityId = IdentityPreferences.getInstance().getIdentityId();
        int received = 0;
        if (TextUtils.equals(identityId, camment.getUserCognitoIdentityId())
                && camment.getDelivered() != null) {
            received = camment.getDelivered() ? 1 : 0;
        }
        if (cammentByUuid != null && cammentByUuid.getDelivered() != null && cammentByUuid.getDelivered()) {
            received = 1;
        }
        cv.put(DataContract.Camment.received, received);

        long startTimestamp = cammentByUuid == null ? camment.getStartTimestamp() : cammentByUuid.getStartTimestamp();
        cv.put(DataContract.Camment.startTimestamp, startTimestamp);

        long endTimestamp = cammentByUuid == null ? camment.getEndTimestamp() : cammentByUuid.getEndTimestamp();
        cv.put(DataContract.Camment.endTimestamp, endTimestamp);

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .insert(DataContract.Camment.CONTENT_URI, cv);
    }

    public static void insertCamments(List<Camment> camments) {
        if (camments == null || camments.size() == 0)
            return;

        List<ContentValues> values = new ArrayList<>();
        ContentValues cv;

        int i = camments.size() + 1;
        for (Camment camment : camments) {
            cv = new ContentValues();

            cv.put(DataContract.Camment.uuid, camment.getUuid());
            cv.put(DataContract.Camment.url, camment.getUrl());
            cv.put(DataContract.Camment.showUuid, camment.getShowUuid());
            cv.put(DataContract.Camment.thumbnail, camment.getThumbnail());
            cv.put(DataContract.Camment.userCognitoIdentityId, camment.getUserCognitoIdentityId());
            cv.put(DataContract.Camment.userGroupUuid, camment.getUserGroupUuid());
            cv.put(DataContract.Camment.timestamp, i);
            cv.put(DataContract.Camment.transferId, -1);
            cv.put(DataContract.Camment.recorded, 1);

            final CCamment cammentByUuid = getCammentByUuid(camment.getUuid());
            int deleted = cammentByUuid == null ? 0 : (cammentByUuid.isDeleted() ? 1 : 0);
            cv.put(DataContract.Camment.deleted, deleted);

            int seen = cammentByUuid == null ? 0 : (cammentByUuid.isSeen() ? 1 : 0);

            String identityId = IdentityPreferences.getInstance().getIdentityId();
            if (TextUtils.equals(identityId, camment.getUserCognitoIdentityId())) {
                seen = 1;
            }
            cv.put(DataContract.Camment.seen, seen);

            cv.put(DataContract.Camment.sent, 1);

            int received = 0;
            if (TextUtils.equals(identityId, camment.getUserCognitoIdentityId())
                    && camment.getDelivered() != null) {
                received = camment.getDelivered() ? 1 : 0;
            }
            cv.put(DataContract.Camment.received, received);

            values.add(cv);
            i--;
        }

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .bulkInsert(DataContract.Camment.CONTENT_URI, values.toArray(new ContentValues[values.size()]));
    }

    static void deleteCamments() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.Camment.CONTENT_URI, null, null);
    }

    public static void deleteCammentByUuid(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {uuid};

        int delete = cr.delete(DataContract.Camment.CONTENT_URI, where, selectionArgs);
        Log.d("deleteCammentByUuid", uuid + " - " + (delete > 0));
    }

    public static void setCammentDeleted(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {uuid};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.deleted, true);

        int update = cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
        Log.d("setCammentDeleted", uuid + " - " + (update > 0));
    }

    public static void setCammentSeen(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {uuid};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.seen, true);

        int update = cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
        Log.d("setCammentSeen", uuid + " - " + (update > 0));
    }

    public static void setCammentSent(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {uuid};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.sent, true);

        int update = cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
        Log.d("setCammentSent", uuid + " - " + (update > 0));
    }

    public static void setCammentReceived(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {uuid};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.received, true);

        int update = cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
        Log.d("setCammentReceived", uuid + " - " + (update > 0));
    }

    public static CCamment getCammentByUuid(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {uuid};

        Cursor cursor = cr.query(DataContract.Camment.CONTENT_URI, CAMMENT_PROJECTION, where, selectionArgs, null);

        CCamment camment = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                camment = fromCursor(cursor);
            }

            cursor.close();
        }

        return camment;
    }

    public static CCamment getCammentByTransferId(int id) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.transferId + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = cr.query(DataContract.Camment.CONTENT_URI, CAMMENT_PROJECTION, where, selectionArgs, null);

        CCamment camment = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                camment = fromCursor(cursor);
            }

            cursor.close();
        }

        return camment;
    }

    public static void setCammentUploadTransferId(CCamment camment, int id) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {camment.getUuid()};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.transferId, id);

        int update = cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
        Log.d("setCUploadTransferId", camment.getUuid() + " - " + (update > 0));
    }

    public static void setRecorded(CCamment camment, boolean recorded) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {camment.getUuid()};

        ContentValues cv = new ContentValues();

        cv.put(DataContract.Camment.recorded, String.valueOf(recorded ? 1 : 0));
        cv.put(DataContract.Camment.timestamp, System.currentTimeMillis());

        int update = cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
        Log.d("setRecorded", camment.getUuid() + " - " + (update > 0));
    }

    public static void updateCammentGroupId(CCamment camment, String groupUuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {camment.getUuid()};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.userGroupUuid, groupUuid);

        int update = cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
        Log.d("updateCammentGroupId", camment.getUuid() + " - " + (update > 0));
    }

    public static void setStartTimestamp(String cammentUuid, long startTimestamp) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {cammentUuid};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.startTimestamp, startTimestamp);

        int update = cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
        Log.d("setStartTimestamp", cammentUuid + " t: " + startTimestamp + " - " + (update > 0));
    }

    public static void setEndTimestamp(String cammentUuid, long endTimestamp) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {cammentUuid};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.endTimestamp, endTimestamp);

        int update = cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
        Log.d("setEndTimestamp", cammentUuid + " t: " + endTimestamp + " - " + (update > 0));
    }

    public static int getCammentsSize() {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup == null
                || TextUtils.isEmpty(activeUserGroup.getUuid())) {
            return 0;
        }

        String selection = DataContract.Camment.recorded + "=? AND " + DataContract.Camment.userGroupUuid + "=?";
        String[] selectionArgs = new String[]{"1", activeUserGroup.getUuid()};

        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();
        Cursor cursor = cr.query(DataContract.Camment.CONTENT_URI, CAMMENT_PROJECTION, selection, selectionArgs, null);

        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }

    private static CCamment fromCursor(Cursor cursor) {
        CCamment camment = new CCamment();
        camment.setUuid(cursor.getString(cursor.getColumnIndex(DataContract.Camment.uuid)));
        camment.setUrl(cursor.getString(cursor.getColumnIndex(DataContract.Camment.url)));
        camment.setThumbnail(cursor.getString(cursor.getColumnIndex(DataContract.Camment.thumbnail)));
        camment.setUserCognitoIdentityId(cursor.getString(cursor.getColumnIndex(DataContract.Camment.userCognitoIdentityId)));
        camment.setUserGroupUuid(cursor.getString(cursor.getColumnIndex(DataContract.Camment.userGroupUuid)));
        camment.setShowUuid(cursor.getString(cursor.getColumnIndex(DataContract.Camment.showUuid)));
        camment.setTimestamp(cursor.getLong(cursor.getColumnIndex(DataContract.Camment.timestamp)));
        camment.setTransferId(cursor.getInt(cursor.getColumnIndex(DataContract.Camment.transferId)));
        camment.setRecorded(cursor.getInt(cursor.getColumnIndex(DataContract.Camment.recorded)) == 1);
        camment.setDeleted(cursor.getInt(cursor.getColumnIndex(DataContract.Camment.deleted)) == 1);
        camment.setSeen(cursor.getInt(cursor.getColumnIndex(DataContract.Camment.seen)) == 1);
        camment.setSent(cursor.getInt(cursor.getColumnIndex(DataContract.Camment.sent)) == 1);
        camment.setDelivered(cursor.getInt(cursor.getColumnIndex(DataContract.Camment.received)) == 1);
        camment.setStartTimestamp(cursor.getLong(cursor.getColumnIndex(DataContract.Camment.startTimestamp)));
        camment.setEndTimestamp(cursor.getLong(cursor.getColumnIndex(DataContract.Camment.endTimestamp)));

        return camment;
    }

    public static List<ChatItem<CCamment>> listFromCursor(Cursor cursor) {
        List<ChatItem<CCamment>> camments = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                CCamment camment;
                do {
                    camment = fromCursor(cursor);
                    camments.add(new ChatItem<>(ChatItem.ChatItemType.CAMMENT, camment.getUuid(), camment.getTimestamp(), camment));
                } while (cursor.moveToNext());
            }
        }
        return camments;
    }

    public static Loader<Cursor> getCammentLoader() {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

        if (activeUserGroup != null
                && !TextUtils.isEmpty(activeUserGroup.getUuid())) {
            String selection = DataContract.Camment.recorded + "=? AND "
                    + DataContract.Camment.userGroupUuid + "=? AND "
                    + DataContract.Camment.deleted + "=?";
            String[] selectionArgs = new String[]{"1", activeUserGroup.getUuid(), "0"};

            return new CursorLoader(CammentSDK.getInstance().getApplicationContext(), DataContract.Camment.CONTENT_URI,
                    null, selection, selectionArgs, DataContract.Camment.timestamp + " DESC");
        } else {
            return null;
        }
    }

}
