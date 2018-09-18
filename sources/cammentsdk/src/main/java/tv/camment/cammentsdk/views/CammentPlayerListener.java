package tv.camment.cammentsdk.views;

public interface CammentPlayerListener {

    int getCurrentPosition();

    boolean isPlaying();

    void onSyncPosition(int currentPosition, boolean isPlaying);

}
