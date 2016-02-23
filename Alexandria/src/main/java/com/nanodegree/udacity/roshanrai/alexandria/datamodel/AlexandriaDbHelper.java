package com.nanodegree.udacity.roshanrai.alexandria.datamodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by roshan.rai on 2/9/2016.
 */
public class AlexandriaDbHelper extends SQLiteOpenHelper {
    // use classname when logging
    private static final String LOG_TAG = AlexandriaDbHelper.class.getSimpleName();

    // version of the database, triggers onUpgrade if version of previous installed is lower
    private static final int DATABASE_VERSION = 2;

    // name of the database file
    public static final String DATABASE_NAME = "alexandria.db";

    /**
     * Constructor
     * @param context Context
     */
    public AlexandriaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the given database, only called if no database exists yet
     * @param db SQLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // sql string for creating the books table
        final String SQL_CREATE_BOOK_TABLE = "CREATE TABLE " + AlexandriaContract.BookEntry.TABLE_NAME + " ("+
                AlexandriaContract.BookEntry._ID + " INTEGER PRIMARY KEY," +
                AlexandriaContract.BookEntry.SAVED + " INTEGER," +
                AlexandriaContract.BookEntry.TITLE + " TEXT NOT NULL," +
                AlexandriaContract.BookEntry.SUBTITLE + " TEXT ," +
                AlexandriaContract.BookEntry.DESC + " TEXT ," +
                AlexandriaContract.BookEntry.IMAGE_URL + " TEXT, " +
                "UNIQUE ("+ AlexandriaContract.BookEntry._ID +") ON CONFLICT IGNORE)";

        // sql string for creating the authors table
        final String SQL_CREATE_AUTHOR_TABLE = "CREATE TABLE " + AlexandriaContract.AuthorEntry.TABLE_NAME + " ("+
                AlexandriaContract.AuthorEntry._ID + " INTEGER," +
                AlexandriaContract.AuthorEntry.AUTHOR + " TEXT," +
                " FOREIGN KEY (" + AlexandriaContract.AuthorEntry._ID + ") REFERENCES " +
                AlexandriaContract.BookEntry.TABLE_NAME + " (" + AlexandriaContract.BookEntry._ID + "))";

        // sql string for creating the categories table
        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + AlexandriaContract.CategoryEntry.TABLE_NAME + " ("+
                AlexandriaContract.CategoryEntry._ID + " INTEGER," +
                AlexandriaContract.CategoryEntry.CATEGORY + " TEXT," +
                " FOREIGN KEY (" + AlexandriaContract.CategoryEntry._ID + ") REFERENCES " +
                AlexandriaContract.BookEntry.TABLE_NAME + " (" + AlexandriaContract.BookEntry._ID + "))";

        // execute the sql query to create the books table
        db.execSQL(SQL_CREATE_BOOK_TABLE);

        // execute the sql query to create the authors table
        db.execSQL(SQL_CREATE_AUTHOR_TABLE);

        // execute the sql query to create the categories table
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
    }

    /**
     * Upgrade the given database, only called if there already exists a database and the given
     *  version number is higher than previously installed version number
     * @param db SQLiteDatabase
     * @param oldVersion int
     * @param newVersion int
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // sql string to update the books table by adding a 'saved' column
        final String SQL_UPDATE_BOOK_TABLE = "ALTER TABLE " + AlexandriaContract.BookEntry.TABLE_NAME +
                " ADD "+ AlexandriaContract.BookEntry.SAVED + " INTEGER;";

        // sql string to update the 'saved' column and set it to 1 for all existing books
        final String SQL_UPDATE_BOOK_RECORDS = "UPDATE "+ AlexandriaContract.BookEntry.TABLE_NAME +
                " SET "+ AlexandriaContract.BookEntry.SAVED +" = 1 "+
                " WHERE 1 = 1;";

        // execute the sql update table query
        db.execSQL(SQL_UPDATE_BOOK_TABLE);

        // execute the sql update records query
        db.execSQL(SQL_UPDATE_BOOK_RECORDS);
    }
}
