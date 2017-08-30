package tv.camment.cammentsdk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


final class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cammentsdk";
    private static final int DB_VERSION = 1;

    DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createUserGroupTable());
        db.execSQL(createShowTable());
        db.execSQL(createCammentTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //will handle upgrade if needed in the future
    }

    private String createUserGroupTable() {
        return DbUtils.TableBuilder.table(DataContract.Tables.USER_GROUP)
                .primaryKey(DataContract.UserGroup._ID)
                .columnTextUnique(DataContract.UserGroup.uuid)
                .columnText(DataContract.UserGroup.userCognitoIdentityId)
                .columnText(DataContract.UserGroup.timestamp)
                .build();
    }

    private String createShowTable() {
        return DbUtils.TableBuilder.table(DataContract.Tables.SHOW)
                .primaryKey(DataContract.Show._ID)
                .columnTextUnique(DataContract.Show.uuid)
                .columnText(DataContract.Show.url)
                .columnText(DataContract.Show.thumbnail)
                .build();
    }

    private String createCammentTable() {
        return DbUtils.TableBuilder.table(DataContract.Tables.CAMMENT)
                .primaryKey(DataContract.Camment._ID)
                .columnTextUnique(DataContract.Camment.uuid)
                .columnText(DataContract.Camment.url)
                .columnText(DataContract.Camment.showUuid)
                .columnText(DataContract.Camment.thumbnail)
                .columnText(DataContract.Camment.userGroupUuid)
                .columnText(DataContract.Camment.userCognitoIdentityId)
                .columnInt(DataContract.Camment.timestamp)
                .columnInt(DataContract.Camment.transferId)
                .columnInt(DataContract.Camment.recorded)
                .build();
    }

}
