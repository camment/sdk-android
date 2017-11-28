package tv.camment.cammentsdk.data.model;

import com.camment.clientsdk.model.Camment;


public final class CCamment extends Camment {

    private long timestamp;
    private int transferId = -1;
    private boolean recorded;
    private boolean deleted;
    private boolean seen;

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
    }

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

}
