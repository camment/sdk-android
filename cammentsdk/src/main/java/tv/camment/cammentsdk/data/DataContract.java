package tv.camment.cammentsdk.data;

import android.net.Uri;
import android.provider.BaseColumns;


final class DataContract {

    static String AUTHORITY;

    static Uri AUTHORITY_URI;

    /**
     * The content:// style URL for the top-level authority
     */
    private DataContract() {
    }

    private static String buildContentTypeDir(String name) {
        return "vnd.android.cursor.dir/" + AUTHORITY + "." + name;
    }

    private static String buildContentTypeItem(String name) {
        return "vnd.android.cursor.item/" + AUTHORITY + "." + name;
    }

    interface Tables {
        String USER_GROUP = "user_group";
        String USER_GROUP_ID = "user_group/*";
        String SHOW = "show";
        String SHOW_ID = "show/*";
        String CAMMENT = "camment";
        String CAMMENT_ID = "camment/*";
        String USER_INFO = "user_info";
        String USER_INFO_ID = "user_info/*";
    }

    interface Codes {
        int USER_GROUP = 100;
        int USER_GROUP_ID = 101;
        int SHOW = 200;
        int SHOW_ID = 201;
        int CAMMENT = 400;
        int CAMMENT_ID = 401;
        int USER_INFO = 500;
        int USER_INFO_ID = 501;
    }

    private interface UserGroupColumns {
        String userCognitoIdentityId = "userCognitoIdentityId";
        String uuid = "uuid";
        String timestamp = "timestamp";
        String active = "active";
    }

    static final class UserGroup implements BaseColumns, UserGroupColumns {
        static final String CONTENT_TYPE = buildContentTypeDir(Tables.USER_GROUP);
        static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, Tables.USER_GROUP);
        static final String CONTENT_TYPE_ITEM = buildContentTypeItem(Tables.USER_GROUP_ID);
    }

    private interface ShowColumns {
        String uuid = "uuid";
        String url = "url";
        String thumbnail = "thumbnail";
        String startAt = "startAt";
    }

    static final class Show implements BaseColumns, ShowColumns {
        static final String CONTENT_TYPE = buildContentTypeDir(Tables.SHOW);
        static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, Tables.SHOW);
        static final String CONTENT_TYPE_ITEM = buildContentTypeItem(Tables.SHOW_ID);
    }

    private interface CammentColumns {
        String uuid = "uuid";
        String url = "url";
        String thumbnail = "thumbnail";
        String userGroupUuid = "userGroupUuid";
        String userCognitoIdentityId = "userCognitoIdentityId";
        String showUuid = "showUuid";
        String timestamp = "timestamp";
        String transferId = "transferId";
        String recorded = "recorded";
        String deleted = "deleted";
        String seen = "seen";
        String sent = "sent";
        String received = "received";
        String startTimestamp = "startTimestamp";
        String endTimestamp = "endTimestamp";
    }

    static final class Camment implements BaseColumns, CammentColumns {
        static final String CONTENT_TYPE = buildContentTypeDir(Tables.CAMMENT);
        static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, Tables.CAMMENT);
        static final String CONTENT_TYPE_ITEM = buildContentTypeItem(Tables.CAMMENT_ID);
    }

    private interface UserInfoColumns {
        String userCognitoIdentityId = "userCognitoIdentityId";
        String name = "name";
        String picture = "picture";
        String groupUuid = "groupUuid";
    }

    static final class UserInfo implements BaseColumns, UserInfoColumns {
        static final String CONTENT_TYPE = buildContentTypeDir(Tables.USER_INFO);
        static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, Tables.USER_INFO);
        static final String CONTENT_TYPE_ITEM = buildContentTypeItem(Tables.USER_INFO_ID);
    }

}
