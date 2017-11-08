package tv.camment.cammentsdk.views;

import android.content.Context;
import android.util.AttributeSet;


public final class RecordingButton extends SquareImageButton {

    public RecordingButton(Context context) {
        super(context);
    }

    public RecordingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void show() {
        animate().translationX(0).alpha(0.5f).start();
    }

    public void hide() {
        animate().translationX(getWidth() * 2).alpha(0.0f).start();
    }

}
