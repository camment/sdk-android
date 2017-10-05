package tv.camment.cammentdemo;


import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.OnDeeplinkShowOpenListener;

public class ShowNavigator implements OnDeeplinkShowOpenListener {

    @Override
    public void onOpenShowWithUuid(String showUuid) {
        CammentMainActivity.start(CammentSDK.getInstance().getApplicationContext(), showUuid);
    }

}
