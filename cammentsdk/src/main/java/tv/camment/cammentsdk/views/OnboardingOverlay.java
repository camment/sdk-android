package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;

import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.helpers.OnboardingPreferences;
import tv.camment.cammentsdk.helpers.Step;

/**
 * Created by petrushka on 21/08/2017.
 */

public class OnboardingOverlay extends RelativeLayout {

    private Map<Step, TooltipView> tooltipViewMap;
    private View recordingButton;
    private View recyclerView;

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
        tooltipViewMap = new HashMap<>();
        OnboardingPreferences.getInstance().initSteps();
    }

    public void setAnchorViews(View recordingButton, View recyclerView) {
        this.recordingButton = recordingButton;
        this.recyclerView = recyclerView;
    }

    public void displayTooltip(Step step) {
        final TooltipView tooltipView = new TooltipView(getContext());
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

        View attachToView;
        switch (step) {
            case RECORD:
            case INVITE:
                attachToView = recordingButton;
                break;
            case PLAY:
            case HIDE:
            case SHOW:
            case DELETE:
            default:
                attachToView = recyclerView;
                break;
        }

        tooltipView.init(attachToView, orientation, step);
        tooltipView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view instanceof TooltipView) {
                    Step step = ((TooltipView) view).getStep();
                    if (step != null) {
                        hideTooltipIfNeeded(step);
                    }
                }
            }
        });

        tooltipViewMap.put(step, tooltipView);

        OnboardingPreferences.getInstance().putOnboardingStepDisplayed(step, true);
    }

    public void hideTooltipIfNeeded(Step step) {
        if (tooltipViewMap != null
                && tooltipViewMap.containsKey(step)) {
            TooltipView tooltipView = tooltipViewMap.get(step);
            if (tooltipView != null) {
                removeView(tooltipView);
            }
        }

        Step nextStep = OnboardingPreferences.getInstance().getNextOnboardingStep();

        if (nextStep != null) {
            switch (nextStep) {
                case HIDE:
                case SHOW:
                    if (CammentProvider.getCammentsSize() > 0) {
                        displayTooltip(nextStep);
                    }
                    break;
                case INVITE:
                    displayTooltip(nextStep);
                    break;
                case DELETE:
                    if (CammentProvider.getCammentsSize() > 0) {
                        displayTooltip(nextStep);
                    }
                    break;
            }
        }
    }

}
