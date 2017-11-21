package tv.camment.cammentsdk.views;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import org.greenrobot.eventbus.EventBus;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;

public class FbLogoutFragment extends Fragment {

    private OnSwitchViewListener onSwitchViewListener;

    public static FbLogoutFragment newInstance() {
        return new FbLogoutFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cmmsdk_fragment_fb_logout, container, false);

        ImageButton ibUser = (ImageButton) rootView.findViewById(R.id.cmmsdk_ib_user);
        ibUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleOnUserInfoClick();
            }
        });

        Button btnLogout = (Button) rootView.findViewById(R.id.cmmsdk_btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFbLogout();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnSwitchViewListener) {
            onSwitchViewListener = (OnSwitchViewListener) getParentFragment();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSwitchViewListener = null;
    }

    private void handleOnUserInfoClick() {
        if (onSwitchViewListener != null) {
            onSwitchViewListener.switchToFbUserInfo();
        }
    }

    private void handleFbLogout() {
        CammentSDK.getInstance().getAppAuthIdentityProvider().logOut();

        ApiManager.clearInstance();

        AWSManager.getInstance().getCognitoCachingCredentialsProvider().clear();

        //ApiManager.getInstance().getUserApi().refreshCognitoCredentials();

        DataManager.getInstance().clearDataForUserGroupChange(false);

        EventBus.getDefault().post(new UserGroupChangeEvent());

        CammentSDK.getInstance().connectToIoT();

        if (onSwitchViewListener != null) {
            onSwitchViewListener.hideUserInfoContainer();
        }
    }

}
