package tv.camment.cammentsdk.views;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.camment.clientsdk.model.FacebookFriendList;

import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.messages.BaseMessage;
import tv.camment.cammentsdk.aws.messages.MessageType;

public class FbFriendsBottomSheetDialog extends BottomSheetDialog implements DialogInterface.OnShowListener {

    private Button btnCancel;
    private Button btnDone;
    private RecyclerView rvFriends;
    private FbFriendsAdapter adapter;

    public FbFriendsBottomSheetDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FbFriendsBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
        init(context);
    }

    protected FbFriendsBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }


    private void init(Context context) {
        if (context instanceof Activity) {
            setOwnerActivity((Activity) context);
        }
        setContentView(R.layout.cmmsdk_fb_friends_bottom_sheet);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        setOnShowListener(this);

        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApiManager.getInstance().getInvitationApi().sendInvitation(adapter.getSelectedFacebookFriends(), sendInvitationCallback());
                dismiss();
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

    private CammentCallback<Object> sendInvitationCallback() {
        return new CammentCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Log.d("onSuccess", "sendInvitation");
                if (getOwnerActivity() instanceof AppCompatActivity) {
                    BaseMessage baseMessage = new BaseMessage();
                    baseMessage.type = MessageType.INVITATION_SENT;

                    Fragment fragment = getOwnerActivity().getFragmentManager().findFragmentByTag(baseMessage.toString());
                    if (fragment == null || !fragment.isAdded()) {
                        CammentDialog cammentDialog = CammentDialog.createInstance(baseMessage);
                        cammentDialog.show(((AppCompatActivity) getOwnerActivity()).getSupportFragmentManager(), baseMessage.toString());
                    }
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "sendInvitation", exception);
            }
        };
    }

}
