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
    private OnCammentLoadingMoreListener onCammentLoadingMoreListener;

    private boolean isLoading;
    private boolean isLastPage;

    private String lastKey;

    CammentListOnScrollListener(LinearLayoutManager layoutManager, OnCammentLoadingMoreListener onCammentLoadingMoreListener) {
        this.layoutManager = layoutManager;
        this.onCammentLoadingMoreListener = onCammentLoadingMoreListener;
        loadMoreItems();
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
                if (loadMoreItems()) {
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (lastKey != null
                                    && onCammentLoadingMoreListener != null) {
                                onCammentLoadingMoreListener.onCammentLoadingMoreStarted();
                            }
                        }
                    });
                }
            }
        }
    }

    boolean loadMoreItems() {
        final Usergroup usergroup = UserGroupProvider.getActiveUserGroup();

        if (usergroup == null
                || TextUtils.isEmpty(usergroup.getUuid())) {
            return false;
        }

        if (layoutManager != null
                && layoutManager.getItemCount() > 0
                && lastKey == null) {
            return false;
        }

        LogUtils.debug("getUserGroupCamments", "GET for uuid " + usergroup.getUuid() + " lastKey " + lastKey);

        isLoading = ApiManager.getInstance().getCammentApi().getUserGroupCamments(lastKey, getUserGroupCammentsCallback(usergroup.getUuid()));

        return isLoading;
    }

    private CammentCallback<CammentList> getUserGroupCammentsCallback(final String groupUuid) {
        return new CammentCallback<CammentList>() {
            @Override
            public void onSuccess(CammentList cammentList) {
                LogUtils.debug("onSuccess1", "getUserGroupCamments " + cammentList.getLastKey());
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_CAMMENTS, groupUuid.hashCode() + (lastKey == null ? 0 : lastKey.hashCode()));

                if (onCammentLoadingMoreListener != null) {
                    onCammentLoadingMoreListener.onCammentLoadingMoreFinished();
                }

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
                Log.e("onException1", "getUserGroupCamments", exception);
                ApiCallManager.getInstance().removeCall(ApiCallType.GET_CAMMENTS, groupUuid.hashCode() + (lastKey == null ? 0 : lastKey.hashCode()));

                if (onCammentLoadingMoreListener != null) {
                    onCammentLoadingMoreListener.onCammentLoadingMoreFinished();
                }

                isLoading = false;
            }
        };
    }

    interface OnCammentLoadingMoreListener {

        void onCammentLoadingMoreStarted();

        void onCammentLoadingMoreFinished();

    }

}
