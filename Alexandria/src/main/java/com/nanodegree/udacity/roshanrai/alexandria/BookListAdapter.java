package com.nanodegree.udacity.roshanrai.alexandria;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nanodegree.udacity.roshanrai.alexandria.datamodel.AlexandriaContract;

/**
 * Created by roshan.rai on 2/9/2016.
 */
public class BookListAdapter extends CursorAdapter {
    // keep the activity context
    final private Context mContext;

    // empty view for when we have no data and want to inform the user
    final private View mEmptyView;

    /**
     * Constructor
     * @param context Context
     * @param cursor Cursor
     * @param flags int
     */
    public BookListAdapter(Context context, Cursor cursor, int flags, View emptyView) {
        super(context, cursor, flags);

        mContext = context;
        mEmptyView = emptyView;

        // show or hide the empty view, depending on empty cursor
        mEmptyView.setVisibility(cursor.getCount() == 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * Inflate a new book_list_item view based on the definition in the viewholder inner class
     * @param context Context
     * @param cursor Cursor
     * @param parent ViewGroup
     * @return View
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // get the view layout
        View view = LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);

        // set references to the view items in the layout as defined in the viewholder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /**
     * Populate the view items from cursor data
     * @param view View
     * @param context Context
     * @param cursor Cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // get references to the view items in the layout as defined in the viewholder
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // set the book cover image
        String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        Glide.with(mContext)
                .load(imgUrl)
                .error(R.drawable.cover_not_available)
                .crossFade()
                .into(viewHolder.bookCover);


        // set the book title
        String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        viewHolder.bookTitle.setText(bookTitle);

        // set the book subtitle
        String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        viewHolder.bookSubTitle.setText(bookSubTitle);
    }

    /**
     * Override the swapCursor so we can detect if we have an empty cursor and show a notice
     * @param newCursor Cursor
     * @return Cursor
     */
    @Override
    public Cursor swapCursor(Cursor newCursor) {

        // show or hide the empty view, depending on empty cursor
        if (newCursor != null) {
            mEmptyView.setVisibility(newCursor.getCount() == 0 ? View.VISIBLE : View.GONE);
        }

        return super.swapCursor(newCursor);
    }

    /**
     * Helper class for references to the view items
     */
    public static class ViewHolder {

        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;

        public ViewHolder(View view) {
            bookCover = (ImageView) view.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) view.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) view.findViewById(R.id.listBookSubTitle);
        }
    }
}
