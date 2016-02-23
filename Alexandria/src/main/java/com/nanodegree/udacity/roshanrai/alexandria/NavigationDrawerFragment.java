package com.nanodegree.udacity.roshanrai.alexandria;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by roshan.rai on 2/9/2016.
 */
public class NavigationDrawerFragment  extends Fragment {
    // use classname when logging
    private static final String LOG_TAG = NavigationDrawerFragment.class.getSimpleName();

    // remember the position of the selected item
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    // show the drawer on launch until the user manually expands it
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    // helper component that ties the action bar to the navigation drawer
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    /**
     * Callbacks interface that all activities using this fragment must implement
     */
    public interface Callbacks {
        /**
         * Called when an item in the navigation drawer is selected
         * @param position int
         */
        void onNavigationDrawerItemSelected(int position);
    }

    /**
     * Constructor
     */
    public NavigationDrawerFragment() {
    }

    /**
     * Get the prefered start fragment from the shared preferences and select it
     * @param savedInstanceState Bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mCurrentSelectedPosition = Integer.parseInt(prefs.getString(getString(R.string.start_fragment_preference_key), "0"));

            // select the related fragment
            selectItem(mCurrentSelectedPosition);
        }
    }

    /**
     * Enable options menu
     * @param savedInstanceState Bundle
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Indicate that this fragment would like to influence the set of actions in the action bar
        setHasOptionsMenu(true);
    }

    /**
     * Setup the drawer listview
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // get the navigation drawer listview
        mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        // add an onclick listener to the listview items
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        // load the menu items in the drawer listview
        mDrawerListView.setAdapter(new ArrayAdapter<String>(
                getContext(),
                R.layout.list_item_navigation_drawer,
                android.R.id.text1,
                new String[]{
                        getString(R.string.books),
                        getString(R.string.scan),
                        getString(R.string.about),
                }));

        // set selected item in the drawer listview
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        return mDrawerListView;
    }

    /**
     * Check if the drawer is currently open
     * @return boolean
     */
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Close the drawer
     */
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions
     * @param fragmentId   The android:id of this fragment in its activity's layout
     * @param drawerLayout The DrawerLayout containing this fragment's UI
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {

        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /**
     * Toogle the toolbar drawer icon between the hamburger and back icon
     * @param backToHome boolean
     */
    public void toggleToolbarDrawerIndicator(boolean backToHome) {
        if (backToHome) {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        } else {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }
    }

    /**
     * When selecting an item in the drawer
     * @param position int
     */
    private void selectItem(int position) {

        // update currently selected drawer menu position
        mCurrentSelectedPosition = position;

        // highlight drawer menu position
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }

        // close the navigation drawer
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }

        // select the chosen fragment in the main activity (using the callback interface)
        ((Callbacks) getActivity()).onNavigationDrawerItemSelected(position);
    }

    /**
     * Save the currently selected position in the instance state
     * @param outState Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.menu_main, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
