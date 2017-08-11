package tv.camment.cammentsdk.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static tv.camment.cammentsdk.data.DataContract.AUTHORITY;
import static tv.camment.cammentsdk.data.DataContract.Camment;
import static tv.camment.cammentsdk.data.DataContract.CammentUpload;
import static tv.camment.cammentsdk.data.DataContract.Codes;
import static tv.camment.cammentsdk.data.DataContract.Show;
import static tv.camment.cammentsdk.data.DataContract.Tables;
import static tv.camment.cammentsdk.data.DataContract.UserGroup;

/**
 * Created by petrushka on 11/08/2017.
 */

public class DataProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, Tables.USER_GROUP, Codes.USER_GROUP);
        uriMatcher.addURI(AUTHORITY, Tables.USER_GROUP_ID, Codes.USER_GROUP_ID);
        uriMatcher.addURI(AUTHORITY, Tables.SHOW, Codes.SHOW);
        uriMatcher.addURI(AUTHORITY, Tables.SHOW_ID, Codes.SHOW_ID);
        uriMatcher.addURI(AUTHORITY, Tables.CAMMENT_UPLOAD, Codes.CAMMENT_UPLOAD);
        uriMatcher.addURI(AUTHORITY, Tables.CAMMENT_UPLOAD_ID, Codes.CAMMENT_UPLOAD_ID);
        uriMatcher.addURI(AUTHORITY, Tables.CAMMENT, Codes.CAMMENT);
        uriMatcher.addURI(AUTHORITY, Tables.CAMMENT_ID, Codes.CAMMENT_ID);
    }

    private SQLiteDatabase db;
    private DbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case Codes.USER_GROUP:
                return UserGroup.CONTENT_TYPE;
            case Codes.USER_GROUP_ID:
                return UserGroup.CONTENT_TYPE_ITEM;
            case Codes.SHOW:
                return Show.CONTENT_TYPE;
            case Codes.SHOW_ID:
                return Show.CONTENT_TYPE_ITEM;
            case Codes.CAMMENT_UPLOAD:
                return CammentUpload.CONTENT_TYPE;
            case Codes.CAMMENT_UPLOAD_ID:
                return CammentUpload.CONTENT_TYPE_ITEM;
            case Codes.CAMMENT:
                return Camment.CONTENT_TYPE;
            case Codes.CAMMENT_ID:
                return Camment.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("unknown uri " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case Codes.USER_GROUP:
                queryBuilder.setTables(Tables.USER_GROUP);
                break;
            case Codes.SHOW:
                queryBuilder.setTables(Tables.SHOW);
                break;
            case Codes.CAMMENT_UPLOAD:
                queryBuilder.setTables(Tables.CAMMENT_UPLOAD);
                break;
            case Codes.CAMMENT:
                queryBuilder.setTables(Tables.CAMMENT);
                break;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long id = 0;
        switch (uriMatcher.match(uri)) {
            case Codes.USER_GROUP:
                id = db.insertWithOnConflict(Tables.USER_GROUP, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case Codes.SHOW:
                id = db.insertWithOnConflict(Tables.SHOW, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case Codes.CAMMENT_UPLOAD:
                id = db.insertWithOnConflict(Tables.CAMMENT_UPLOAD, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case Codes.CAMMENT:
                id = db.insertWithOnConflict(Tables.CAMMENT, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                break;
        }

        Uri newUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(newUri, null);
        return newUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int numInserted = 0;

        String table = "";

        switch (uriMatcher.match(uri)) {
            case Codes.USER_GROUP:
                table = Tables.USER_GROUP;
                break;
            case Codes.SHOW:
                table = Tables.SHOW;
                break;
            case Codes.CAMMENT_UPLOAD:
                table = Tables.CAMMENT_UPLOAD;
                break;
            case Codes.CAMMENT:
                table = Tables.CAMMENT;
                break;
        }

        db.beginTransaction();
        try {
            for (ContentValues cv : values) {
                db.insertOrThrow(table, null, cv);
            }
            db.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(uri, null);
            numInserted = values.length;
        } finally {
            db.endTransaction();
        }

        return numInserted;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int delCount = 0;

        switch (uriMatcher.match(uri)) {
            case Codes.USER_GROUP:
                delCount = db.delete(Tables.USER_GROUP, selection, selectionArgs);
                break;
            case Codes.SHOW:
                delCount = db.delete(Tables.SHOW, selection, selectionArgs);
                break;
            case Codes.CAMMENT_UPLOAD:
                delCount = db.delete(Tables.CAMMENT_UPLOAD, selection, selectionArgs);
                break;
            case Codes.CAMMENT:
                delCount = db.delete(Tables.CAMMENT, selection, selectionArgs);
                break;
        }
        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @
            Nullable String[] selectionArgs) {
        int updateCount = 0;

        switch (uriMatcher.match(uri)) {
            case Codes.USER_GROUP:
                updateCount = db.update(Tables.USER_GROUP, values, selection, selectionArgs);
                break;
            case Codes.SHOW:
                updateCount = db.update(Tables.SHOW, values, selection, selectionArgs);
                break;
            case Codes.CAMMENT_UPLOAD:
                updateCount = db.update(Tables.CAMMENT_UPLOAD, values, selection, selectionArgs);
                break;
            case Codes.CAMMENT:
                updateCount = db.update(Tables.CAMMENT, values, selection, selectionArgs);
                break;
        }

        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }


}
