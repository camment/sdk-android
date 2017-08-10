package tv.camment.cammentsdk.views;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.camment.clientsdk.model.FacebookFriend;
import com.facebook.internal.ImageRequest;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;


class FbFriendViewHolder extends RecyclerView.ViewHolder {

    private final ActionListener actionListener;
    private FacebookFriend facebookFriend;

    private ImageView ivAvatar;
    private TextView tvName;
    private CheckBox cbSelect;

    FbFriendViewHolder(final View itemView, final ActionListener actionListener) {
        super(itemView);

        this.actionListener = actionListener;

        ivAvatar = itemView.findViewById(R.id.iv_avatar);
        tvName = itemView.findViewById(R.id.tv_name);
        cbSelect = itemView.findViewById(R.id.cb_select);

        cbSelect.setOnCheckedChangeListener(checkedChangeListener);
    }

    void bindData(FacebookFriend facebookFriend) {
        if (facebookFriend == null)
            return;

        this.facebookFriend = facebookFriend;

        tvName.setText(facebookFriend.getName());

        Uri pictureUri = ImageRequest.getProfilePictureUri(String.valueOf(facebookFriend.getId().longValue()), 270, 270);

        Glide.with(CammentSDK.getInstance().getApplicationContext()).asBitmap().load(pictureUri).into(new BitmapImageViewTarget(ivAvatar) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(itemView.getContext().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                ivAvatar.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (actionListener != null) {
                actionListener.onFbFriendClick(facebookFriend);
            }
        }
    };

    interface ActionListener {

        void onFbFriendClick(FacebookFriend facebookFriend);

    }

}
