package tv.camment.cammentsdk.aws.messages;

import android.os.Parcel;
import android.os.Parcelable;

public final class UserOnlineMessage extends BaseMessage {

    public Body body;

    public UserOnlineMessage() {
        super();
    }

    private UserOnlineMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        super.writeToParcel(dest, i);
        dest.writeParcelable(body, i);
    }

    public static final Creator<UserOnlineMessage> CREATOR = new Creator<UserOnlineMessage>() {
        @Override
        public UserOnlineMessage createFromParcel(Parcel in) {
            return new UserOnlineMessage(in);
        }

        @Override
        public UserOnlineMessage[] newArray(int size) {
            return new UserOnlineMessage[size];
        }
    };

    public static class Body implements Parcelable {
        public String userId;

        Body(Parcel in) {
            userId = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(userId);
        }

        @Override
        public int describeContents() {
            return 0;
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
    }
}
