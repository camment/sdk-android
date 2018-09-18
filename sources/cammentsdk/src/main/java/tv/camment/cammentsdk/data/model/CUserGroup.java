package tv.camment.cammentsdk.data.model;


import android.text.TextUtils;

import com.camment.clientsdk.model.Usergroup;

public final class CUserGroup extends Usergroup {

    private boolean active;
    private long longTimestamp;

    public CUserGroup() {

    }

    @Override
    public int hashCode() {
        return (TextUtils.isEmpty(getUuid()) ? 0 : getUuid().hashCode())
                + (TextUtils.isEmpty(getShowId()) ? 0 : getShowId().hashCode())
                + (TextUtils.isEmpty(getUserCognitoIdentityId()) ? 0 : getUserCognitoIdentityId().hashCode())
                + (TextUtils.isEmpty(getTimestamp()) ? 0 : getTimestamp().hashCode())
                + (isActive() ? 1 : 0)
                + (getUsers() != null ? getUsers().hashCode() : 0)
                + (TextUtils.isEmpty(getHostId()) ? 0 : getHostId().hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (o == null || o.getClass() != getClass())
            return false;

        CUserGroup c = (CUserGroup) o;

        return c.getUuid().equals(getUuid());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getLongTimestamp() {
        return longTimestamp;
    }

    public void setLongTimestamp(long longTimestamp) {
        this.longTimestamp = longTimestamp;
    }

}
