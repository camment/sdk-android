package tv.camment.cammentsdk.views;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.camment.clientsdk.model.Usergroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import tv.camment.cammentsdk.BuildConfig;
import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.PendingActions;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.auth.CammentUserInfo;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.AuthHelper;

public class FbUserFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private ImageView ivAvatar;
    private TextView tvName;
    private Button btnInvite;

    private OnSwitchViewListener onSwitchViewListener;

    public static FbUserFragment newInstance() {
        return new FbUserFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cmmsdk_fragment_fb_user, container, false);

        ivAvatar = (ImageView) rootView.findViewById(R.id.cmmsdk_iv_avatar);
        tvName = (TextView) rootView.findViewById(R.id.cmmsdk_tv_name);

        ImageButton ibSettings = (ImageButton) rootView.findViewById(R.id.cmmsdk_ib_settings);
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleOnSettingsClick();
            }
        });

        if (BuildConfig.SHOW_GROUP_LIST) {
            ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onSwitchViewListener != null) {
                        onSwitchViewListener.switchGroupContainer();
                    }
                }
            });
        }

        btnInvite = (Button) rootView.findViewById(R.id.cmmsdk_btn_invite);
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleInviteUsers();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fillFbInfo();

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
            return null;
        } else {
            return UserInfoProvider.getUserInfoLoader(activeUserGroup.getUuid());
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<CUserInfo> userInfos = UserInfoProvider.listFromCursor(data);

        boolean inviteBtnVisible = userInfos != null && userInfos.size() > 0;

        btnInvite.setVisibility(inviteBtnVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void fillFbInfo() {
        CammentUserInfo userInfo = CammentSDK.getInstance().getAppAuthIdentityProvider().getUserInfo();

        if (userInfo != null) {

            Glide.with(CammentSDK.getInstance().getApplicationContext()).asBitmap().load(userInfo.getImageUrl()).into(new BitmapImageViewTarget(ivAvatar) {
                @Override
                protected void setResource(Bitmap resource) {
                    if (getContext() != null) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivAvatar.setImageDrawable(circularBitmapDrawable);
                    }
                }
            });

            tvName.setText(userInfo.getName());
        }
    }

    private void handleOnSettingsClick() {
        if (onSwitchViewListener != null) {
            onSwitchViewListener.switchToFbUserLogout();
        }
    }

    private void handleInviteUsers() {
        if (AuthHelper.getInstance().isLoggedIn()) {
            ApiManager.getInstance().getGroupApi().createEmptyUsergroupIfNeededAndGetDeeplink();
        } else {
            if (getContext() instanceof Activity) {
                PendingActions.getInstance().addAction(PendingActions.Action.SHOW_SHARING_OPTIONS);

                CammentSDK.getInstance().getAppAuthIdentityProvider().logIn((Activity) getContext());
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserGroupChangeEvent event) {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        int count = 0;
        if (activeUserGroup != null) {
            count = UserInfoProvider.getConnectedUsersCountByGroupUuid(activeUserGroup.getUuid());
        }
        btnInvite.setVisibility(count > 0 ? View.VISIBLE : View.GONE);

        if (getContext() instanceof AppCompatActivity) {
            getLoaderManager().destroyLoader(1);
            getLoaderManager().initLoader(1, null, this);
        }
    }

}
