package tv.camment.cammentsdk.aws.messages;


import android.os.Parcel;
import android.os.Parcelable;

public class BaseMessage implements Parcelable {

    public MessageType type;

    public BaseMessage() {

    }

    protected BaseMessage(Parcel in) {
        this.type = MessageType.fromInt(in.readInt());
    }

    public static final Creator<BaseMessage> CREATOR = new Creator<BaseMessage>() {
        @Override
        public BaseMessage createFromParcel(Parcel in) {
            return new BaseMessage(in);
        }

        @Override
        public BaseMessage[] newArray(int size) {
            return new BaseMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(type.getIntValue());
    }

}
