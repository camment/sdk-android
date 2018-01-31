package tv.camment.cammentdemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import tv.camment.cammentsdk.CammentAudioVolume;
import tv.camment.cammentsdk.CammentSDK;

public class CammentSettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

                setCammentAudioAdjustment(stringValue);
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void setCammentAudioAdjustment(String volumeValue) {
        if (volumeValue != null) {
            switch (volumeValue) {
                case "0":
                    CammentSDK.getInstance().setCammentAudioVolumeAdjustment(CammentAudioVolume.NO_ADJUSTMENT);
                    break;
                case "1":
                    CammentSDK.getInstance().setCammentAudioVolumeAdjustment(CammentAudioVolume.MILD_ADJUSTMENT);
                    break;
                case "2":
                default:
                    CammentSDK.getInstance().setCammentAudioVolumeAdjustment(CammentAudioVolume.FULL_ADJUSTMENT);
                    break;
            }
        }
    }

    public static class MainPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference releasePreference = findPreference(getString(R.string.key_release_version));
            releasePreference.setSummary(BuildConfig.VERSION_NAME + " (" + BuildConfig.API_ENDPOINT + ", " + BuildConfig.BUILD_TYPE + ")");

            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_adjust_camment_volume)));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CammentSDK.getInstance().getApplicationContext());
            String volumeValue = prefs.getString(getString(R.string.key_adjust_camment_volume), null);

            if (volumeValue == null) {
                SharedPreferences.Editor prefEditor = prefs.edit();

                CammentAudioVolume cammentAudioVolume = CammentSDK.getInstance().getCammentAudioVolumeAdjustment();

                switch (cammentAudioVolume) {
                    case NO_ADJUSTMENT:
                        volumeValue = "0";
                        break;
                    case MILD_ADJUSTMENT:
                        volumeValue = "1";
                        break;
                    case FULL_ADJUSTMENT:
                    default:
                        volumeValue = "2";
                        break;
                }

                prefEditor.putString(getString(R.string.key_adjust_camment_volume), volumeValue);
                prefEditor.commit();

                ListPreference audioListPreference = (ListPreference) super.findPreference(getString(R.string.key_adjust_camment_volume));
                int index = audioListPreference.findIndexOfValue(volumeValue);

                audioListPreference.setSummary(
                        index >= 0
                                ? audioListPreference.getEntries()[index]
                                : null);
            }
        }
    }

}
