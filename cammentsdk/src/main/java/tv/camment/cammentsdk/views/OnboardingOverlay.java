package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import tv.camment.cammentsdk.helpers.OnboardingPreferences;
import tv.camment.cammentsdk.helpers.Step;

/**
 * Created by petrushka on 21/08/2017.
 */

public class OnboardingOverlay extends RelativeLayout {

    public OnboardingOverlay(Context context) {
        super(context);
        init();
    }

    public OnboardingOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OnboardingOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public OnboardingOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        OnboardingPreferences.getInstance().initSteps();
    }

    public void displayTooltip(Step step, View attachToView) {
        TooltipView tooltipView = new TooltipView(getContext());
        addView(tooltipView);

        TooltipView.Orientation orientation;
        switch (step) {
            case RECORD:
            case INVITE:
                orientation = TooltipView.Orientation.RIGHT;
                break;
            case PLAY:
            case HIDE:
            case SHOW:
            case DELETE:
            default:
                orientation = TooltipView.Orientation.LEFT;
                break;
        }

        tooltipView.init(attachToView, orientation, step);
    }

}
