package tv.camment.cammentsdk.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by petrushka on 11/08/2017.
 */

public class DataContract {

    public static final String AUTHORITY = "tv.camment.cammentsdk";
    /**
     * The content:// style URL for the top-level authority
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    private DataContract() {
    }

    private static String buildContentTypeDir(String name) {
        return "vnd.android.cursor.dir/" + AUTHORITY + "." + name;
    }

    private static String buildContentTypeItem(String name) {
        return "vnd.android.cursor.item/" + AUTHORITY + "." + name;
    }

    public interface Tables {
        String USER_GROUP = "user_group";
        String USER_GROUP_ID = "user_group/*";
        String SHOW = "show";
        String SHOW_ID = "show/*";
        String CAMMENT_UPLOAD = "camment_upload";
        String CAMMENT_UPLOAD_ID = "camment_upload/*";
        String CAMMENT = "camment";
        String CAMMENT_ID = "camment/*";
    }

    public interface Codes {
        int USER_GROUP = 100;
        int USER_GROUP_ID = 101;
        int SHOW = 200;
        int SHOW_ID = 201;
        int CAMMENT_UPLOAD = 300;
        int CAMMENT_UPLOAD_ID = 301;
        int CAMMENT = 400;
        int CAMMENT_ID = 401;
    }

    private interface UserGroupColumns {
        String userCognitoIdentityId = "userCognitoIdentityId";
        String uuid = "uuid";
        String timestamp = "timestamp";
    }

    public static final class UserGroup implements BaseColumns, UserGroupColumns {
        public static final String CONTENT_TYPE = buildContentTypeDir(Tables.USER_GROUP);
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, Tables.USER_GROUP);
        public static final String CONTENT_TYPE_ITEM = buildContentTypeItem(Tables.USER_GROUP_ID);
    }

    private interface ShowColumns {
        String uuid = "uuid";
        String url = "url";
    }

    public static final class Show implements BaseColumns, ShowColumns {
        public static final String CONTENT_TYPE = buildContentTypeDir(Tables.SHOW);
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, Tables.SHOW);
        public static final String CONTENT_TYPE_ITEM = buildContentTypeItem(Tables.SHOW_ID);
    }

    private interface CammentColumns {
        String uuid = "uuid";
        String url = "url";
        String thumbnail = "thumbnail";
        String userGroupUuid = "userGroupUuid";
        String userCognitoIdentityId = "userCognitoIdentityId";
        String showUuid = "showUuid";
    }

    private interface UploadColumns {
        String transferId = "transferId";
    }

    public static final class CammentUpload implements BaseColumns, CammentColumns, UploadColumns {
        public static final String CONTENT_TYPE = buildContentTypeDir(Tables.CAMMENT_UPLOAD);
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, Tables.CAMMENT_UPLOAD);
        public static final String CONTENT_TYPE_ITEM = buildContentTypeItem(Tables.CAMMENT_UPLOAD_ID);
    }

    public static final class Camment implements BaseColumns, CammentColumns {
        public static final String CONTENT_TYPE = buildContentTypeDir(Tables.CAMMENT);
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, Tables.CAMMENT);
        public static final String CONTENT_TYPE_ITEM = buildContentTypeItem(Tables.CAMMENT_ID);
    }

}
