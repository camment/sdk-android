package tv.camment.cammentsdk.aws.messages;

import android.os.Parcel;
import android.os.Parcelable;

public final class PlayerStateMessage extends BaseMessage {

    public Body body;

    public PlayerStateMessage() {
        super();
    }

    private PlayerStateMessage(Parcel in) {
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

    public static final Creator<PlayerStateMessage> CREATOR = new Creator<PlayerStateMessage>() {
        @Override
        public PlayerStateMessage createFromParcel(Parcel in) {
            return new PlayerStateMessage(in);
        }

        @Override
        public PlayerStateMessage[] newArray(int size) {
            return new PlayerStateMessage[size];
        }
    };

    public static class Body implements Parcelable {
        public String groupUuid;
        public boolean isPlaying;
        public int timestamp;

        public Body(String groupUuid, boolean isPlaying, int timestamp) {
            this.groupUuid = groupUuid;
            this.isPlaying = isPlaying;
            this.timestamp = timestamp;
        }

        protected Body(Parcel in) {
            groupUuid = in.readString();
            isPlaying = in.readByte() != 0;
            timestamp = in.readInt();
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
            dest.writeString(groupUuid);
            dest.writeByte((byte) (isPlaying ? 1 : 0));
            dest.writeInt(timestamp);
        }
    }

}
