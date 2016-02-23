package barqsoft.footballscores.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class ScoresDbHelper extends SQLiteOpenHelper
{
    // use classname when logging
    private static final String LOG_TAG = ScoresDbHelper.class.getSimpleName();

    // version of the database, triggers onUpgrade if version of previous installed is lower
    private static final int DATABASE_VERSION = 3;

    // name of the database file
    public static final String DATABASE_NAME = "Scores.db";

    /**
     * Constructor
     * @param context Context
     */
    public ScoresDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the given database, only called if no database exists yet
     * @param db SQLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // sql string for creating the scores table
        final String CreateScoresTable = "CREATE TABLE "+ ScoresContract.SCORES_TABLE +" ("+
                ScoresContract.ScoresEntry._ID +" INTEGER PRIMARY KEY,"+
                ScoresContract.ScoresEntry.DATE_COL +" TEXT NOT NULL,"+
                ScoresContract.ScoresEntry.TIME_COL +" INTEGER NOT NULL,"+
                ScoresContract.ScoresEntry.HOME_COL +" TEXT NOT NULL,"+
                ScoresContract.ScoresEntry.HOME_ID_COL +" INTEGER NOT NULL,"+
                ScoresContract.ScoresEntry.HOME_LOGO_COL +" TEXT,"+
                ScoresContract.ScoresEntry.HOME_GOALS_COL +" TEXT NOT NULL,"+
                ScoresContract.ScoresEntry.AWAY_COL +" TEXT NOT NULL,"+
                ScoresContract.ScoresEntry.AWAY_ID_COL +" INTEGER NOT NULL,"+
                ScoresContract.ScoresEntry.AWAY_LOGO_COL +" TEXT,"+
                ScoresContract.ScoresEntry.AWAY_GOALS_COL +" TEXT NOT NULL,"+
                ScoresContract.ScoresEntry.LEAGUE_COL +" INTEGER NOT NULL,"+
                ScoresContract.ScoresEntry.MATCH_ID +" INTEGER NOT NULL,"+
                ScoresContract.ScoresEntry.MATCH_DAY +" INTEGER NOT NULL,"+
                " UNIQUE ("+ ScoresContract.ScoresEntry.MATCH_ID +") ON CONFLICT REPLACE);";

        // execute the scores table creation sql
        db.execSQL(CreateScoresTable);
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

        // remove old values when upgrading
        db.execSQL("DROP TABLE IF EXISTS "+ ScoresContract.SCORES_TABLE);

        // create the upgraded database
        onCreate(db);
    }
}
