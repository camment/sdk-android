package tv.camment.cammentsdk.data.model;


import com.camment.clientsdk.model.Usergroup;
import com.camment.clientsdk.model.UsergroupListItem;
import com.camment.clientsdk.model.Userinfo;

import java.util.List;

public final class CUserGroup extends Usergroup {

    private boolean active;
    private List<Userinfo> users;

    public CUserGroup() {

    }

    public CUserGroup(Usergroup usergroup) {
        setUuid(usergroup.getUuid());
        setUserCognitoIdentityId(usergroup.getUserCognitoIdentityId());
        setTimestamp(usergroup.getTimestamp());
    }

    public CUserGroup(UsergroupListItem usergroup) {
        setUuid(usergroup.getGroupUuid());
        setTimestamp(usergroup.getTimestamp());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Userinfo> getUsers() {
        return users;
    }

    public void setUsers(List<Userinfo> users) {
        this.users = users;
    }

}
