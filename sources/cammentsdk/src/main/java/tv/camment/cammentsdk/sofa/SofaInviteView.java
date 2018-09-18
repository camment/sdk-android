package tv.camment.cammentsdk.sofa;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.PendingActions;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.helpers.AuthHelper;

public class SofaInviteView extends FrameLayout {

    private ImageButton ibInvite;
    private ImageView ivPlus;
    private ProgressBar progressBar;

    public SofaInviteView(Context context) {
        this(context, null);
    }

    public SofaInviteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.cmmsdk_sofa_invite_view, this);

        ibInvite = findViewById(R.id.cmmsdk_ib_invite);
        ibInvite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleInviteUsers();
            }
        });

        ivPlus = findViewById(R.id.cmmsdk_iv_plus);

        progressBar = findViewById(R.id.cmmsdk_progressbar);
    }

    private void handleInviteUsers() {
        CammentSDK.getInstance().disableProgressBar();

        ivPlus.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);

        if (AuthHelper.getInstance().isLoggedIn()) {
            ApiManager.getInstance().getGroupApi().createEmptyUsergroupIfNeededAndGetDeeplink();
        } else {
            if (CammentSDK.getInstance().getCurrentActivity() != null) {
                PendingActions.getInstance().addAction(PendingActions.Action.SHOW_SHARING_OPTIONS);

                CammentSDK.getInstance().getAppAuthIdentityProvider().logIn(CammentSDK.getInstance().getCurrentActivity());
            }
        }
    }

    public void hideProgressView() {
        ivPlus.setVisibility(VISIBLE);
        progressBar.setVisibility(GONE);
    }

}
