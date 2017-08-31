package tv.camment.cammentdemo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.camment.clientsdk.model.Show;

import java.util.List;

import tv.camment.cammentsdk.api.ApiManager;
import tv.camment.cammentsdk.data.ShowProvider;
import tv.camment.cammentsdk.helpers.GeneralPreferences;
import tv.camment.cammentsdk.views.CammentDialog;

public class CammentShowsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, CammentShowsAdapter.ActionListener, CammentPasscodeDialog.ActionListener {

    private RecyclerView rvShows;
    private CammentShowsAdapter adapter;

    public static void startClearHistory(Context context) {
        Intent intent = new Intent(context, CammentShowsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camment_activity_camment_shows);

        rvShows = (RecyclerView) findViewById(R.id.rv_shows);

        adapter = new CammentShowsAdapter(this);
        LinearLayoutManager layoutManager = new CammentPrecachingLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvShows.setLayoutManager(layoutManager);
        rvShows.setAdapter(adapter);
        rvShows.setItemAnimator(null);

        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            ApiManager.getInstance().getShowApi().getShows("");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ShowProvider.getShowLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<Show> showList = ShowProvider.listFromCursor(data);
        adapter.setData(showList);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.camment_menu_shows, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mi_passcode) {

            Fragment fragment = getSupportFragmentManager().findFragmentByTag("PasscodeDialog");

            if (fragment == null || !fragment.isAdded()) {
                CammentPasscodeDialog passcodeDialog = CammentPasscodeDialog.createInstance();
                passcodeDialog.setActionListener(this);
                passcodeDialog.show(getSupportFragmentManager(), "PasscodeDialog");
            }
        }
        return true;
    }

    @Override
    public void onShowClick(Show show) {
        CammentMainActivity.start(this, show.getUuid());

        overridePendingTransition(R.anim.camment_slide_in_right, R.anim.camment_slide_out_left);
    }

    @Override
    public void onPositiveButtonClick(String passcode) {
        ApiManager.getInstance().getShowApi().getShows(passcode);
    }

}
