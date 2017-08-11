package tv.camment.cammentsdk.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.camment.clientsdk.model.Show;

import tv.camment.cammentsdk.CammentSDK;

/**
 * Created by petrushka on 11/08/2017.
 */

public class ShowProvider {

    private static final String[] SHOW_PROJECTION = {
            DataContract.Show._ID,
            DataContract.Show.uuid,
            DataContract.Show.url};

    public static void insertShow(Show show) {
        if (show == null)
            return;

        deleteShows();

        ContentValues cv = new ContentValues();
        cv.put(DataContract.Show.uuid, show.getUuid());
        cv.put(DataContract.Show.url, show.getUrl());

        CammentSDK.getInstance().getApplicationContext().getContentResolver().insert(DataContract.Show.CONTENT_URI, cv);
    }

    public static void deleteShows() {
        CammentSDK.getInstance().getApplicationContext().getContentResolver()
                .delete(DataContract.Show.CONTENT_URI, null, null);
    }

    public static Show getShow() {
        ContentResolver cr = CammentSDK.getInstance().getApplicationContext().getContentResolver();

        Cursor cursor = cr.query(DataContract.Show.CONTENT_URI, SHOW_PROJECTION, null, null, null);

        Show show = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                show = fromCursor(cursor);
            }

            cursor.close();
        }

        return show;
    }

    private static Show fromCursor(Cursor cursor) {
        Show show = new Show();
        show.setUuid(cursor.getString(cursor.getColumnIndex(DataContract.Show.uuid)));
        show.setUrl(cursor.getString(cursor.getColumnIndex(DataContract.Show.url)));

        return show;
    }

}
