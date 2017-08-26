package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public final class CammentMessage extends BaseMessage {

    public Body body;

    public CammentMessage() {
        super();
    }

    public static class Body implements Parcelable {
        public String uuid;
        public String thumbnail;
        public String userGroupUuid;
        public String url;
        public String userCognitoIdentityId;
        public String timestamp;

        Body(Parcel in) {
            uuid = in.readString();
            thumbnail = in.readString();
            userGroupUuid = in.readString();
            url = in.readString();
            userCognitoIdentityId = in.readString();
            timestamp = in.readString();
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
            parcel.writeString(uuid);
            parcel.writeString(thumbnail);
            parcel.writeString(userGroupUuid);
            parcel.writeString(url);
            parcel.writeString(userCognitoIdentityId);
            parcel.writeString(timestamp);
        }
    }

    private CammentMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<CammentMessage> CREATOR = new Creator<CammentMessage>() {
        @Override
        public CammentMessage createFromParcel(Parcel in) {
            return new CammentMessage(in);
        }

        @Override
        public CammentMessage[] newArray(int size) {
            return new CammentMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        super.writeToParcel(dest, i);
        dest.writeParcelable(body, i);
    }

}
