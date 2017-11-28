package tv.camment.cammentsdk.views;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.camment.clientsdk.model.Camment;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.R;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.helpers.IdentityPreferences;
import tv.camment.cammentsdk.utils.CommonUtils;
import tv.camment.cammentsdk.utils.FileUtils;


final class CammentViewHolder extends RecyclerView.ViewHolder {

    private final CammentsAdapter.ActionListener actionListener;
    private CCamment camment;

    private ImageView ivThumbnail;
    private TextureView textureView;

    private LinearLayout llSeen;

    private boolean cammentClicked;

    CammentViewHolder(final View itemView, final CammentsAdapter.ActionListener actionListener) {
        super(itemView);

        this.actionListener = actionListener;

        itemView.setPivotX(0);
        itemView.setPivotY(0);

        ivThumbnail = (ImageView) itemView.findViewById(R.id.cmmsdk_iv_thumbnail);

        llSeen = (LinearLayout) itemView.findViewById(R.id.cmmsdk_ll_seen);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleOnItemClick();
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final String cognitoId = IdentityPreferences.getInstance().getIdentityId();
                final String oldCognitoId = IdentityPreferences.getInstance().getOldIdentityId();

                if (actionListener != null) {
                    actionListener.onCammentBottomSheetDisplayed();
                }

                if (camment.getUserCognitoIdentityId() == null) {
                    camment.setUserCognitoIdentityId("");
                }

                if (TextUtils.equals(camment.getUserCognitoIdentityId(), cognitoId)
                        || TextUtils.isEmpty(camment.getUserCognitoIdentityId())
                        || TextUtils.equals(camment.getUserCognitoIdentityId(), oldCognitoId)) { //TODO this should be removed as server should overwrite this in camment
                    CammentBottomSheetDialog dialog = new CammentBottomSheetDialog(itemView.getContext());
                    dialog.setCamment(camment);
                    dialog.show();
                }
                return true;
            }
        });

        setItemViewScale(0.5f);
    }

    private void handleOnItemClick() {
        if (camment != null && actionListener != null) {
            cammentClicked = true;

            if (!camment.isSeen()) {
                CammentProvider.setCammentSeen(camment.getUuid());
            }
            llSeen.setVisibility(View.GONE);

            if (getItemViewScale() == 0.5f) {
                textureView = new SquareTextureView(itemView.getContext());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
                int dp2 = CommonUtils.dpToPx(CammentSDK.getInstance().getApplicationContext(), 2);
                params.setMargins(dp2, dp2, dp2, dp2);
                ((SquareFrameLayout) itemView).addView(textureView, 1, params);
            }
            actionListener.onCammentClick(this, camment, textureView);
        }
    }

    void setItemViewScale(float scale) {
        if (itemView instanceof SquareFrameLayout) {
            if (getItemViewScale() != scale) {
                ((SquareFrameLayout) itemView).setCustomScale(scale);
                if (scale == 0.5f) {
                    setThumbnailVisibility(View.VISIBLE);
                }
                if (textureView != null
                        && scale == 0.5f) {
                    ((SquareFrameLayout) itemView).removeView(textureView);
                }
            }
            cammentClicked = false;
        }
    }

    void stopCammentIfPlaying() {
        if (actionListener != null) {
            actionListener.stopCammentIfPlaying(camment);
        }
    }

    void setThumbnailVisibility(int visibility) {
        ivThumbnail.setVisibility(visibility);
    }

    float getItemViewScale() {
        if (itemView instanceof SquareFrameLayout) {
            return ((SquareFrameLayout) itemView).getCustomScale();
        }
        return 1.0f;
    }

    void bindData(CCamment camment) {
        if (camment == null)
            return;

        this.camment = camment;

        if (FileUtils.getInstance().isLocalVideoAvailable(camment.getUuid())) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(FileUtils.getInstance()
                    .getUploadCammentPath(camment.getUuid()), MediaStore.Video.Thumbnails.MINI_KIND);
            if (bitmap != null) {
                ivThumbnail.setImageBitmap(bitmap);

                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                ivThumbnail.setColorFilter(filter);
            }
        } else {
            loadThumbnailFromServer();
        }

        llSeen.setVisibility(camment.isSeen() ? View.GONE : View.VISIBLE);
    }

    private void loadThumbnailFromServer() {
        Glide.with(CammentSDK.getInstance().getApplicationContext())
                .load(camment.getThumbnail())
                .into(ivThumbnail);
    }

}