package tv.camment.cammentsdk.views;

import android.content.Context;
import android.util.AttributeSet;


@SuppressWarnings("deprecation")
public final class RecordingButton extends BaseRecordingButton {

    public RecordingButton(Context context) {
        super(context);
    }

    public RecordingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(ActionsListener actionsListener) {
        this.actionsListener = actionsListener;
    }

    public void show() {
        super.show();
    }

    public void hide() {
        super.hide();
    }

    interface ActionsListener
            extends BaseRecordingButton.ActionsListener {

    }

}
