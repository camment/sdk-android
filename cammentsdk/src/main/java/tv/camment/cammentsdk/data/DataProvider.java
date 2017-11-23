package tv.camment.cammentsdk.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static tv.camment.cammentsdk.data.DataContract.AUTHORITY;
import static tv.camment.cammentsdk.data.DataContract.AUTHORITY_URI;
import static tv.camment.cammentsdk.data.DataContract.Camment;
import static tv.camment.cammentsdk.data.DataContract.Codes;
import static tv.camment.cammentsdk.data.DataContract.Show;
import static tv.camment.cammentsdk.data.DataContract.Tables;
import static tv.camment.cammentsdk.data.DataContract.UserGroup;


public final class DataProvider extends ContentProvider {

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        DbHelper dbHelper = new DbHelper(getContext());
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);

        AUTHORITY = info.authority;
        AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

        uriMatcher.addURI(AUTHORITY, Tables.USER_GROUP, Codes.USER_GROUP);
        uriMatcher.addURI(AUTHORITY, Tables.USER_GROUP_ID, Codes.USER_GROUP_ID);
        uriMatcher.addURI(AUTHORITY, Tables.SHOW, Codes.SHOW);
        uriMatcher.addURI(AUTHORITY, Tables.SHOW_ID, Codes.SHOW_ID);
        uriMatcher.addURI(AUTHORITY, Tables.CAMMENT, Codes.CAMMENT);
        uriMatcher.addURI(AUTHORITY, Tables.CAMMENT_ID, Codes.CAMMENT_ID);
        uriMatcher.addURI(AUTHORITY, Tables.USER_INFO, Codes.USER_INFO);
        uriMatcher.addURI(AUTHORITY, Tables.USER_INFO_ID, Codes.USER_INFO_ID);
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
            case Codes.CAMMENT:
                return Camment.CONTENT_TYPE;
            case Codes.CAMMENT_ID:
                return Camment.CONTENT_TYPE_ITEM;
            case Codes.USER_INFO:
                return Camment.CONTENT_TYPE;
            case Codes.USER_INFO_ID:
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
            case Codes.CAMMENT:
                queryBuilder.setTables(Tables.CAMMENT);
                break;
            case Codes.USER_INFO:
                queryBuilder.setTables(Tables.USER_INFO);
                break;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long id = -1;
        switch (uriMatcher.match(uri)) {
            case Codes.USER_GROUP:
                id = db.insertWithOnConflict(Tables.USER_GROUP, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case Codes.SHOW:
                id = db.insertWithOnConflict(Tables.SHOW, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case Codes.CAMMENT:
                id = db.insertWithOnConflict(Tables.CAMMENT, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case Codes.USER_INFO:
                id = db.insertWithOnConflict(Tables.USER_INFO, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                break;
        }

        if (id > -1) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return uri;
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
            case Codes.CAMMENT:
                table = Tables.CAMMENT;
                break;
            case Codes.USER_INFO:
                table = Tables.USER_INFO;
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
            case Codes.CAMMENT:
                delCount = db.delete(Tables.CAMMENT, selection, selectionArgs);
                break;
            case Codes.USER_INFO:
                delCount = db.delete(Tables.USER_INFO, selection, selectionArgs);
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
            case Codes.CAMMENT:
                updateCount = db.update(Tables.CAMMENT, values, selection, selectionArgs);
                break;
            case Codes.USER_INFO:
                updateCount = db.update(Tables.USER_INFO, values, selection, selectionArgs);
                break;
        }

        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

}