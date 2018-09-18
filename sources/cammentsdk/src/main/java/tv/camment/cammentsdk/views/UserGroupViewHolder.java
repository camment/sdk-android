package tv.camment.cammentsdk.views;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.camment.clientsdk.model.Userinfo;

import java.util.concurrent.Executors;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.data.model.CUserGroup;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.helpers.MultiImageHelper;
import tv.camment.cammentsdk.utils.DateTimeUtils;
import tv.camment.cammentsdk.utils.LogUtils;


final class UserGroupViewHolder extends RecyclerView.ViewHolder {

    private final UserGroupAdapter.ActionListener actionListener;
    private CUserGroup usergroup;
    private ImageView ivAvatar;

    private TextView tvGroup;
    private TextView tvTimestamp;

    private MultiImageHelper multiImageHelper;
    private boolean loadAvatars;
    private RequestOptions requestOptions;
    private RequestOptions requestOptionsAll;

    UserGroupViewHolder(final View itemView, final UserGroupAdapter.ActionListener actionListener) {
        super(itemView);

        this.actionListener = actionListener;

        tvGroup = itemView.findViewById(R.id.cmmsdk_tv_group);
        tvTimestamp = itemView.findViewById(R.id.cmmsdk_tv_timestamp);

        ivAvatar = itemView.findViewById(R.id.cmmsdk_iv_avatar);

        itemView.setOnClickListener(itemOnClickListener);
    }

    void bindData(final CUserGroup usergroup) {
        if (usergroup == null || usergroup.getUsers() == null)
            return;

        loadAvatars = true;

        if (this.usergroup != null
                && this.usergroup.getUsers() != null
                && usergroup.getUsers() != null
                && this.usergroup.getUsers().hashCode() == usergroup.getUsers().hashCode()) {
            loadAvatars = false;
        }

        this.usergroup = usergroup;

        itemView.setBackgroundColor(usergroup.isActive()
                ? itemView.getResources().getColor(R.color.cmmsdk_camment_darker_grey)
                : itemView.getResources().getColor(R.color.cmmsdk_camment_lightest_grey));

        String identityId = IdentityPreferences.getInstance().getIdentityId();

        String groupName = "";

        multiImageHelper = new MultiImageHelper(Executors.newSingleThreadExecutor(), usergroup.getUsers().size());

        int index = 0;
        for (final Userinfo userinfo : usergroup.getUsers()) {
            if (usergroup.getUsers().size() == 3 || !TextUtils.equals(userinfo.getUserCognitoIdentityId(), identityId)) {
                loadBitmap(userinfo.getPicture(), index);
                index++;
            }

            if (!TextUtils.equals(userinfo.getUserCognitoIdentityId(), identityId) && !TextUtils.isEmpty(userinfo.getName())) {
                if (!TextUtils.isEmpty(groupName)) {
                    groupName += ", ";
                }

                String[] split = userinfo.getName().split(" ");

                if (split.length > 0 && usergroup.getUsers().size() > 2) {
                    groupName += split[0];
                } else {
                    groupName += userinfo.getName();
                }
            }
        }

        if (TextUtils.isEmpty(groupName)
                && usergroup.getUsers() != null && usergroup.getUsers().size() == 1) {
            groupName = usergroup.getUsers().get(0).getName();
            loadBitmap(usergroup.getUsers().get(0).getPicture(), 0);
        }

        tvGroup.setText(groupName);

        tvTimestamp.setText(String.format(CammentSDK.getInstance().getApplicationContext().getString(R.string.cmmsdk_created),
                DateTimeUtils.getLocaleDateForUI(usergroup.getLongTimestamp())));
    }

    private void loadBitmap(String pictureUrl, final int position) {
        if (!loadAvatars)
            return;

        if (requestOptions == null) {
            requestOptions = new RequestOptions().placeholder(R.drawable.cmmsdk_user).error(R.drawable.cmmsdk_user).dontAnimate();
        }

        ivAvatar.setImageResource(R.drawable.cmmsdk_user);

        Glide.with(CammentSDK.getInstance().getApplicationContext())
                .asBitmap().load(pictureUrl)
                .apply(requestOptions)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null) {
                            multiImageHelper.addBitmap(resource, position, getGroupAvatarCallback());
                        }

                        return true;
                    }
                }).submit();
    }

    private CammentCallback<Bitmap> getGroupAvatarCallback() {
        return new CammentCallback<Bitmap>() {
            @Override
            public void onSuccess(Bitmap result) {
                if (result != null) {
                    if (requestOptionsAll == null) {
                        requestOptionsAll = new RequestOptions().placeholder(R.drawable.cmmsdk_user).error(R.drawable.cmmsdk_user).dontAnimate().circleCrop();
                    }

                    Glide.with(CammentSDK.getInstance().getApplicationContext())
                            .load(result)
                            .apply(requestOptionsAll)
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.cmmsdk_user);
                }
            }

            @Override
            public void onException(Exception exception) {
                LogUtils.debug("onException", "getGroupAvatar", exception);
            }
        };
    }

    private final View.OnClickListener itemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (actionListener != null) {
                actionListener.onUserGroupClick(usergroup);
            }
        }
    };

}