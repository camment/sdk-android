package tv.camment.cammentsdk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tv.camment.cammentsdk.utils.DbUtils;

/**
 * Created by petrushka on 11/08/2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cammentsdk";
    private static final int DB_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("database", "onCreate");
        db.execSQL(createUserGroupTable());
        db.execSQL(createShowTable());
        db.execSQL(createCammentUploadTable());
        db.execSQL(createCammentTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO do we need to do upgrade?
        //we can alter tables
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
                .build();
    }

    private String createCammentUploadTable() {
        return DbUtils.TableBuilder.table(DataContract.Tables.CAMMENT_UPLOAD)
                .primaryKey(DataContract.CammentUpload._ID)
                .columnTextUnique(DataContract.CammentUpload.uuid)
                .columnText(DataContract.CammentUpload.url)
                .columnText(DataContract.CammentUpload.showUuid)
                .columnText(DataContract.CammentUpload.thumbnail)
                .columnText(DataContract.CammentUpload.userGroupUuid)
                .columnText(DataContract.CammentUpload.userCognitoIdentityId)
                .columnInt(DataContract.CammentUpload.transferId)
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
                .build();
    }

}
