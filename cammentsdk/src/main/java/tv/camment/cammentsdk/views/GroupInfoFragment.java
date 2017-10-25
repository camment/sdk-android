package tv.camment.cammentsdk.views;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.camment.clientsdk.model.Usergroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.UserInfoProvider;
import tv.camment.cammentsdk.data.model.CUserGroup;
import tv.camment.cammentsdk.data.model.CUserInfo;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.FacebookHelper;


public final class GroupInfoFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView rvGroups;
    private UserInfoAdapter adapter;

    private RelativeLayout rlInvite;
    private RelativeLayout rlGroupInfo;

    public static GroupInfoFragment newInstance() {
        return new GroupInfoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.cmmsdk_fragment_group_info, container, false);

        rvGroups = (RecyclerView) layout.findViewById(R.id.cmmsdk_rv_groups_info);

        setupRecyclerView();

        rlInvite = (RelativeLayout) layout.findViewById(R.id.cmmsdk_rl_invite);
        rlGroupInfo = (RelativeLayout) layout.findViewById(R.id.cmmsdk_rl_group_info);

        handleContainersVisibility();

        TextView tvLearnMore = (TextView) layout.findViewById(R.id.cmmsdk_tv_learn_more);
        tvLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCammentWebSite();
            }
        });

        Button btnInvite = (Button) layout.findViewById(R.id.cmmsdk_btn_invite);
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleInviteUsers();
            }
        });

        return layout;
    }

    private void setupRecyclerView() {
        adapter = new UserInfoAdapter();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvGroups.setLayoutManager(layoutManager);
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(null);
        rvGroups.setHasFixedSize(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getLoaderManager().initLoader(1, null, this);

        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup != null) {
            ApiManager.getInstance().getUserApi().getUserInfosForGroupUuid(activeUserGroup.getUuid());
        }
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
        handleContainersVisibility();

        List<CUserInfo> userInfos = UserInfoProvider.listFromCursor(data);
        adapter.setData(userInfos);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void handleContainersVisibility() {
        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        int count = 0;
        if (activeUserGroup != null) {
            count = UserInfoProvider.getConnectedUsersCountByGroupUuid(activeUserGroup.getUuid());
        }
        rlInvite.setVisibility(activeUserGroup == null || count == 0 ? View.VISIBLE : View.GONE);
        rlGroupInfo.setVisibility(activeUserGroup == null || count == 0 ? View.GONE : View.VISIBLE);
    }

    private void openCammentWebSite() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://camment.tv/"));
        startActivity(intent);
    }

    private void handleInviteUsers() {
        if (FacebookHelper.getInstance().isLoggedIn()) {
            ApiManager.getInstance().getGroupApi().createEmptyUsergroupIfNeededAndGetDeeplink();
        } else {
            FacebookHelper.getInstance().logIn(getActivity(), true);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserGroupChangeEvent event) {
        handleContainersVisibility();

        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();
        if (activeUserGroup != null) {
            ApiManager.getInstance().getUserApi().getUserInfosForGroupUuid(activeUserGroup.getUuid());
        }

        if (adapter != null) {
            adapter.setData(null);
        }

        if (getContext() instanceof AppCompatActivity) {
            getLoaderManager().destroyLoader(1);
            getLoaderManager().initLoader(1, null, this);
        }
    }

}
