package barqsoft.footballscores;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends PreferenceActivity {

    // use classname when logging
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    // use an appcompat delegate to extend appcompat functionality to the preference activity
    private AppCompatDelegate mDelegate;

    /**
     * On create attach the appcompat delegate and init the toolbar elements
     * @param savedInstanceState Bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        // set view to activity_settings layout
        setContentView(R.layout.settings_activity);

        // get toolbar and set it as the support toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // enable the back/home button and hide the default title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // set the toolbar title
        TextView title = (TextView) findViewById(R.id.toolbar_title);
        title.setText(R.string.action_settings);

        // attach a clicklistener to the back button so we can navigate back to the previous screen
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // load the preferences using a helperclass that will create a preference fragment, so we
        //  don't need the deprecated method addPreferencesFromResource in the preferenceactivity
        getFragmentManager().beginTransaction().replace(R.id.container, new AlexandriaPreferenceFragment()).commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    private void setSupportActionBar(Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    private ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    /**
     * Helperclass that will create a preference fragment, so we don't need the deprecated method
     *  addPreferencesFromResource in the preferenceactivity
     */
    public static class AlexandriaPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // bind the api key preference
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_apikey_key)));
        }

        /**
         * Bind a preference changelistener to update the preference
         * @param preference Preference
         */
        private void bindPreferenceSummaryToValue(Preference preference) {

            // set the listener to watch for value changes
            preference.setOnPreferenceChangeListener(this);

            // trigger the listener immediately with the preference's current value
            onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));

            if (preference.getKey().equals(getString(R.string.pref_apikey_key))) {

                Object value = PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "");

                preference.setSummary(value.toString());
            }
        }

        /**
         * Update the preference once its changed
         * @param preference Preference
         * @param value Object
         * @return boolean
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            // set the value of the preference
            preference.setSummary(stringValue);

            // check if the football-data api key was changed, so we should trigger a data refresh
            if (preference.getKey().equals(getString(R.string.pref_apikey_key)) &&
                    stringValue != null &&
                    !stringValue.equals("") &&
                    !Utility.getPreferredApiKey(getActivity()).equals(stringValue)) {

                // check if we have a network connection
                if (Utility.isNetworkAvailable(getActivity())) {

                    // start the football-data service to trigger loading the teams and fixtures
                    Utility.startFootballDataService(getActivity(), stringValue);

                } else {

                    // if without internet, show a notice to the user in the form of a toast
                    Utility.showToast(getActivity(), null, getString(R.string.network_required_notice));
                }
            }

            return true;
        }
    }
}