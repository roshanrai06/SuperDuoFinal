package com.nanodegree.udacity.roshanrai.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nanodegree.udacity.roshanrai.alexandria.datamodel.AlexandriaContract;
import com.nanodegree.udacity.roshanrai.alexandria.services.GoogleBooksService;

/**
 * Created by roshan.rai on 2/9/2016.
 */
public class BookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    // use classname when logging
    private final String LOG_TAG = BookFragment.class.getSimpleName();

    public static final String EAN_KEY = "eanFieldKey";
    public static final String TITLE_KEY = "titleFieldKey";

    // unique id for the loadermanager
    private final int LOADER_ID = 20;

    // ean number of selected book, passed in as arguments bundle intent to the fragment
    private String mEan;

    // book title
    private String mBookTitle = "";

    // provider for sharing the book details
    private ShareActionProvider mShareActionProvider;

    /**
     * Constructor
     */
    public BookFragment() {
    }

    /**
     * Callback interface to be used in the mainactivity, for restoring the drawer icon
     */
    public interface Callbacks {
        /**
         * Set the drawer icon
         * @param backToHome boolean
         */
        void toggleToolbarDrawerIndicator(boolean backToHome);
        /**
         * Called when a book is deleted, to reload the booklist
         * @param position int
         */
        void onNavigationDrawerItemSelected(int position);
    }

    /**
     * Set the options menu to true because we have the sharing options
     * @param savedInstanceState Bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    /**
     * Get fragment view and attach event handlers
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // get the ean number from the arguments and load the book details
        Bundle arguments = getArguments();
        if (arguments != null) {
            mEan = arguments.getString(BookFragment.EAN_KEY);
            mBookTitle = arguments.getString(BookFragment.TITLE_KEY);
            restartLoader();
        }

        // get the fragment view and attach onclick handler to the delete button
        View rootView = inflater.inflate(R.layout.fragment_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // tell the service to delete the current book from the database
                Intent bookIntent = new Intent(getActivity(), GoogleBooksService.class);
                bookIntent.putExtra(GoogleBooksService.EAN_KEY, mEan);
                bookIntent.setAction(GoogleBooksService.DELETE_BOOK);
                getActivity().startService(bookIntent);

                // reset the drawer icon
                ((Callbacks) getActivity()).toggleToolbarDrawerIndicator(false);

                // reload the booklist fragment if we are on a tablet in landscape mode
                if (MainActivity.IS_TABLET && getActivity().findViewById(R.id.right_container) != null) {
                    ((Callbacks) getActivity()).onNavigationDrawerItemSelected(MainActivity.BOOKLIST_FRAGMENT_POSITION);
                } else {
                    // else close the current detail fragment
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        // set the title of the mainactivity actionbar
        getActivity().setTitle(R.string.detail);

        // set/keep the drawer button to home/back icon on rotation
        if (savedInstanceState != null) {
            ((Callbacks) getActivity()).toggleToolbarDrawerIndicator(true);
        }

        return rootView;
    }

    /**
     * Initialize the share action
     * @param menu Menu
     * @param inflater MenuInflator
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_book, menu);

        // attach the shareaction intent to the action share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        setShareBookIntent();

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Set the intent to share a book recommendation
     */
    private void setShareBookIntent() {

        // set the share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_text) +" "+ mBookTitle +" | "+
                        getString(R.string.share_url) + mEan
        );

        mShareActionProvider.setShareIntent(shareIntent);
    }

    /**
     * Load full book details for given ean book number
     * @param id int
     * @param args Bundle
     * @return Loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(mEan)),
                null,
                null,
                null,
                null
        );
    }

    /**
     * When finished loading populate the book details view
     * @param loader Loader
     * @param data Cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        View view = getView();
        if (view != null) {

            // book title
            mBookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
            ((TextView) view.findViewById(R.id.fullBookTitle)).setText(mBookTitle);

            // subtitle
            String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
            ((TextView) view.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

            // cover image
            String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
            ImageView coverView = (ImageView) view.findViewById(R.id.fullBookCover);
            if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
                // load the cover image using glide
                Glide.with(this)
                        .load(imgUrl)
                        .error(R.drawable.cover_not_available)
                        .crossFade()
                        .into(coverView);
            } else {
                // or set the image-not-found resource
                coverView.setImageResource(R.drawable.cover_not_available);
            }
            coverView.setVisibility(View.VISIBLE);

            // authors
            String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
            if (authors != null) {
                String[] authorsArr = authors.split(",");
                ((TextView) view.findViewById(R.id.authors)).setLines(authorsArr.length);
                ((TextView) view.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
            }

            // categories
            String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
            ((TextView) view.findViewById(R.id.categories)).setText(categories);

            // description
            String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
            ((TextView) view.findViewById(R.id.fullBookDesc)).setText(desc);

            // ean
            ((TextView) view.findViewById(R.id.ean)).setText(getString(R.string.isbn_13) +": "+ mEan);
        }
    }

    /**
     * Reload the book details from the database
     */
    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    /**
     * Reset the loader - not used
     * @param loader Loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Just before rotating from portrait to landscape mode close the detail fragment, or we will
     * possibly end up with 2 detail fragments
     */
    @Override
    public void onPause() {
        super.onPause();

        // if we are on a tablet in portrait mode
        if (MainActivity.IS_TABLET && getActivity().findViewById(R.id.right_container) == null) {

            // close the book detail fragment
            getActivity().getSupportFragmentManager().popBackStack(getString(R.string.detail), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
