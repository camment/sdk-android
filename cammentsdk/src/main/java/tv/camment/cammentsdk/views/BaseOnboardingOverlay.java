package tv.camment.cammentsdk.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.helpers.OnboardingPreferences;
import tv.camment.cammentsdk.helpers.PermissionHelper;
import tv.camment.cammentsdk.helpers.Step;


abstract class BaseOnboardingOverlay extends RelativeLayout
        implements CammentDialog.ActionListener {

    private Map<Step, TooltipView> tooltipViewMap;
    private View recordingButton;
    private View recyclerView;

    BaseOnboardingOverlay(Context context) {
        super(context);
        init();
    }

    BaseOnboardingOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    BaseOnboardingOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    BaseOnboardingOverlay(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        tooltipViewMap = new HashMap<>();
        OnboardingPreferences.getInstance().initSteps();

        if (!OnboardingPreferences.getInstance().wasOnboardingStepShown(Step.RECORD)) {
            BaseMessage message = new BaseMessage();
            message.type = MessageType.ONBOARDING;

            CammentDialog cammentDialog = CammentDialog.createInstance(message);
            cammentDialog.setActionListener(this);
            cammentDialog.show(message.toString());
        }
    }

    void setAnchorViews(View recordingButton, View recyclerView) {
        this.recordingButton = recordingButton;
        this.recyclerView = recyclerView;
    }

    void displayTooltip(Step step) {
        if (tooltipViewMap != null
                && tooltipViewMap.size() > 0) {
            return;
        }

        OnboardingPreferences.getInstance().putOnboardingStepDisplayed(step, true);

        final TooltipView tooltipView = new TooltipView(getContext());
        addView(tooltipView);

        if (step == Step.RECORD
                && !PermissionHelper.getInstance().hasPermissions()) {
            PermissionHelper.getInstance().cameraAndMicTask();
        }

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
    }

    void hideTooltipIfNeeded(Step step) {
        if (tooltipViewMap != null
                && tooltipViewMap.containsKey(step)) {
            TooltipView tooltipView = tooltipViewMap.get(step);
            if (tooltipView != null) {
                removeView(tooltipView);
            }
            tooltipViewMap.remove(step);
        } else {
            return;
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

    @Override
    public void onPositiveButtonClick(BaseMessage baseMessage) {
        if (baseMessage.type == MessageType.ONBOARDING) {
            displayTooltip(Step.RECORD);
        }
    }

    @Override
    public void onNegativeButtonClick(BaseMessage baseMessage) {

    }

    @Override
    public void addView(View child) {
        child.setAlpha(0.0f);
        child.setTranslationY(16);
        super.addView(child);
        child.animate().alpha(1.0f).translationY(0).setDuration(500).start();
    }

    @Override
    public void removeView(View child) {
        child.animate().alpha(1.0f).setDuration(500).start();
        super.removeView(child);
    }

}
