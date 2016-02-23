package com.nanodegree.udacity.roshanrai.alexandria;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
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
        setContentView(R.layout.activity_settings);

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
        title.setText(R.string.settings);

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
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

}
