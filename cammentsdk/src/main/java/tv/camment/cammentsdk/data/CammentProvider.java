package tv.camment.cammentsdk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.camment.clientsdk.model.Camment;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.data.model.CCamment;

/**
 * Created by petrushka on 11/08/2017.
 */

public class CammentProvider {

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
            DataContract.Camment.timestamp};

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

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .insert(DataContract.Camment.CONTENT_URI, cv);
    }

    public static void insertCamments(List<Camment> camments) {
        if (camments == null || camments.size() == 0)
            return;

        List<ContentValues> values = new ArrayList<>();
        ContentValues cv;

        for (Camment camment : camments) {
            cv = new ContentValues();

            cv.put(DataContract.Camment.uuid, camment.getUuid());
            cv.put(DataContract.Camment.url, camment.getUrl());
            cv.put(DataContract.Camment.showUuid, camment.getShowUuid());
            cv.put(DataContract.Camment.thumbnail, camment.getThumbnail());
            cv.put(DataContract.Camment.userCognitoIdentityId, camment.getUserCognitoIdentityId());
            cv.put(DataContract.Camment.userGroupUuid, camment.getUserGroupUuid());
            cv.put(DataContract.Camment.timestamp, System.currentTimeMillis());
            cv.put(DataContract.Camment.transferId, -1);
            cv.put(DataContract.Camment.recorded, 1);

            values.add(cv);
        }

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .bulkInsert(DataContract.Camment.CONTENT_URI, values.toArray(new ContentValues[values.size()]));
    }

    public static void deleteCamments() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.Camment.CONTENT_URI, null, null);
    }

    public static void deleteCammentByUuid(String uuid) {
        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {uuid};

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.Camment.CONTENT_URI, where, selectionArgs);
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

        cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
    }

    public static void setRecorded(CCamment camment, boolean recorded) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {camment.getUuid()};

        ContentValues cv = new ContentValues();

        int rec = recorded ? 1 : 0;

        cv.put(DataContract.Camment.recorded, String.valueOf(rec));

        cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
    }

    public static void updateCammentGroupId(CCamment camment, String groupUuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {camment.getUuid()};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.userGroupUuid, groupUuid);

        cr.update(DataContract.Camment.CONTENT_URI, cv, where, selectionArgs);
    }

    public static int getCammentsSize() {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();
        Cursor cursor = cr.query(DataContract.Camment.CONTENT_URI, CAMMENT_PROJECTION, null, null, null);

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

        return camment;
    }

    public static List<CCamment> listFromCursor(Cursor cursor) {
        List<CCamment> camments = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                CCamment camment;
                do {
                    camment = fromCursor(cursor);
                    camments.add(camment);
                } while (cursor.moveToNext());
            }
        }
        return camments;
    }

}
