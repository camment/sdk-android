package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public final class UserRemovedMessage extends BaseMessage {

    public Body body;

    public UserRemovedMessage() {
        super();
    }

    public static class Body implements Parcelable {
        public String userCognitoIdentityId;
        public String groupUuid;

        Body(Parcel in) {
            userCognitoIdentityId = in.readString();
            groupUuid = in.readString();
        }

        public static final Creator<Body> CREATOR = new Creator<Body>() {
            @Override
            public Body createFromParcel(Parcel in) {
                return new Body(in);
            }

            @Override
            public Body[] newArray(int size) {
                return new Body[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(userCognitoIdentityId);
            parcel.writeString(groupUuid);
        }
    }

}
