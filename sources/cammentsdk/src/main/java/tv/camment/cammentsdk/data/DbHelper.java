package tv.camment.cammentsdk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


final class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cammentsdk";
    private static final int DB_VERSION = 16;

    DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createUserGroupTable());
        db.execSQL(createShowTable());
        db.execSQL(createCammentTable());
        db.execSQL(createUserInfoTable());
        db.execSQL(createAdvertisementTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //will handle upgrade if needed in the future
        db.execSQL(DbUtils.dropTable(DataContract.Tables.CAMMENT));
        db.execSQL(DbUtils.dropTable(DataContract.Tables.SHOW));
        db.execSQL(DbUtils.dropTable(DataContract.Tables.USER_GROUP));
        db.execSQL(DbUtils.dropTable(DataContract.Tables.USER_INFO));
        db.execSQL(DbUtils.dropTable(DataContract.Tables.ADVERTISEMENT));
        onCreate(db);
    }

    private String createUserGroupTable() {
        return DbUtils.TableBuilder.table(DataContract.Tables.USER_GROUP)
                .primaryKey(DataContract.UserGroup._ID)
                .columnTextUnique(DataContract.UserGroup.uuid)
                .columnText(DataContract.UserGroup.userCognitoIdentityId)
                .columnInt(DataContract.UserGroup.timestamp)
                .columnInt(DataContract.UserGroup.active)
                .columnText(DataContract.UserGroup.hostId)
                .columnTextUnique(DataContract.UserGroup.showUuid)
                .columnInt(DataContract.UserGroup.isPublic)
                .build();
    }

    private String createShowTable() {
        return DbUtils.TableBuilder.table(DataContract.Tables.SHOW)
                .primaryKey(DataContract.Show._ID)
                .columnTextUnique(DataContract.Show.uuid)
                .columnText(DataContract.Show.url)
                .columnText(DataContract.Show.thumbnail)
                .columnInt(DataContract.Show.startAt)
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
                .columnInt(DataContract.Camment.deleted)
                .columnInt(DataContract.Camment.seen)
                .columnInt(DataContract.Camment.sent)
                .columnInt(DataContract.Camment.received)
                .columnInt(DataContract.Camment.startTimestamp)
                .columnInt(DataContract.Camment.endTimestamp)
                .columnInt(DataContract.Camment.pinned)
                .columnInt(DataContract.Camment.showAt)
                .build();
    }

    private String createUserInfoTable() {
        return DbUtils.TableBuilder.table(DataContract.Tables.USER_INFO)
                .primaryKey(DataContract.UserInfo._ID)
                .columnTextUnique(DataContract.UserInfo.userCognitoIdentityId)
                .columnText(DataContract.UserInfo.name)
                .columnText(DataContract.UserInfo.picture)
                .columnTextUnique(DataContract.UserInfo.groupUuid)
                .columnText(DataContract.UserInfo.state)
                .columnInt(DataContract.UserInfo.isOnline)
                .columnText(DataContract.UserInfo.activeGroup)
                .build();
    }

    private String createAdvertisementTable() {
        return DbUtils.TableBuilder.table(DataContract.Tables.ADVERTISEMENT)
                .primaryKey(DataContract.Advertisement._ID)
                .columnTextUnique(DataContract.Advertisement.uuid)
                .columnInt(DataContract.Advertisement.timestamp)
                .columnText(DataContract.Advertisement.title)
                .columnText(DataContract.Advertisement.file)
                .columnText(DataContract.Advertisement.url)
                .columnText(DataContract.Advertisement.thumbnail)
                .columnText(DataContract.Advertisement.groupUuid)
                .build();
    }

}
