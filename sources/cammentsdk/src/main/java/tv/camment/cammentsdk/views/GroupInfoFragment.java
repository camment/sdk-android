package tv.camment.cammentsdk.views;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.camment.clientsdk.model.Usergroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.PendingActions;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.aws.messages.MessageType;
import tv.camment.cammentsdk.aws.messages.UserBlockMessage;
import tv.camment.cammentsdk.aws.messages.UserUnblockMessage;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.data.model.CUserGroup;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.views.dialogs.UserBlockDialog;
import tv.camment.cammentsdk.views.dialogs.UserUnblockDialog;


public final class GroupInfoFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, UserInfoAdapter.ActionListener {

    private static final int LOADER_USERINFO = 1;
    private static final int LOADER_USERGROUP = 2;

    private RecyclerView rvGroups;
    private UserInfoAdapter adapter;

    private TextView tvShowName;

    private OnDrawerViewListener onDrawerViewListener;

    public static GroupInfoFragment newInstance() {
        return new GroupInfoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.cmmsdk_fragment_group_info, container, false);
        rvGroups = layout.findViewById(R.id.cmmsdk_rv_groups_info);

        tvShowName = layout.findViewById(R.id.cmmsdk_tv_show_name);

        ImageButton ibBack = layout.findViewById(R.id.cmmsdk_ib_back);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDrawerViewListener != null) {
                    onDrawerViewListener.switchGroupContainer();
                }
            }
        });

        Button btnInvite = layout.findViewById(R.id.cmmsdk_btn_invite);
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleInviteUsers();
            }
        });

        setupRecyclerView();

        return layout;
    }

    private void setupRecyclerView() {
        adapter = new UserInfoAdapter(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvGroups.setLayoutManager(layoutManager);
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(null);
        rvGroups.setHasFixedSize(true);
    }

    private void handleInviteUsers() {
        if (AuthHelper.getInstance().isLoggedIn()) {
            ApiManager.getInstance().getGroupApi().createEmptyUsergroupIfNeededAndGetDeeplink();
        } else {
            if (CammentSDK.getInstance().getCurrentActivity() != null) {
                PendingActions.getInstance().addAction(PendingActions.Action.SHOW_SHARING_OPTIONS);

                CammentSDK.getInstance().getAppAuthIdentityProvider().logIn(CammentSDK.getInstance().getCurrentActivity());
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getLoaderManager().initLoader(LOADER_USERINFO, null, this); //TODO loaders can be replaced by 1
        getLoaderManager().initLoader(LOADER_USERGROUP, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof
                OnDrawerViewListener) {
            onDrawerViewListener = (OnDrawerViewListener) getParentFragment();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onDrawerViewListener = null;
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_USERINFO) {
            Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

            return UserInfoProvider.getUserInfoLoader(activeUserGroup == null ? "" : activeUserGroup.getUuid());
        } else {
            return UserGroupProvider.getActiveUserGroupLoader();
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_USERINFO) {
            String identityId = IdentityPreferences.getInstance().getIdentityId();
            Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

            List<CUserInfo> userInfos = UserInfoProvider.listFromCursor(data);
            adapter.setData(userInfos,
                    activeUserGroup != null && TextUtils.equals(identityId, activeUserGroup.getUserCognitoIdentityId()));
        } else {
            CUserGroup userGroup = UserGroupProvider.oneFromCursor(data);

            if (userGroup != null) {
                adapter.setHostId(userGroup.getHostId());

                tvShowName.setText(userGroup.getIsPublic() ? getString(R.string.cmmsdk_special_host) : getString(R.string.cmmsdk_camment_chat));
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserGroupChangeEvent event) {
        if (adapter != null) {
            adapter.setData(null, false);
            adapter.setHostId(null);
        }

        getLoaderManager().destroyLoader(LOADER_USERINFO);
        getLoaderManager().initLoader(LOADER_USERINFO, null, this);

        getLoaderManager().destroyLoader(LOADER_USERGROUP);
        getLoaderManager().initLoader(LOADER_USERGROUP, null, this);
    }

    @Override
    public void onUserBlockClick(final CUserInfo userInfo) {
        if (userInfo.getUserState() != null) {
            switch (userInfo.getUserState()) {
                case ACTIVE:
                    displayBlockConfirmationDialog(userInfo);
                    break;
                case BLOCKED:
                    displayUnblockConfirmationDialog(userInfo);
                    break;
                case UNDEFINED:
                default:
                    break;
            }
        }
    }

    private void displayBlockConfirmationDialog(final CUserInfo userInfo) {
        UserBlockMessage message = new UserBlockMessage();
        message.type = MessageType.BLOCK_CONFIRMATION;
        UserBlockMessage.Body body = new UserBlockMessage.Body();
        body.name = userInfo.getName();
        message.body = body;

        UserBlockDialog.createInstance(message, userInfo).show();
    }

    private void displayUnblockConfirmationDialog(final CUserInfo userInfo) {
        UserUnblockMessage message = new UserUnblockMessage();
        message.type = MessageType.UNBLOCK_CONFIRMATION;
        UserUnblockMessage.Body body = new UserUnblockMessage.Body();
        body.name = userInfo.getName();
        message.body = body;

        UserUnblockDialog.createInstance(message, userInfo).show();
    }

}
