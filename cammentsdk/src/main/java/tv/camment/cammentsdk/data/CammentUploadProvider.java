package tv.camment.cammentsdk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.data.model.CammentUpload;

/**
 * Created by petrushka on 11/08/2017.
 */

public class CammentUploadProvider {

    private static final String[] CAMMENT_PROJECTION = {
            DataContract.CammentUpload._ID,
            DataContract.CammentUpload.uuid,
            DataContract.CammentUpload.url,
            DataContract.CammentUpload.showUuid,
            DataContract.CammentUpload.thumbnail,
            DataContract.CammentUpload.userCognitoIdentityId,
            DataContract.CammentUpload.userGroupUuid,
            DataContract.CammentUpload.transferId};

    public static void insertCammentUpload(CammentUpload camment) {
        if (camment == null)
            return;

        ContentValues cv = new ContentValues();
        cv.put(DataContract.CammentUpload.uuid, camment.getUuid());
        cv.put(DataContract.CammentUpload.url, camment.getUrl());
        cv.put(DataContract.CammentUpload.showUuid, camment.getShowUuid());
        cv.put(DataContract.CammentUpload.thumbnail, camment.getThumbnail());
        cv.put(DataContract.CammentUpload.userCognitoIdentityId, camment.getUserCognitoIdentityId());
        cv.put(DataContract.CammentUpload.userGroupUuid, camment.getUserGroupUuid());
        cv.put(DataContract.CammentUpload.transferId, camment.getTransferId());

        CammentSDK.getInstance().getApplicationContext().getContentResolver().insert(DataContract.CammentUpload.CONTENT_URI, cv);
    }

    public static void deleteCammentUploads() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.CammentUpload.CONTENT_URI, null, null);
    }

    public static void deleteCammentUploadByUuid(String uuid) {
        String where = DataContract.CammentUpload.uuid + "=?";
        String[] selectionArgs = {uuid};

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.CammentUpload.CONTENT_URI, where, selectionArgs);
    }

    public static CammentUpload getCammentUploadByTransferId(int transferId) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.CammentUpload.transferId + "=?";
        String[] selectionArgs = {String.valueOf(transferId)};

        Cursor cursor = cr.query(DataContract.CammentUpload.CONTENT_URI, CAMMENT_PROJECTION, where, selectionArgs, null);

        CammentUpload camment = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                camment = fromCursor(cursor);
            }

            cursor.close();
        }

        return camment;
    }

    public static CammentUpload getCammentUploadByUuid(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.CammentUpload.uuid + "=?";
        String[] selectionArgs = {uuid};

        Cursor cursor = cr.query(DataContract.CammentUpload.CONTENT_URI, CAMMENT_PROJECTION, where, selectionArgs, null);

        CammentUpload camment = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                camment = fromCursor(cursor);
            }

            cursor.close();
        }

        return camment;
    }

    public static void setCammentUploadTransferId(CammentUpload camment, int transferId) {
        String where = DataContract.CammentUpload.uuid + "=?";
        String[] selectionArgs = {camment.getUuid()};

        ContentValues cv = new ContentValues();
        cv.put(DataContract.CammentUpload.transferId, transferId);

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .update(DataContract.CammentUpload.CONTENT_URI, cv, where, selectionArgs);
    }

    private static CammentUpload fromCursor(Cursor cursor) {
        CammentUpload camment = new CammentUpload();
        camment.setUuid(cursor.getString(cursor.getColumnIndex(DataContract.CammentUpload.uuid)));
        camment.setUrl(cursor.getString(cursor.getColumnIndex(DataContract.CammentUpload.url)));
        camment.setThumbnail(cursor.getString(cursor.getColumnIndex(DataContract.CammentUpload.thumbnail)));
        camment.setUserCognitoIdentityId(cursor.getString(cursor.getColumnIndex(DataContract.CammentUpload.userCognitoIdentityId)));
        camment.setUserGroupUuid(cursor.getString(cursor.getColumnIndex(DataContract.CammentUpload.userGroupUuid)));
        camment.setShowUuid(cursor.getString(cursor.getColumnIndex(DataContract.CammentUpload.showUuid)));
        camment.setTransferId(cursor.getInt(cursor.getColumnIndex(DataContract.CammentUpload.transferId)));

        return camment;
    }

}
