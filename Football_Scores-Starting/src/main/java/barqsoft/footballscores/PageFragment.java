package barqsoft.footballscores;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import barqsoft.footballscores.data.ScoresContract;
import barqsoft.footballscores.widget.TodaysFixturesWidgetProvider;

public class PageFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    // tag logging with classname
    private static final String LOG_TAG = PageFragment.class.getSimpleName();

    // unique loader id
    public static final int SCORES_LOADER = 0;

    // cursor adapter for loading the scores
    public ScoresAdapter mAdapter;

    // keep a reference to the scores listview
    private ListView mScoresList;

    // date of this pagefragment
    private String mDate;

    // when receiving an intent from the widget we can jump straight to a fixture in the listview
    public int mSelectedFixtureMatchId = 0;

    /**
     * Attach the scoresadapter to the listview and attach the listitem clickhandlers
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.page_fragment, container, false);

        // check if a certain fixture was pre-selected (when coming from the collection widget)
        Bundle arguments = getArguments();
        if (arguments != null && arguments.getInt(TodaysFixturesWidgetProvider.SCORES_MATCH_ID) != 0) {
            mSelectedFixtureMatchId = arguments.getInt(TodaysFixturesWidgetProvider.SCORES_MATCH_ID);
        }

        // create the scoresadapter
        mAdapter = new ScoresAdapter(getActivity(), null, 0);

        // get the scores listview and attach the adapter and clickhandler
        mScoresList = (ListView) rootView.findViewById(R.id.scores_list);
        mScoresList.setAdapter(mAdapter);
        mScoresList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Toggle the details of clicked item and hide all others
             * @param parent ListView
             * @param view View
             * @param position int
             * @param id long
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // toggle detailsview of selected item
                View detailsView = view.findViewById(R.id.match_details);
                if (detailsView.getVisibility() != View.VISIBLE) {
                    view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.score_selected));
                    detailsView.setVisibility(View.VISIBLE);
                    if ((position + 1) == parent.getCount()) {
                        // if this is the last listview item scroll to it by selecting it
                        parent.setSelection(position);
                    }
                } else {
                    view.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
                    detailsView.setVisibility(View.GONE);
                }

                // hide detailsview of all other items in the list
                for (int i = 0; i < parent.getCount(); i++) {
                    View otherItem = parent.getChildAt(i);
                    if (otherItem != null && otherItem != view) {
                        otherItem.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
                        View otherItemDetailsView = otherItem.findViewById(R.id.match_details);
                        otherItemDetailsView.setVisibility(View.GONE);
                    }
                }
            }
        });
        mScoresList.setOnScrollListener(new AbsListView.OnScrollListener() {
            /**
             * Hide the detailsview of all listitems on scroll
             * @param absListView AbsListView
             * @param i int
             */
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                for (int j = 0; j < absListView.getCount(); j++) {
                    View otherItem = absListView.getChildAt(j);
                    if (otherItem != null) {
                        otherItem.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
                        View otherItemDetailsView = otherItem.findViewById(R.id.match_details);
                        otherItemDetailsView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });

        // initialise the loader
        getLoaderManager().initLoader(SCORES_LOADER, null, this);

        return rootView;
    }

    /**
     * Set the date of this pagefragment
     * @param date String
     */
    public void setDate(String date) {
        mDate = date;
    }

    /**
     * Create the loader to load the scores for this fragment date sorted by time and home team name
     * @param i int
     * @param bundle Bundle
     * @return Loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                ScoresContract.ScoresEntry.buildScoreWithDate(),
                null,
                null,
                new String[] { mDate },
                ScoresContract.ScoresEntry.TIME_COL +" ASC, "+ ScoresContract.ScoresEntry.HOME_COL +" ASC"
        );
    }

    /**
     * Populate the view of the fragment by setting the adapter to the cursor with the loaded data
     * @param cursorLoader Loader
     * @param cursor Cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);

        // if we recieved a pre-selected match-id from the collection widget, find the list position
        int fixturePosition = 0;
        if (mSelectedFixtureMatchId != 0) {
            int position = 0;
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex(ScoresContract.ScoresEntry.MATCH_ID)) == mSelectedFixtureMatchId) {
                    fixturePosition = position;
                    break;
                }
                position++;
            }
        }

        // if we recieved a pre-selected position from the collection widget, select it
        if (fixturePosition != 0) {
            final int selectedFixturePosition = fixturePosition;
            mScoresList.post(new Runnable() {
                @Override
                public void run() {

                    // scroll to it
                    mScoresList.setSelection(selectedFixturePosition);

                    // reset the pre-selection so we only selected on app launch
                    mSelectedFixtureMatchId = 0;
                }
            });
        }
    }

    /**
     * Reset the adapter
     * @param cursorLoader Loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
}