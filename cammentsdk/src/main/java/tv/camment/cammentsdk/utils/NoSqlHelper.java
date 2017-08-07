package tv.camment.cammentsdk.utils;

import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.CammentList;
import com.camment.clientsdk.model.Show;
import com.camment.clientsdk.model.Usergroup;
import com.github.florent37.androidnosql.Listener;
import com.github.florent37.androidnosql.NoSql;

/**
 * Created by petrushka on 04/08/2017.
 */

public class NoSqlHelper {

    private static final String ACTIVE_GROUP = "/groups/active_group";
    private static final String CAMMENT_UPLOAD = "/camment/upload/";
    private static final String CAMMENT_TRANSFER = "/camment/transfer/";
    private static final String CAMMENTS = "/camments/";
    private static final String CAMMENTS_LIST = "/camments_list";
    private static final String CURRENT_SHOW_UUID = "/show/current";

    public static synchronized void setActiveGroup(Usergroup usergroup) {
        NoSql.getInstance().put(ACTIVE_GROUP, usergroup);
    }

    public static Usergroup getActiveGroup() {
        return NoSql.getInstance().get(ACTIVE_GROUP, Usergroup.class);
    }

    public static synchronized void setCurrentShow(String uuid) {
        Show show = new Show();
        show.setUuid(uuid);
        show.setUrl("");

        NoSql.getInstance().put(CURRENT_SHOW_UUID, show);
    }

    public static Show getCurrentShow() {
        return NoSql.getInstance().get(CURRENT_SHOW_UUID, Show.class);
    }

    public static synchronized void setCammentUpload(Camment camment) {
        NoSql.getInstance().put(CAMMENT_UPLOAD + camment.getUuid(), camment);
    }

    public static Camment getCammentUpload(String cammentUuid) {
        return NoSql.getInstance().get(CAMMENT_UPLOAD + cammentUuid, Camment.class);
    }

    public static synchronized void setCammentTransfer(int transferId, Camment camment) {
        NoSql.getInstance().put(CAMMENT_TRANSFER + transferId, camment);
    }

    public static Camment getCammentTransfer(int transferId) {
        return NoSql.getInstance().get(CAMMENT_TRANSFER + transferId, Camment.class);
    }

    public static synchronized void setCammentList(CammentList cammentList) {
        NoSql.getInstance().put(CAMMENTS_LIST, cammentList);
    }

    public static CammentList getCammentList() {
        return NoSql.getInstance().get(CAMMENTS_LIST, CammentList.class);
    }

}
