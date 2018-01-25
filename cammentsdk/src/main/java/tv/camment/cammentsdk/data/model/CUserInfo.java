package tv.camment.cammentsdk.data.model;


import android.os.Parcel;
import android.os.Parcelable;

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
        groupUuid = in.readString();
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
        dest.writeString(groupUuid);
    }
}
