package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import tv.camment.cammentsdk.helpers.Step;


public final class OnboardingOverlay extends BaseOnboardingOverlay {

    public OnboardingOverlay(Context context) {
        super(context);
    }

    public OnboardingOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OnboardingOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public OnboardingOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setAnchorViews(View recordingButton, View recyclerView) {
        super.setAnchorViews(recordingButton, recyclerView);
    }

    public void displayTooltip(Step step) {
        super.displayTooltip(step);
    }

    public void hideTooltipIfNeeded(Step step) {
        super.hideTooltipIfNeeded(step);
    }

}
