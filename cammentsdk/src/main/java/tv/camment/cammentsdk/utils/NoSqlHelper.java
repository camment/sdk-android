package tv.camment.cammentsdk.utils;

import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.Usergroup;
import com.github.florent37.androidnosql.NoSql;

/**
 * Created by petrushka on 04/08/2017.
 */

public class NoSqlHelper {

    private static final String ACTIVE_GROUP = "/groups/active_group";
    private static final String CAMMENT_UPLOAD = "/camment/upload";
    private static final String CURRENT_SHOW_UUID = "/show/current";

    public static synchronized void setActiveGroup(Usergroup usergroup) {
        NoSql.getInstance().put(ACTIVE_GROUP, usergroup);
    }

    public static Usergroup getActiveGroup() {
        return NoSql.getInstance().get(ACTIVE_GROUP, Usergroup.class);
    }

    public static synchronized void setCurrentShowUuid(String uuid) {
        NoSql.getInstance().put(CURRENT_SHOW_UUID, uuid);
    }

    public static String getCurrentShowUuid() {
        return NoSql.getInstance().get(CURRENT_SHOW_UUID, String.class);
    }

    public static synchronized void setCammentUpload(Camment camment) {
        NoSql.getInstance().put(CAMMENT_UPLOAD, camment);
    }

    public static Camment getCammentUpload() {
        return NoSql.getInstance().get(CAMMENT_UPLOAD, Camment.class);
    }
}
