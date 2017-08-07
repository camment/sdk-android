package tv.camment.cammentsdk.api;

import android.util.Log;

import com.camment.clientsdk.DevcammentClient;
import com.camment.clientsdk.model.Usergroup;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import tv.camment.cammentsdk.asyncclient.CammentAsyncClient;
import tv.camment.cammentsdk.asyncclient.CammentCallback;
import tv.camment.cammentsdk.utils.NoSqlHelper;

/**
 * Created by petrushka on 03/08/2017.
 */

public class GroupApi extends CammentAsyncClient {

    private final DevcammentClient devcammentClient;

    public GroupApi(ExecutorService executorService, DevcammentClient devcammentClient) {
        super(executorService);
        this.devcammentClient = devcammentClient;
    }

    public void createEmptyUsergroup() {
        submitTask(new Callable<Usergroup>() {
            @Override
            public Usergroup call() throws Exception {
                return devcammentClient.usergroupsPost();
            }
        }, createEmptyUsergroupCallback());
    }

    private CammentCallback<Usergroup> createEmptyUsergroupCallback() {
        return new CammentCallback<Usergroup>() {
            @Override
            public void onSuccess(Usergroup usergroup) {
                Log.d("onSuccess", "createEmptyUsergroup " + usergroup.getUuid());
                NoSqlHelper.setActiveGroup(usergroup);
            }

            @Override
            public void onException(Exception exception) {
                Log.e("onException", "createEmptyUsergroup", exception);
            }
        };
    }

}
