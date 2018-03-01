package tv.camment.cammentsdk.data;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.camment.clientsdk.model.Usergroup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.aws.messages.AdMessage;
import tv.camment.cammentsdk.data.model.ChatItem;
import tv.camment.cammentsdk.utils.LogUtils;

public final class AdvertisementProvider {

    private static final String[] ADS_PROJECTION = {
            DataContract.Advertisement._ID,
            DataContract.Advertisement.uuid,
            DataContract.Advertisement.timestamp,
            DataContract.Advertisement.title,
            DataContract.Advertisement.file,
            DataContract.Advertisement.url,
            DataContract.Advertisement.thumbnail,
            DataContract.Advertisement.groupUuid};

    public static void insertAd(AdMessage adMessage) {
        if (adMessage == null || adMessage.body == null)
            return;

        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup == null)
            return;

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Advertisement.uuid, UUID.randomUUID().toString());
        cv.put(DataContract.Advertisement.timestamp, System.currentTimeMillis());
        cv.put(DataContract.Advertisement.title, adMessage.body.title);
        cv.put(DataContract.Advertisement.file, adMessage.body.file);
        cv.put(DataContract.Advertisement.url, adMessage.body.url);
        cv.put(DataContract.Advertisement.thumbnail, adMessage.body.thumbnail);
        cv.put(DataContract.Advertisement.groupUuid, activeUserGroup.getUuid());

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .insert(DataContract.Advertisement.CONTENT_URI, cv);
    }

    static void deleteAds() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.Advertisement.CONTENT_URI, null, null);
    }

    public static void deleteAdByUuid(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Advertisement.uuid + "=?";
        String[] selectionArgs = {uuid};

        int delete = cr.delete(DataContract.Advertisement.CONTENT_URI, where, selectionArgs);
        LogUtils.debug("deleteAd", uuid + " - " + (delete > 0));
    }

    public static ChatItem<AdMessage> getAdByUuid(String uuid) {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Advertisement.uuid + "=?";
        String[] selectionArgs = {uuid};

        Cursor cursor = cr.query(DataContract.Advertisement.CONTENT_URI, ADS_PROJECTION, where, selectionArgs, null);

        ChatItem<AdMessage> adMessage = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                adMessage = fromCursor(cursor);
            }

            cursor.close();
        }

        return adMessage;
    }

    private static ChatItem<AdMessage> fromCursor(Cursor cursor) {
        AdMessage.Body adBody = new AdMessage.Body();
        adBody.title = cursor.getString(cursor.getColumnIndex(DataContract.Advertisement.title));
        adBody.file = cursor.getString(cursor.getColumnIndex(DataContract.Advertisement.file));
        adBody.url = cursor.getString(cursor.getColumnIndex(DataContract.Advertisement.url));
        adBody.thumbnail = cursor.getString(cursor.getColumnIndex(DataContract.Advertisement.thumbnail));

        AdMessage adMessage = new AdMessage();
        adMessage.body = adBody;

        String uuid = cursor.getString(cursor.getColumnIndex(DataContract.Advertisement.uuid));
        long timestamp = cursor.getLong(cursor.getColumnIndex(DataContract.Advertisement.timestamp));

        return new ChatItem<>(ChatItem.ChatItemType.AD, uuid, timestamp, adMessage);
    }

    public static List<ChatItem<AdMessage>> listFromCursor(Cursor cursor) {
        List<ChatItem<AdMessage>> ads = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                ChatItem<AdMessage> ad;
                do {
                    ad = fromCursor(cursor);
                    ads.add(ad);
                } while (cursor.moveToNext());
            }
        }
        return ads;
    }

    public static Loader<Cursor> getAdvertisementLoader() {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

        if (activeUserGroup != null
                && !TextUtils.isEmpty(activeUserGroup.getUuid())) {
            String selection = DataContract.Advertisement.groupUuid + "=?";
            String[] selectionArgs = new String[]{activeUserGroup.getUuid()};

            return new CursorLoader(CammentSDK.getInstance().getApplicationContext(), DataContract.Advertisement.CONTENT_URI,
                    null, selection, selectionArgs, DataContract.Advertisement.timestamp + " DESC");
        } else {
            return null;
        }
    }
}
