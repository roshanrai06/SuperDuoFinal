package barqsoft.footballscores;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import barqsoft.footballscores.widget.LatestFixtureWidgetProvider;

public class MainFragment extends Fragment
{
    // tag logging with classname
    private static final String LOG_TAG = MainFragment.class.getSimpleName();

    // set the amount of pages in the viewpager
    public static final int NUM_PAGES = 5;

    // reference to the viewpager object
    public ViewPager mViewPager;

    // array containing the pagefragments
    private PageFragment[] viewFragments = new PageFragment[NUM_PAGES];

    // keep a reference to toast messages
    private Toast mToast;

    /**
     * Create the scores pagefragments and add the pageradapter to the viewpager
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundl
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.main_fragment, container, false);

        // call the footballdataservice to load the scores (not on rotate)
        if (savedInstanceState == null) {
            updateScores();
        }

        // get a reference to the viewpager and set the pageadapter
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(new ScoresPagerAdapter(getChildFragmentManager()));
        mViewPager.setCurrentItem(MainActivity.mCurrentFragment);

        // create the pagefragments and set the date for each page (for days ago until 2 days from now)
        for (int i = 0;i < NUM_PAGES; i++) {

            // create the date for each pagefragment
            Date fragmentDate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            // create the fragment with date and add it to the array
            viewFragments[i] = new PageFragment();
            viewFragments[i].setDate(simpleDateFormat.format(fragmentDate));

            // pass on received arguments from widgets
            viewFragments[i].setArguments(getArguments());

            // if we received a date argument we select the corresponding page
            Bundle arguments = getArguments();
            if (arguments != null && arguments.getString(LatestFixtureWidgetProvider.SCORES_DATE) != null) {
                if (simpleDateFormat.format(fragmentDate).equals(arguments.getString(LatestFixtureWidgetProvider.SCORES_DATE))) {
                    mViewPager.setCurrentItem(i);
                }
            }
        }

        // reverse the order of the page date fragments when in rtl mode
        if (Utility.isRtl(getContext())) {
            Collections.reverse(Arrays.asList(viewFragments));
        }

        // set tabindicator color and tab padding programmatically
        PagerTabStrip strip = (PagerTabStrip) rootView.findViewById(R.id.pager_header);
        strip.setTabIndicatorColor(ContextCompat.getColor(getContext(), android.R.color.white));
        strip.setPadding(
                (int) getResources().getDimension(R.dimen.tabstrip_tab_padding_horizontal),
                0,
                (int) getResources().getDimension(R.dimen.tabstrip_tab_padding_horizontal),
                0);

        return rootView;
    }

    /**
     * Create the FootballDataService with the api key from the preferences and request the scores
     *  and team details from the footbaal-data.org api
     */
    public void updateScores() {

        // has the api key been set?
        if (!Utility.getPreferredApiKey(getActivity()).equals("")) {

            // check if we have a network connection
            if (Utility.isNetworkAvailable(getActivity())) {

                // start the football-data service to trigger loading the teams and fixtures
                Utility.startFootballDataService(getActivity(), Utility.getPreferredApiKey(getActivity()));

            } else {

                // if without internet, show a notice to the user in the form of a toast
                mToast = Utility.showToast(getActivity(), mToast, getString(R.string.network_required_notice));
            }
        } else {
            mToast = Utility.showToast(getContext(), mToast, getString(R.string.config_footballdata_api_key_not_set_notice));
        }
    }

    /**
     * Manage the pagefragments
     */
    private class ScoresPagerAdapter extends FragmentStatePagerAdapter {

        /**
         * Constructor
         * @param fragmentManager FragmentManager
         */
        public ScoresPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        /**
         * Get the fragment for given pager position
         * @param position int
         * @return PageFragment
         */
        @Override
        public Fragment getItem(int position) {
            return viewFragments[position];
        }

        /**
         * Get the amount of pages in pager
         * @return int
         */
        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        /**
         * Return the page title for given pager position
         */
        @Override
        public CharSequence getPageTitle(int position) {

            long dateInMillis = 0;
            String title = "";

            // get the time of given day position -xdays in milliseconds (or +xdays when in rtl mode)
            if (Utility.isRtl(getContext())) {
                dateInMillis = System.currentTimeMillis() - ((position - 2) * 86400000);
            } else {
                dateInMillis = System.currentTimeMillis() + ((position - 2) * 86400000);
            }

            Time t = new Time();
            t.setToNow();
            int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
            int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);

            if (julianDay == currentJulianDay) {
                // today
                title = getActivity().getString(R.string.today);
            } else if (julianDay == currentJulianDay + 1) {
                // tomorrow
                title = getActivity().getString(R.string.tomorrow);
            } else if (julianDay == currentJulianDay - 1) {
                // yesterday
                title = getActivity().getString(R.string.yesterday);
            } else {
                // weekday
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
                title = dayFormat.format(dateInMillis);
            }

            return title.toUpperCase();
        }
    }
}
