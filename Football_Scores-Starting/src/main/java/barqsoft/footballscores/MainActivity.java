package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    // tag logging with classname
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // the mainfragment containing the viewpager
    private MainFragment mMainFragment;

    // keep the currently selected fragment (page)
    public static int mCurrentFragment = 2;

    // key constants for keeping state
    private static final String PAGER_CURRENT_ITEM_KEY = "pager_current_item";
    private static final String MAIN_FRAGMENT_KEY = "main_fragment";

    /**
     * Set the main_activity layout and add the pagerfragment to the container
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        // set a toolbar as supportactionbar, with default title homebutton disabled
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            // get the toolbar title view and set the title
            TextView titleView = (TextView) findViewById(R.id.toolbar_title);
            titleView.setText(R.string.app_name);
        }

        if (savedInstanceState == null) {

            // create the mainfragment and pass on optionally received extras from collection widget
            mMainFragment = new MainFragment();
            mMainFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mMainFragment)
                    .commit();
        }
    }

    /**
     * Save the pagerfragment and currentpage to the outstate
     * @param outState Bundle
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        // save current page to outstate bundle
        outState.putInt(PAGER_CURRENT_ITEM_KEY, mMainFragment.mViewPager.getCurrentItem());

        // save a reference to the mainfragment to the outstate bundle
        getSupportFragmentManager().putFragment(outState, MAIN_FRAGMENT_KEY, mMainFragment);

        super.onSaveInstanceState(outState);
    }

    /**
     * Restore the pagerfragment and currentpage from the savedstate
     * @param savedState Bundle
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedState)
    {
        // get current page from the savedstate bundle
        mCurrentFragment = savedState.getInt(PAGER_CURRENT_ITEM_KEY);

        // get reference to the mainfragment from the savedstate bundle
        mMainFragment = (MainFragment) getSupportFragmentManager().getFragment(savedState, MAIN_FRAGMENT_KEY);

        super.onRestoreInstanceState(savedState);
    }

    /**
     * Inflate the main options menu
     * @param menu Menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // inflate the main menu
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    /**
     * Handle option menu item selection
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // refresh the fixtures
        if (id == R.id.action_refresh) {
            mMainFragment.updateScores();
            return true;
        }

        // open the settings activity
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        // open the about activity
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
