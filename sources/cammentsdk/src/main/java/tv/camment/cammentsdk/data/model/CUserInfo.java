package tv.camment.cammentsdk.data.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.camment.clientsdk.model.Userinfo;

public final class CUserInfo extends Userinfo implements Parcelable {

    private String groupUuid;

    public CUserInfo() {
        super();
    }

    private CUserInfo(Parcel in) {
        setUserCognitoIdentityId(in.readString());
        setName(in.readString());
        setState(in.readString());
        setPicture(in.readString());
        setGroupUuid(in.readString());
        setActiveGroup(in.readString());
        setIsOnline(in.readByte() == 1);
    }

    @Override
    public int hashCode() {
        return (TextUtils.isEmpty(getUserCognitoIdentityId()) ? 0 : getUserCognitoIdentityId().hashCode())
                + (getIsOnline() ? 1 : 0)
                + (TextUtils.isEmpty(getState()) ? 0 : getState().hashCode())
                + (TextUtils.isEmpty(getGroupUuid()) ? 0 : getGroupUuid().hashCode())
                + (TextUtils.isEmpty(getName()) ? 0 : getName().hashCode())
                + (TextUtils.isEmpty(getPicture()) ? 0 : getPicture().hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (o == null || o.getClass() != getClass())
            return false;

        CUserInfo c = (CUserInfo) o;

        return c.getUserCognitoIdentityId().equals(getUserCognitoIdentityId());
    }

    public static final Creator<CUserInfo> CREATOR = new Creator<CUserInfo>() {
        @Override
        public CUserInfo createFromParcel(Parcel in) {
            return new CUserInfo(in);
        }

        @Override
        public CUserInfo[] newArray(int size) {
            return new CUserInfo[size];
        }
    };

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    public UserState getUserState() {
        if (getState() == null)
            return UserState.UNDEFINED;

        return UserState.fromString(getState());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getUserCognitoIdentityId());
        dest.writeString(getName());
        dest.writeString(getState());
        dest.writeString(getPicture());
        dest.writeString(getGroupUuid());
        dest.writeString(getActiveGroup());
        dest.writeByte((byte) (getIsOnline() ? 1 : 0));
    }
}
