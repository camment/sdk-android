package tv.camment.cammentdemo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.camment.clientsdk.model.Show;

import net.hockeyapp.android.UpdateManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import tv.camment.cammentsdk.CammentSDK;
import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.data.ShowProvider;
import tv.camment.cammentsdk.events.IoTStatusChangeEvent;
import tv.camment.cammentsdk.helpers.GeneralPreferences;

public class CammentShowsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        CammentShowsAdapter.ActionListener,
        CammentPasscodeDialog.ActionListener {

    private CammentShowsAdapter adapter;
    private LinearLayout llEmpty;
    private TextView tvConnection;

    public static void startClearHistory(Context context) {
        Intent intent = new Intent(context, CammentShowsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camment_activity_camment_shows);

        RecyclerView rvShows = (RecyclerView) findViewById(R.id.rv_shows);

        Button btnPasscode = (Button) findViewById(R.id.btn_passcode);
        btnPasscode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPasscodeClick();
            }
        });

        llEmpty = (LinearLayout) findViewById(R.id.ll_empty);

        tvConnection = (TextView) findViewById(R.id.tv_connection);

        adapter = new CammentShowsAdapter(this);
        LinearLayoutManager layoutManager = new CammentPrecachingLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvShows.setLayoutManager(layoutManager);
        rvShows.setAdapter(adapter);
        rvShows.setItemAnimator(null);

        getSupportLoaderManager().initLoader(1, null, this);

        checkForUpdates();
    }

    private void onPasscodeClick() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("PasscodeDialog");

        if (fragment == null || !fragment.isAdded()) {
            CammentPasscodeDialog passcodeDialog = CammentPasscodeDialog.createInstance();
            passcodeDialog.setActionListener(this);
            passcodeDialog.show(getSupportFragmentManager(), "PasscodeDialog");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkInternetConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isConnected = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected();

        if (!isConnected) {
            Toast.makeText(this, R.string.camment_no_network, Toast.LENGTH_LONG).show();
        }
    }

    private void checkForUpdates() {
        if (BuildConfig.USE_HOCKEYAPP) {
            UpdateManager.register(this);
        }
    }

    private void unregisterManagers() {
        if (!BuildConfig.USE_HOCKEYAPP) {
            UpdateManager.unregister();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            ApiManager.getInstance().getShowApi().getShows(GeneralPreferences.getInstance().getProviderPasscode());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ShowProvider.getShowLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<Show> showList = ShowProvider.listFromCursor(data);

        if (showList == null || showList.size() == 0) {
            llEmpty.setVisibility(View.VISIBLE);
        } else {
            llEmpty.setVisibility(View.GONE);
        }

        adapter.setData(showList);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onShowClick(Show show) {
        CammentMainActivity.start(this, show.getUuid());

        overridePendingTransition(R.anim.camment_slide_in_right, R.anim.camment_slide_out_left);
    }

    @Override
    public void onPositiveButtonClick(String passcode) {
        GeneralPreferences.getInstance().setProviderPasscode(passcode);
        ApiManager.getInstance().getShowApi().getShows(passcode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CammentSDK.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(IoTStatusChangeEvent event) {
        Log.d("EVENT", event.getStatus().name());
        if (event != null
                && tvConnection != null) {
            int textResId = R.string.camment_disconnected;

            if (event.getStatus() != null) {
                switch (event.getStatus()) {
                    case Connected:
                        textResId = R.string.camment_connected;
                        break;
                    case Connecting:
                        textResId = R.string.camment_connecting;
                        break;
                    case ConnectionLost:
                        textResId = R.string.camment_connection_lost;
                        break;
                    case Reconnecting:
                        textResId = R.string.camment_reconnecting;
                        break;
                }
            }
            tvConnection.setText(textResId);
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

}
