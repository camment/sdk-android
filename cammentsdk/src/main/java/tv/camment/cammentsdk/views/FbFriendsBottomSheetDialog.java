package tv.camment.cammentsdk.views;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.camment.clientsdk.model.FacebookFriendList;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentCallback;

public class FbFriendsBottomSheetDialog extends BottomSheetDialog implements DialogInterface.OnShowListener {

    private TextView tvCancel;
    private TextView tvDone;
    private RecyclerView rvFriends;
    private FbFriendsAdapter adapter;

    public FbFriendsBottomSheetDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public FbFriendsBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
        init();
    }

    protected FbFriendsBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        setContentView(R.layout.cmmsdk_fb_friends_bottom_sheet);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        setOnShowListener(this);

        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        tvDone = (TextView) findViewById(R.id.tv_done);
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        rvFriends = (RecyclerView) findViewById(R.id.rv_friends);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new FbFriendsAdapter();
        rvFriends.setAdapter(adapter);

        ApiManager.getInstance().getUserApi().getFacebookFriends(getFacebookFriendsCallback());
    }

    private CammentCallback<FacebookFriendList> getFacebookFriendsCallback() {
        return new CammentCallback<FacebookFriendList>() {
            @Override
            public void onSuccess(FacebookFriendList facebookFriendList) {
                if (facebookFriendList != null) {
                    Log.d("onSuccess", "getFacebookFriends");
                    adapter.setFacebookFriends(facebookFriendList.getItems());
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getFacebookFriends", exception);
            }
        };
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        if (dialogInterface instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = (FrameLayout) dialog.findViewById(android.support.design.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet)
                        .setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }
}
