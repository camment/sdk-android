package tv.camment.cammentsdk.views;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import com.camment.clientsdk.model.Camment;
import com.camment.clientsdk.model.CammentList;
import com.camment.clientsdk.model.Usergroup;

import tv.camment.cammentsdk.SDKConfig;
import tv.camment.cammentsdk.api.ApiCallManager;
import tv.camment.cammentsdk.api.ApiCallType;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.aws.AWSManager;
import tv.camment.cammentsdk.data.CammentProvider;
import tv.camment.cammentsdk.data.UserGroupProvider;
import tv.camment.cammentsdk.data.model.CCamment;
import tv.camment.cammentsdk.utils.FileUtils;
import tv.camment.cammentsdk.utils.LogUtils;


final class CammentListOnScrollListener extends RecyclerView.OnScrollListener {

    private final LinearLayoutManager layoutManager;

    private boolean isLoading;
    private boolean isLastPage;

    private String lastKey;

    CammentListOnScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        if (!isLoading && !isLastPage) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && totalItemCount >= SDKConfig.CAMMENT_PAGE_SIZE) {
                loadMoreItems();
            }
        }

        if (firstVisibleItemPosition <= 0
                && totalItemCount <= SDKConfig.CAMMENT_PAGE_SIZE) {
            loadMoreItems();
        }
    }

    private void loadMoreItems() {
        final Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        if (usergroup == null
                || TextUtils.isEmpty(usergroup.getUuid())) {
            return;
        }

        isLoading = true;

        ApiManager.getInstance().getCammentApi().getUserGroupCamments(lastKey, getUserGroupCammentsCallback(usergroup.getUuid()));
    }

    private CammentCallback<CammentList> getUserGroupCammentsCallback(final String groupUuid) {
        return new CammentCallback<CammentList>() {
            @Override
            public void onSuccess(CammentList cammentList) {
                LogUtils.debug("onSuccess", "getUserGroupCamments " + cammentList.getLastKey());
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_CAMMENTS, groupUuid.hashCode()); //TODO add hash for lastKey at least

                isLoading = false;

                if (cammentList != null
                        && cammentList.getItems() != null) {
                    lastKey = cammentList.getLastKey();

                    isLastPage = lastKey == null;

                    for (Camment camment : cammentList.getItems()) {
                        if (!FileUtils.getInstance().isLocalVideoAvailable(camment.getUuid())) {
                            AWSManager.getInstance().getS3UploadHelper().preCacheFile(new CCamment(camment), false);
                        }
                    }
                    CammentProvider.insertCamments(cammentList.getItems());
                }
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "getUserGroupCamments", exception);
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_CAMMENTS, groupUuid.hashCode());

                isLoading = false;
            }
        };
    }

}
