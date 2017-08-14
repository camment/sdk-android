package tv.camment.cammentsdk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.camment.clientsdk.model.Camment;

import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.CammentSDK;

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
            DataContract.Camment.userGroupUuid};

    public static void insertCamment(Camment camment) {
        if (camment == null)
            return;

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Camment.uuid, camment.getUuid());
        cv.put(DataContract.Camment.url, camment.getUrl());
        cv.put(DataContract.Camment.showUuid, camment.getShowUuid());
        cv.put(DataContract.Camment.thumbnail, camment.getThumbnail());
        cv.put(DataContract.Camment.userCognitoIdentityId, camment.getUserCognitoIdentityId());
        cv.put(DataContract.Camment.userGroupUuid, camment.getUserGroupUuid());

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

    public static Camment getCammentByUuid(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Camment.uuid + "=?";
        String[] selectionArgs = {uuid};

        Cursor cursor = cr.query(DataContract.Camment.CONTENT_URI, CAMMENT_PROJECTION, where, selectionArgs, null);

        Camment camment = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                camment = fromCursor(cursor);
            }

            cursor.close();
        }

        return camment;
    }

    private static Camment fromCursor(Cursor cursor) {
        Camment camment = new Camment();
        camment.setUuid(cursor.getString(cursor.getColumnIndex(DataContract.Camment.uuid)));
        camment.setUrl(cursor.getString(cursor.getColumnIndex(DataContract.Camment.url)));
        camment.setThumbnail(cursor.getString(cursor.getColumnIndex(DataContract.Camment.thumbnail)));
        camment.setUserCognitoIdentityId(cursor.getString(cursor.getColumnIndex(DataContract.Camment.userCognitoIdentityId)));
        camment.setUserGroupUuid(cursor.getString(cursor.getColumnIndex(DataContract.Camment.userGroupUuid)));
        camment.setShowUuid(cursor.getString(cursor.getColumnIndex(DataContract.Camment.showUuid)));

        return camment;
    }

}
