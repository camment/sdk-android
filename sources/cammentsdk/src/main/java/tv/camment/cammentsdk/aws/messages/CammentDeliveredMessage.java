package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public final class CammentDeliveredMessage extends BaseMessage {

    public Body body;

    public CammentDeliveredMessage() {
        super();
    }

    private CammentDeliveredMessage(Parcel in) {
        super(in);
        body = in.readParcelable(Body.class.getClassLoader());
    }

    public static final Creator<CammentDeliveredMessage> CREATOR = new Creator<CammentDeliveredMessage>() {
        @Override
        public CammentDeliveredMessage createFromParcel(Parcel in) {
            return new CammentDeliveredMessage(in);
        }

        @Override
        public CammentDeliveredMessage[] newArray(int size) {
            return new CammentDeliveredMessage[size];
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

        Body(Parcel in) {
            uuid = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(uuid);
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
