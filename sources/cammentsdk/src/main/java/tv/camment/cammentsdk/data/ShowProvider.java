package tv.camment.cammentsdk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.camment.clientsdk.model.Show;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import tv.camment.cammentsdk.CammentSDK;


public final class ShowProvider {

    public static void insertShows(List<Show> shows) {
        if (shows == null || shows.size() == 0)
            return;

        List<ContentValues> values = new ArrayList<>();
        ContentValues cv;

        for (Show show : shows) {
            cv = new ContentValues();

            cv.put(DataContract.Show.uuid, show.getUuid());
            cv.put(DataContract.Show.url, show.getUrl());
            cv.put(DataContract.Show.thumbnail, show.getThumbnail());
            cv.put(DataContract.Show.startAt, show.getStartAt() != null ? show.getStartAt().longValue() * 1000 : -1);

            values.add(cv);
        }

        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .bulkInsert(DataContract.Show.CONTENT_URI, values.toArray(new ContentValues[values.size()]));
    }

    public static void deleteShows() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.Show.CONTENT_URI, null, null);
    }

    public static Show getShowByUuid(String uuid) {
        if (TextUtils.isEmpty(uuid))
            return null;

        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        String where = DataContract.Show.uuid + "=?";
        String[] selectionArgs = {uuid};

        Cursor cursor = cr.query(DataContract.Show.CONTENT_URI, null, where, selectionArgs, null);

        Show show = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                show = fromCursor(cursor);
            }

            cursor.close();
        }

        return show;
    }

    public static List<Show> listFromCursor(Cursor cursor) {
        List<Show> shows = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Show show;
                do {
                    show = fromCursor(cursor);
                    shows.add(show);
                } while (cursor.moveToNext());
            }
        }
        return shows;
    }

    private static Show fromCursor(Cursor cursor) {
        Show show = new Show();
        show.setUuid(cursor.getString(cursor.getColumnIndex(DataContract.Show.uuid)));
        show.setUrl(cursor.getString(cursor.getColumnIndex(DataContract.Show.url)));
        show.setThumbnail(cursor.getString(cursor.getColumnIndex(DataContract.Show.thumbnail)));
        long startAt = cursor.getLong(cursor.getColumnIndex(DataContract.Show.startAt));
        show.setStartAt(startAt == -1 ? null : BigDecimal.valueOf(startAt));

        return show;
    }

    public static Loader<Cursor> getShowLoader() {
        return new CursorLoader(CammentSDK.getInstance().getApplicationContext(), DataContract.Show.CONTENT_URI,
                null, null, null, null);
    }

}
