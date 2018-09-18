package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public final class CammentMessage extends BaseMessage {

    public Body body;

    public CammentMessage() {
        super();
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

    public static class Body implements Parcelable {
        public String uuid;
        public String thumbnail;
        public String userGroupUuid;
        public String url;
        public String userCognitoIdentityId;
        public String timestamp;
        public boolean pinned;
        public int showAt;


        Body(Parcel in) {
            uuid = in.readString();
            thumbnail = in.readString();
            userGroupUuid = in.readString();
            url = in.readString();
            userCognitoIdentityId = in.readString();
            timestamp = in.readString();
            pinned = in.readByte() != 0;
            showAt = in.readInt();
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
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(uuid);
            dest.writeString(thumbnail);
            dest.writeString(userGroupUuid);
            dest.writeString(url);
            dest.writeString(userCognitoIdentityId);
            dest.writeString(timestamp);
            dest.writeByte((byte) (pinned ? 1 : 0));
            dest.writeInt(showAt);
        }
    }

}
