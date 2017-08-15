package tv.camment.cammentsdk.data.model;

import com.camment.clientsdk.model.Camment;


public class CCamment extends Camment {

    private long timestamp;

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (o == null || o.getClass() != getClass())
            return false;

        CCamment c = (CCamment) o;

        return c.getUuid().equals(getUuid());
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
