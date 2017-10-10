package tv.camment.cammentdemo;


import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.OnDeeplinkOpenShowListener;

public class ShowNavigator implements OnDeeplinkOpenShowListener {

    @Override
    public void onOpenShowWithUuid(String showUuid) {
        CammentMainActivity.start(CammentSDK.getInstance().getApplicationContext(), showUuid);
    }

}
