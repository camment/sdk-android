package tv.camment.cammentsdk.views;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.camment.clientsdk.model.Usergroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.data.DataManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.views.dialogs.LeaveDialog;

public class FbLogoutFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Button btnLeave;

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

        btnLeave = (Button) rootView.findViewById(R.id.cmmsdk_btn_leave);
        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLeaveGroup();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getLoaderManager().initLoader(1, null, this);
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

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup == null) {
            btnLeave.setVisibility(View.GONE);
            return null;
        } else {
            return UserInfoProvider.getUserInfoLoader(activeUserGroup.getUuid());
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String identityId = IdentityPreferences.getInstance().getIdentityId();
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

        boolean isMyGroup = activeUserGroup != null && TextUtils.equals(identityId, activeUserGroup.getUserCognitoIdentityId());
        btnLeave.setVisibility(activeUserGroup == null || isMyGroup ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void handleOnUserInfoClick() {
        if (onSwitchViewListener != null) {
            onSwitchViewListener.switchToFbUserInfo();
        }
    }

    private void handleFbLogout() {
        CammentSDK.getInstance().getAppAuthIdentityProvider().logOut();

        ApiManager.clearInstance();

        AWSManager.getInstance().getCognitoCachingCredentialsProvider().clearCredentials();

        AWSManager.getInstance().getCognitoCachingCredentialsProvider().clear();

        DataManager.getInstance().clearDataForLogOut();

        EventBus.getDefault().post(new UserGroupChangeEvent());

        AWSManager.getInstance().getIoTHelper().subscribe();

        if (onSwitchViewListener != null) {
            onSwitchViewListener.hideUserInfoContainer();
        }
    }

    private void handleLeaveGroup() {
        BaseMessage message = new BaseMessage();
        message.type = MessageType.LEAVE_CONFIRMATION;

        LeaveDialog.createInstance(message).show();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserGroupChangeEvent event) {
        if (getContext() instanceof AppCompatActivity) {
            getLoaderManager().destroyLoader(1);
            getLoaderManager().initLoader(1, null, this);
        }
    }

}
