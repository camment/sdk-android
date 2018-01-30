package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public final class UserUnblockMessage extends BaseMessage {

    public Body body;

    public UserUnblockMessage() { //internal
        super();
    }

    private UserUnblockMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<UserUnblockMessage> CREATOR = new Creator<UserUnblockMessage>() {
        @Override
        public UserUnblockMessage createFromParcel(Parcel in) {
            return new UserUnblockMessage(in);
        }

        @Override
        public UserUnblockMessage[] newArray(int size) {
            return new UserUnblockMessage[size];
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
        public String name;

        public Body() {

        }

        Body(Parcel in) {
            name = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
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
