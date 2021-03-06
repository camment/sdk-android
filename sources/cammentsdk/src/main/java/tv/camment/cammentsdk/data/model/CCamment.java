package tv.camment.cammentsdk.data.model;

import android.text.TextUtils;

import com.camment.clientsdk.model.Camment;

import tv.camment.cammentsdk.utils.DateTimeUtils;


public final class CCamment extends Camment {

    private long timestampLong;
    private int transferId = -1;
    private boolean recorded;
    private boolean deleted;
    private boolean seen;
    private long startTimestamp;
    private long endTimestamp;

    public CCamment() {

    }

    public CCamment(Camment camment) {
        setUuid(camment.getUuid());
        setUserGroupUuid(camment.getUserGroupUuid());
        setUserCognitoIdentityId(camment.getUserCognitoIdentityId());
        setThumbnail(camment.getThumbnail());
        setShowUuid(camment.getShowUuid());
        setUrl(camment.getUrl());
        setTimestampLong(DateTimeUtils.getTimestampFromIsoDateString(camment.getTimestamp()));
        setTransferId(-1);
        setRecorded(true);
        setDeleted(false);
        setSeen(false);
        setStartTimestamp(0);
        setEndTimestamp(0);
        setPinned(camment.getPinned());
        setShowAt(camment.getShowAt());
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode()
                + (TextUtils.isEmpty(getThumbnail()) ? 0 : getThumbnail().hashCode());
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

    public long getTimestampLong() {
        return timestampLong;
    }

    public void setTimestampLong(long timestampLong) {
        this.timestampLong = timestampLong;
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

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

}