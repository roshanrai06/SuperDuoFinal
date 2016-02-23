package com.nanodegree.udacity.roshanrai.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nanodegree.udacity.roshanrai.alexandria.datamodel.AlexandriaContract;
import com.nanodegree.udacity.roshanrai.alexandria.services.GoogleBooksService;

/**
 * Created by roshan.rai on 2/9/2016.
 */
public class AddBookFragment  extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    // use classname when logging
    private static final String LOG_TAG = AddBookFragment.class.getSimpleName();

    // reference to the ean search field
    private EditText mEanSearchField;

    // unique id for the loadermanager
    private final int LOADER_ID = 30;

    // key for storing the ean search field value in the savedinstance bundel
    private final String EAN_CONTENT = "eanFieldKey";

    // the first 3 digits of a isbn13 are always the same
    private String mEanPrefix;

    // common toast object
    protected Toast mToast;

    /**
     * Constructor
     */
    public AddBookFragment() {
    }

    /**
     * On create view initialize the components and its listeners
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mEanPrefix = getString(R.string.ean_13_prefix);

        // get the add book fragment
        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);

        // get the edittext input field and attach text changed listener
        mEanSearchField = (EditText) rootView.findViewById(R.id.ean);
        mEanSearchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            /**
             * After the text value has changed trigger an api call using the bookservice
             * @param text Editable
             */
            @Override
            public void afterTextChanged(Editable text) {
                String ean = text.toString().trim();

                // if we have a 10- or 13-digit number we already handle the submit
                if (((ean.length() == 10) && !ean.startsWith(mEanPrefix)) || (ean.length() == 13)) {
                    handleSubmit(false);
                } else {
                    // else clear the preview fields
                    clearFields();
                }
            }
        });

        // submit on enter
        mEanSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((   (event != null) &&
                        (event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE)) {

                    // force a submit
                    handleSubmit(true);

                    // keep the focus on the textfield
                    mEanSearchField.requestFocus();
                }

                return true;
            }
        });

        // handle submit onclick on the search button
        rootView.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // force a submit
                handleSubmit(true);

                // set the focus on the textfield
                mEanSearchField.requestFocus();
            }
        });

        // get the scan button and attach the onclick to launch the scanner
        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // launch the barcode scanner
                if (Utility.isNetworkAvailable(getActivity())) {
                    scanBarcode();
                } else {
                    mToast = Utility.showToast(getActivity(), mToast, getString(R.string.network_required_notice));
                }
            }
        });

        // get the save button and attach the onclick handler to save the book to the database
        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // send message to bookservice to save the book
                Intent bookIntent = new Intent(getActivity(), GoogleBooksService.class);
                bookIntent.putExtra(GoogleBooksService.EAN_KEY, mEanSearchField.getText().toString());
                bookIntent.setAction(GoogleBooksService.CONFIRM_BOOK);
                getActivity().startService(bookIntent);

                // clear the search field
                mEanSearchField.setText("");
            }
        });

        // get the delete button and attach the onclick handler to let the mainactivity delete a
        //  book from the database
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // clear the search field
                mEanSearchField.setText("");
            }
        });

        // load previously values from instancestate, if available
        if(savedInstanceState != null) {

            // get ean search field value and update if not empty
            String ean = savedInstanceState.getString(EAN_CONTENT);
            if (ean != null) {
                if (!ean.equals("")) {
                    mEanSearchField.setText(ean);
                }
            }
        }

        // set the toolbar title field
        getActivity().setTitle(R.string.scan);

        // set the focus on the edittext field
        mEanSearchField.requestFocus();

        return rootView;
    }

    /**
     * Save the state
     * @param outState Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save the edittext field state for rotation
        if(mEanSearchField != null) {
            outState.putString(EAN_CONTENT, mEanSearchField.getText().toString());
        }
    }

    /**
     * Trigger the fetch bookservice on various events
     * @param forced boolean show toast notices when forced is true
     */
    private void handleSubmit(boolean forced) {

        // get the value entered in the searchfield
        String ean = mEanSearchField.getText().toString().trim();

        // check if an isbn number was entered
        if ((ean.length() == 10) || (ean.length() == 13)) {

            // prefix isbn10 numbers
            if ((ean.length() == 10) && !ean.startsWith(mEanPrefix)) {
                ean = mEanPrefix + ean;
            }

            // if we have a string of 13 digits
            if (ean.length() == 13) {

                // start a bookservice intent to call the books api
                if (Utility.isNetworkAvailable(getActivity())) {
                    fetchBookFromService(ean);

                    // restart the loader to populate the preview view
                    restartLoader();

                } else {
                    if (forced) {
                        // show toast when we don't have a network connection
                        mToast = Utility.showToast(getActivity(), mToast, getString(R.string.network_required_notice));
                    }
                }
            }
        } else if (forced) {
            // show toast when no text was entered
            mToast = Utility.showToast(getActivity(), mToast, getString(R.string.text_input_required));
        }
    }

    /**
     * Start the GoogleBooksService and tell it to fetch a book with given ean
     * @param ean String
     */
    private void fetchBookFromService(String ean) {
        Intent bookIntent = new Intent(getActivity(), GoogleBooksService.class);
        bookIntent.putExtra(GoogleBooksService.EAN_KEY, ean);
        bookIntent.setAction(GoogleBooksService.FETCH_BOOK);
        getActivity().startService(bookIntent);
    }

    /**
     * Scan a ISBN book barcode using the zxing library
     */
    private void scanBarcode() {

        // create the intent integrator to scan in the current fragment
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);

        // use a custom scanactivity that can be rotated
        integrator.setCaptureActivity(ScanActivity.class);
        integrator.setOrientationLocked(false);

        // limit scanning to only one-dimensional barcodes
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);

        // set the prompt message
        integrator.setPrompt(getString(R.string.scanner_prompt));

        // launch the scanner
        integrator.initiateScan();
    }

    /**
     * Catch the result of a scanned barcode
     * @param requestCode int
     * @param resultCode int
     * @param data Intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // parse the result in a intentresult object
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(result != null) {
            if(result.getContents() == null) {
                mToast = Utility.showToast(getActivity(), mToast, getString(R.string.scanner_cancelled));
            } else {

                // get the scanned code and code format
                String ean = result.getContents();

                // inform the user about the ean number
                mToast = Utility.showToast(getActivity(), mToast, "Scanned barcode: "+ ean);

                // once we have an isbn, add it to the search field (this will trigger the bookservice call)
                mEanSearchField.setText(ean);
            }
        }
    }

    /**
     * Restart the loader
     */
    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    /**
     * Create the loader to load full book date for entered ean
     * @param id int
     * @param args Bundle
     * @return Loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mEanSearchField.getText().length() > 0) {

            // get the string value from the ean search field
            String eanStr = mEanSearchField.getText().toString();

            // add prefix if entered value length is 10
            if (eanStr.length() == 10 && !eanStr.startsWith(mEanPrefix)) {
                eanStr = mEanPrefix + eanStr;
            }

            // load full book date for entered ean
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                    null,
                    null,
                    null,
                    null
            );
        } else {
            return null;
        }
    }

    /**
     * Populate the view when finished loading
     * @param loader Loader
     * @param data Cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {

            // populate the view items
            View view = getView();
            if (view != null) {

                // cover image
                String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
                ImageView coverView = (ImageView) view.findViewById(R.id.bookCover);
                if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
                    // load the cover image
                    Glide.with(this)
                            .load(imgUrl)
                            .error(R.drawable.cover_not_available)
                            .crossFade()
                            .into(coverView);
                } else {
                    // or set the image-not-available resource
                    coverView.setImageResource(R.drawable.cover_not_available);
                }
                coverView.setVisibility(View.VISIBLE);

                // book title
                String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
                ((TextView) view.findViewById(R.id.bookTitle)).setText(bookTitle);

                // subtitle
                String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
                ((TextView) view.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

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
                ((TextView) view.findViewById(R.id.bookDescription)).setText(desc);

                // show the delete and save button
                view.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
                view.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * On reset the loader - not used
     * @param loader Loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Helper method to clear the book preview view items
     */
    private void clearFields() {
        View view = getView();

        if (view != null) {
            ((TextView) view.findViewById(R.id.bookTitle)).setText("");
            ((TextView) view.findViewById(R.id.bookSubTitle)).setText("");
            ((TextView) view.findViewById(R.id.authors)).setText("");
            ((TextView) view.findViewById(R.id.categories)).setText("");
            ((TextView) view.findViewById(R.id.bookDescription)).setText("");

            view.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
        }
    }
}
