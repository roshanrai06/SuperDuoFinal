package com.nanodegree.udacity.roshanrai.alexandria;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nanodegree.udacity.roshanrai.alexandria.datamodel.AlexandriaContract;

/**
 * Created by roshan.rai on 2/9/2016.
 */
public class BookListFragment  extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    // use classname when logging
    private final String LOG_TAG = BookListFragment.class.getSimpleName();

    // adapter for population of the booklist view
    private BookListAdapter mBookListAdapter;

    // unique id for the loadermanager
    private final int LOADER_ID = 10;

    // reference to the search textfield
    private EditText mSearchText;

    // reference to the books listview
    private ListView mBookList;

    // books listitem position
    private int mListPosition = ListView.INVALID_POSITION;

    // key constants for saving the state
    private static final String mListPositionStateKey = "listPosition";

    private Toast mToast;

    /**
     * Constructor
     */
    public BookListFragment() {
    }

    /**
     * Callback interface to be used in the mainactivity, for selecting a book from the listview
     */
    public interface Callbacks {
        /**
         * Open a bookdetail fragment from given ean
         * @param ean String
         * @param title String
         */
        void onItemSelected(String ean, String title);
    }

    /**
     * On create setup fragment layout items, data and clickhandlers
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // get the booklist fragment
        View rootView = inflater.inflate(R.layout.fragment_book_list, container, false);

        // get a reference to the search field and add handler to search the list after typing
        mSearchText = (EditText) rootView.findViewById(R.id.searchText);
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            /**
             * Search/auto-complete the booklist when typing
             * @param text Editable
             */
            @Override
            public void afterTextChanged(Editable text) {
                restartLoader();
            }
        });

        // on enter search the list
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (((event != null) &&
                        (event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE)) {

                    // get the value of the edittext and search if the length is minimum 2 chars
                    String query = mSearchText.getText().toString().trim();
                    if (query.length() > 1) {
                        restartLoader();
                    } else {
                        mToast = Utility.showToast(getActivity(), mToast, getString(R.string.search_list_notice));
                    }

                    // keep the focus on the textfield
                    mSearchText.requestFocus();
                }

                return true;
            }
        });

        // get the button and attach onclick handler to load the books (based on optionally entered search string)
        ImageButton button = (ImageButton) rootView.findViewById(R.id.searchButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the value of the edittext and search if the length is minimum 2 chars
                String query = mSearchText.getText().toString().trim();
                if (query.length() > 1) {
                    restartLoader();
                } else {

                    // show the toast
                    mToast = Utility.showToast(getActivity(), mToast, getString(R.string.search_list_notice));

                    // set the focus in the edittext
                    mSearchText.requestFocus();
                }
            }
        });

        // get cursor containing all the saved books
        Cursor booksCursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                AlexandriaContract.BookEntry.SAVED +" = ? ",
                new String[] {"1"},
                null
        );

        // get the emptyview for the books listview
        View emptyView = rootView.findViewById(R.id.listview_empty);

        // create the booklist adapter and populate it with the loaded books
        mBookListAdapter = new BookListAdapter(getActivity(), booksCursor, 0, emptyView);

        // get the booklist view, attach the adapter, and attach onclick handler on the list items to open the bookdetail fragment
        mBookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        mBookList.setAdapter(mBookListAdapter);
        mBookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor selectedBookCursor = mBookListAdapter.getCursor();
                if (selectedBookCursor != null && selectedBookCursor.moveToPosition(position)) {

                    // get the clicked listitem position
                    mListPosition = position;

                    // get the book ean and title from the cursor based on the mPosition of the clicked booklist item
                    String ean = selectedBookCursor.getString(selectedBookCursor.getColumnIndex(AlexandriaContract.BookEntry._ID));
                    String title = selectedBookCursor.getString(selectedBookCursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));

                    // call the mainactivity method to open the bookdetail fragment
                    ((Callbacks) getActivity()).onItemSelected(ean, title);
                }
            }
        });

        // set the title of the mainactivity toolbar
        getActivity().setTitle(R.string.books);

        // set the focus on the edittext field
        mSearchText.requestFocus();

        // get the saved state vars
        if (savedInstanceState != null) {

            // get the listview scroll position
            if (savedInstanceState.containsKey(mListPositionStateKey)) {
                mListPosition = savedInstanceState.getInt(mListPositionStateKey);
            }
        }

        return rootView;
    }

    /**
     * Save the the current state before leaving the activity
     *
     * @param outState Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save the selected books listitem position
        if (mListPosition != ListView.INVALID_POSITION) {
            outState.putInt(mListPositionStateKey, mListPosition);
        }
    }

    /**
     * Create the loader, optionally based on a given search string
     * @param id int
     * @param args Bundle
     * @return Loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // query saved books only
        String selection = AlexandriaContract.BookEntry.SAVED +" = ? ";
        String saved = "1";

        // get the search query string from the textfield
        String searchString = mSearchText.getText().toString();

        // if not empty, use it to query the books
        if(searchString.length() > 0) {

            // search in the book title and subtitle
            selection += " AND ("+ AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? )";
            searchString = "%"+ searchString +"%";

            // create the loader with given criteria
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[] {saved, searchString, searchString},
                    null
            );
        } else {

            // else return all the books
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[] {saved},
                    null
            );
        }
    }

    /**
     * Update the booklist adapter cursor when finished loading
     * @param loader Loader
     * @param data data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBookListAdapter.swapCursor(data);

        // scroll to the saved list scroll position
        if (mListPosition != ListView.INVALID_POSITION) {
            mBookList.smoothScrollToPosition(mListPosition);
        }
    }

    /**
     * Restart the loader
     */
    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    /**
     * On reset clear the cursor of the booklist adapter
     * @param loader Loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBookListAdapter.swapCursor(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }
}
