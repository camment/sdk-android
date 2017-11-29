package tv.camment.cammentsdk.data.model;

import android.text.TextUtils;

import com.camment.clientsdk.model.Camment;


public final class CCamment extends Camment {

    private long timestamp;
    private int transferId = -1;
    private boolean recorded;
    private boolean deleted;
    private boolean seen;
    private boolean sent;
    private boolean received;

    public CCamment() {

    }

    public CCamment(Camment camment) {
        setUuid(camment.getUuid());
        setUserGroupUuid(camment.getUserGroupUuid());
        setUserCognitoIdentityId(camment.getUserCognitoIdentityId());
        setThumbnail(camment.getThumbnail());
        setShowUuid(camment.getShowUuid());
        setUrl(camment.getUrl());
        setTimestamp(System.currentTimeMillis());
        setTransferId(-1);
        setRecorded(true);
        setDeleted(false);
        setSeen(false);
        setSent(false);
        setReceived(false);
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode()
                + (TextUtils.isEmpty(getThumbnail()) ? 0 : getThumbnail().hashCode())
                + (isSent() ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (o == null || o.getClass() != getClass())a
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

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public boolean isRecorded() {
        return recorded;
    }

    public void setRecorded(boolean recorded) {
        this.recorded = recorded;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

}