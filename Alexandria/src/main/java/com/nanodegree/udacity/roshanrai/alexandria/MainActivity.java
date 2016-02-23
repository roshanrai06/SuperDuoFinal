package com.nanodegree.udacity.roshanrai.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.nanodegree.udacity.roshanrai.alexandria.services.GoogleBooksService;

public class MainActivity extends AppCompatActivity implements
        NavigationDrawerFragment.Callbacks,
        BookListFragment.Callbacks,
        BookFragment.Callbacks {

    // use classname when logging
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // fragment managing the behaviors, interactions and presentation of the navigation drawer
    private NavigationDrawerFragment mNavigationDrawerFragment;

    // when we rotate from portrait to landscape on a tablet we can keep the selected book here
    private String mEan;
    private String mBookTitle;

    // used to store the last screen mTitle
    private String mTitle;

    // reference to the app title textview in the toolbar
    private TextView mToolbarTitle;

    // receive messages
    private BroadcastReceiver mMessageReciever;

    // tablet indicator
    public static boolean IS_TABLET = false;

    // message type constants we can receive from the googlebooksservice
    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    // key constants for saving the state
    private static final String mEanStateKey = "mEan";
    private static final String mBookTitleStateKey = "mBookTitle";
    private static final String mTitleStateKey = "mTitle";

    // drawer position constants
    public static final int BOOKLIST_FRAGMENT_POSITION = 0;
    public static final int ADDBOOK_FRAGMENT_POSITION = 1;
    public static final int ABOUT_FRAGMENT_POSITION = 2;

    private Toast mToast;

    /**
     * On create setup layout and fragments
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // are we running on a tablet
        IS_TABLET = isTablet();

        // if we run on a tablet select an alternative main layout
        if (IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        } else {
            setContentView(R.layout.activity_main);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        // create receiver to receive event messages
        mMessageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciever, filter);

        // get a reference to the navigation drawer fragment
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // get the mTitle of the activity
        mTitle = (String) getTitle();

        // set the toolbar title to the name of the app
        mToolbarTitle.setText(R.string.app_name);

        // set up the navigation drawer
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        // send message to bookservice to delete existing not-saved books from previous session
        Intent bookIntent = new Intent(this, GoogleBooksService.class);
        bookIntent.setAction(GoogleBooksService.DELETE_NOT_SAVED);
        startService(bookIntent);

        // prevent the keyboard from appearing already oncreate mainactivity
        Utility.hideKeyboardFromActivity(this);

        // get the saved state vars
        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(mTitleStateKey)) {
                mTitle = savedInstanceState.getString(mTitleStateKey, null);
            }

            if (savedInstanceState.containsKey(mEanStateKey)) {
                mEan = savedInstanceState.getString(mEanStateKey, null);
            }

            if (savedInstanceState.containsKey(mBookTitleStateKey)) {
                mBookTitle = savedInstanceState.getString(mBookTitleStateKey, null);
            }

            // when rotating to landscape booklist on a tablet select the previously selected book
            if (IS_TABLET && findViewById(R.id.right_container) != null && mTitle.equals(getString(R.string.books)) && mEan != null && mBookTitle != null) {
                onItemSelected(mEan, mBookTitle);
                mEan = null;
                mBookTitle = null;
            }
        }
    }

    /**
     * Save the the current state before leaving the activity
     *
     * @param outState Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putString(mTitleStateKey, mTitle);
        outState.putString(mEanStateKey, mEan);
        outState.putString(mBookTitleStateKey, mBookTitle);
    }

    /**
     * Select the fragment based on the selected position in the navigation drawer menu
     * @param position int
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {

        // choose the selected fragment based on given position
        Fragment choosenFragment;
        switch (position){
            default:
            case BOOKLIST_FRAGMENT_POSITION:
                choosenFragment = new BookListFragment();
                break;
            case ADDBOOK_FRAGMENT_POSITION:
                choosenFragment = new AddBookFragment();
                break;
            case ABOUT_FRAGMENT_POSITION:
                choosenFragment = new AboutFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        // if we are on a tablet in landscape mode pop the book detail fragment (in case we have one)
        if(findViewById(R.id.right_container) != null) {
            fragmentManager.popBackStack(getString(R.string.detail), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // open the selected fragment in the container element
        fragmentManager.beginTransaction()
                .replace(R.id.container, choosenFragment)
                .addToBackStack((String) mTitle)
                .commit();

        // prevent the keyboard from appearing oncreateview of the fragment
        Utility.hideKeyboardFromActivity(this);
    }

    /**
     * Set title of the activity depending on the loaded fragment
     * @param titleId int
     */
    public void setTitle(int titleId) {

        // set the local mtitle var
        mTitle = getString(titleId);

        // update the toolbar title textview
        mToolbarTitle.setText(mTitle);
    }

    /**
     * When creating the settings menu options
     * @param menu Menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // only show the settings menu when the navigation drawer is not open
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.menu_main, menu);

            // restore the toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                mToolbarTitle.setText(mTitle);
            }

            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Act based on the selected options menu item
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // act on the selected menu option
        if (item.getItemId() == R.id.action_settings) {

            // start the settings activity
            startActivity(new Intent(this, SettingsActivity.class));
            return true;

        } else if (item.getItemId() == android.R.id.home) {

            // hide keyboard when the drawer is opened
            Utility.hideKeyboardFromActivity(this);

            // if we are not on a tablet in landscape mode
            if(findViewById(R.id.right_container) == null) {

                // if we are coming back from the bookdetail fragment, reset the hamburger
                if (mTitle.equals(getString(R.string.detail))) {
                    getSupportFragmentManager().popBackStack();
                    toggleToolbarDrawerIndicator(false);
                    return true;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Open the BookFragment fragment with given ean, using a callback interface, from BookListFragment
     * @param ean String
     */
    @Override
    public void onItemSelected(String ean, String title) {

        // select where to put the detail fragment based on mobile/tablet
        int containerId = R.id.container;
        if(findViewById(R.id.right_container) != null) {
            containerId = R.id.right_container;
        }

        mEan = ean;
        mBookTitle = title;

        // create a bundle to pass the ean to the detail fragment
        Bundle args = new Bundle();
        args.putString(BookFragment.EAN_KEY, ean);
        args.putString(BookFragment.TITLE_KEY, title);

        // instantiate the detail fragment and pass it the bundle containing the ean
        BookFragment fragment = new BookFragment();
        fragment.setArguments(args);

        // replace the contents of the container element with the detail fragment and add it to the backstack
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerId, fragment);
        fragmentTransaction.addToBackStack(getString(R.string.detail));
        fragmentTransaction.commit();

        // toggle the hamburger icon for the back icon
        toggleToolbarDrawerIndicator(true);

        // hide the keyboard when we navigate to the bookdetail fragment
        Utility.hideKeyboardFromActivity(this);
    }

    /**
     * Receive message to show a toast
     */
    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY) != null) {
                mToast = Utility.showToast(MainActivity.this, mToast, intent.getStringExtra(MESSAGE_KEY));
            }
        }
    }

    /**
     * Check if we are running on a tablet
     * @return boolean
     */
    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Check if we can finish the activity when the backbutton is pressed (toolbar or os)
     */
    @Override
    public void onBackPressed() {

        // only close the drawer if it's opened
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
            return;
        }

        // if we are coming back from the book detail fragment, reset the hamburger icon
        if (mTitle.equals(getString(R.string.detail))) {
            toggleToolbarDrawerIndicator(false);
        }

        // if there is only 1 fragment on the backstack, it is a 'main' fragment and we have
        //  nowhere to return to, but exit
        if (getSupportFragmentManager().getBackStackEntryCount() < 2) {
            finish();
        }

        super.onBackPressed();
    }

    /**
     * Toogle the toolbar drawer icon between the hamburger and back icon
     * @param backToHome boolean
     */
    public void toggleToolbarDrawerIndicator(boolean backToHome) {
        // if we are not on a tablet in landscape mode
        if(findViewById(R.id.right_container) == null) {
            mNavigationDrawerFragment.toggleToolbarDrawerIndicator(backToHome);
        }
    }

    /**
     * Release resource on destroy
     */
    @Override
    protected void onDestroy() {

        // unregister the message receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReciever);

        super.onDestroy();
    }
}
