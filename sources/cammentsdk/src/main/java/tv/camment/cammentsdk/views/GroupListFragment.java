package tv.camment.cammentsdk.views;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.camment.clientsdk.model.Usergroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.PendingActions;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.ShowMetadata;
import tv.camment.cammentsdk.api.ApiCallType;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CUserGroup;
import tv.camment.cammentsdk.events.ApiCalledEvent;
import tv.camment.cammentsdk.events.ApiResultEvent;
import tv.camment.cammentsdk.events.HideUserInfoContainerEvent;
import tv.camment.cammentsdk.events.LoginStatusChangedEvent;
import tv.camment.cammentsdk.events.ShowMetadataSetEvent;
import tv.camment.cammentsdk.events.TutorialContinueEvent;
import tv.camment.cammentsdk.events.TutorialSkippedEvent;
import tv.camment.cammentsdk.events.UserGroupChangeEvent;
import tv.camment.cammentsdk.helpers.AuthHelper;
import tv.camment.cammentsdk.helpers.OnboardingPreferences;


public final class GroupListFragment extends Fragment
        implements UserGroupAdapter.ActionListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_USERGROUP = 1;

    private RelativeLayout rlInvite;
    private LinearLayout llGroups;

    private Button btnCreateGroup;

    private Button btnContinueTutorial;

    private RecyclerView rvGroups;
    private UserGroupAdapter adapter;
    private ContentLoadingProgressBar contentLoadingProgressBar;

    private OnDrawerViewListener onDrawerViewListener;

    public static GroupListFragment newInstance() {
        return new GroupListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.cmmsdk_fragment_group_list, container, false);

        rlInvite = layout.findViewById(R.id.cmmsdk_rl_invite);
        llGroups = layout.findViewById(R.id.cmmsdk_ll_groups);

        btnCreateGroup = layout.findViewById(R.id.cmmsdk_btn_create_group);
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiManager.getInstance().getGroupApi().createEmptyUsergroup();
            }
        });

        Button btnInvite = layout.findViewById(R.id.cmmsdk_btn_invite);
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleInviteUsers();
            }
        });

        btnContinueTutorial = layout.findViewById(R.id.cmmsdk_btn_continue_tutorial);
        btnContinueTutorial.setVisibility(OnboardingPreferences.getInstance().wasTutorialSkipped() ? View.VISIBLE : View.GONE);
        btnContinueTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnboardingPreferences.getInstance().setTutorialSkipped(false);
                EventBus.getDefault().post(new TutorialContinueEvent());
                btnContinueTutorial.setVisibility(View.GONE);
            }
        });

        contentLoadingProgressBar = layout.findViewById(R.id.cmmsdk_cl_progressbar);
        contentLoadingProgressBar.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(android.R.color.holo_blue_dark),
                        PorterDuff.Mode.SRC_IN);

        rvGroups = layout.findViewById(R.id.cmmsdk_rv_groups);

        setupRecyclerView();

        return layout;
    }

    private void setupRecyclerView() {
        adapter = new UserGroupAdapter(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvGroups.setLayoutManager(layoutManager);
        rvGroups.setAdapter(adapter);
        rvGroups.setItemAnimator(null);
        rvGroups.setHasFixedSize(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (AuthHelper.getInstance().isLoggedIn()) {
            displayFbSignedInUser();
        } else {
            hideFbUserView();
        }

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

    @Override
    public void onUserGroupClick(CUserGroup userGroup) {
        if (onDrawerViewListener != null) {
            onDrawerViewListener.switchGroupContainer();
        }

        Usergroup activeUserGroup = UserGroupProvider.getActiveUserGroup();

        if (activeUserGroup != null
                && TextUtils.equals(activeUserGroup.getUuid(), userGroup.getUuid())) {
            return;
        }

        if (activeUserGroup != null) {
            UserGroupProvider.setActive(activeUserGroup.getUuid(), false);
        }

        UserGroupProvider.setActive(userGroup.getUuid(), true);

        EventBus.getDefault().post(new UserGroupChangeEvent());

        ShowMetadata showMetadata = CammentSDK.getInstance().getShowMetadata();

        if (showMetadata != null) {
            ApiManager.getInstance().getInvitationApi().sendInvitationForDeeplink(userGroup.getUuid(), showMetadata.getUuid(), false);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (CammentSDK.getInstance().getShowMetadata() != null
                && !TextUtils.isEmpty(CammentSDK.getInstance().getShowMetadata().getUuid())
                && AuthHelper.getInstance().isLoggedIn()) {
            return UserGroupProvider.getUserGroupLoaderByShowUuid(CammentSDK.getInstance().getShowMetadata().getUuid());
        } else {
            return UserGroupProvider.getUserGroupLoaderByShowUuid("dummy_uuid");
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        List<CUserGroup> userGroups = AuthHelper.getInstance().isLoggedIn() ? UserGroupProvider.listFromCursorWithInfo(data) : null;
        adapter.setData(userGroups);

        if (userGroups == null || userGroups.size() == 0) {
            rlInvite.setVisibility(View.VISIBLE);
            llGroups.setVisibility(View.GONE);
            btnCreateGroup.setVisibility(View.GONE);
        } else {
            rlInvite.setVisibility(View.GONE);
            llGroups.setVisibility(View.VISIBLE);
            btnCreateGroup.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ShowMetadataSetEvent event) {
        getLoaderManager().destroyLoader(LOADER_USERGROUP);
        getLoaderManager().initLoader(LOADER_USERGROUP, null, this);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TutorialSkippedEvent event) {
        if (btnContinueTutorial != null) {
            btnContinueTutorial.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginStatusChangedEvent event) {
        if (AuthHelper.getInstance().isLoggedIn()) {
            ApiManager.getInstance().getUserApi().getMyUserGroups();
        }

        if (AuthHelper.getInstance().isLoggedIn()
                && !isFbUserViewDisplayed()) {
            displayFbSignedInUser();
        }

        getLoaderManager().destroyLoader(LOADER_USERGROUP);
        getLoaderManager().initLoader(LOADER_USERGROUP, null, this);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(HideUserInfoContainerEvent event) {
        hideFbUserView();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ApiCalledEvent event) {
        if (event.getApiCallType() == ApiCallType.GET_MY_USER_GROUPS
                && contentLoadingProgressBar != null) {
            contentLoadingProgressBar.show();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ApiResultEvent event) {
        if (event.getApiCallType() == ApiCallType.GET_MY_USER_GROUPS
                && contentLoadingProgressBar != null) {
            contentLoadingProgressBar.hide();
        }
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

    private void displayFbSignedInUser() {
        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().replace(R.id.cmmsdk_user_container, FbUserFragment.newInstance()).commit();
    }

    private void hideFbUserView() {
        FragmentManager fm = getChildFragmentManager();
        Fragment fragmentById = fm.findFragmentById(R.id.cmmsdk_user_container);
        if (fragmentById != null) {
            fm.beginTransaction().remove(fragmentById).commit();
        }
    }

    private boolean isFbUserViewDisplayed() {
        FragmentManager fm = getChildFragmentManager();
        Fragment fragmentById = fm.findFragmentById(R.id.cmmsdk_user_container);
        return fragmentById != null;
    }

}
