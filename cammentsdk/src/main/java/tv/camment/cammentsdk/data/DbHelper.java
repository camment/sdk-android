package tv.camment.cammentsdk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import tv.camment.cammentsdk.api.ApiManager;


final class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cammentsdk";
    private static final int DB_VERSION = 2;

    DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createUserGroupTable());
        db.execSQL(createShowTable());
        db.execSQL(createCammentTable());
        db.execSQL(createUserInfoTable());

        ApiManager.getInstance().getUserApi().getMyUserGroups();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //will handle upgrade if needed in the future
        db.execSQL(DbUtils.dropTable(DataContract.Tables.CAMMENT));
        db.execSQL(DbUtils.dropTable(DataContract.Tables.SHOW));
        db.execSQL(DbUtils.dropTable(DataContract.Tables.USER_GROUP));
        db.execSQL(DbUtils.dropTable(DataContract.Tables.USER_INFO));
        onCreate(db);
    }

    private String createUserGroupTable() {
        return DbUtils.TableBuilder.table(DataContract.Tables.USER_GROUP)
                .primaryKey(DataContract.UserGroup._ID)
                .columnTextUnique(DataContract.UserGroup.uuid)
                .columnText(DataContract.UserGroup.userCognitoIdentityId)
                .columnInt(DataContract.UserGroup.timestamp)
                .columnInt(DataContract.UserGroup.active)
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

    private String createUserInfoTable() {
        return DbUtils.TableBuilder.table(DataContract.Tables.USER_INFO)
                .primaryKey(DataContract.UserInfo._ID)
                .columnTextUnique(DataContract.UserInfo.name)
                .columnText(DataContract.UserInfo.userCognitoIdentityId)
                .columnText(DataContract.UserInfo.picture)
                .columnText(DataContract.UserInfo.groupUuid)
                .build();
    }

}
