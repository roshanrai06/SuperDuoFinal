package barqsoft.footballscores.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class ScoresProvider extends ContentProvider
{
    // tag logging with classname
    private static final String LOG_TAG = ScoresProvider.class.getSimpleName();

    // reference to our sqlitedbhelper
    private static ScoresDbHelper mOpenHelper;

    // uri matcher
    private UriMatcher mUriMatcher = buildUriMatcher();

    // uris
    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int MATCHES_MOST_RECENT = 104;

    // sql filters
    private static final String SCORES_BY_LEAGUE = ScoresContract.ScoresEntry.LEAGUE_COL + " = ?";
    private static final String SCORES_BY_DATE = ScoresContract.ScoresEntry.DATE_COL + " LIKE ?";
    private static final String SCORES_BY_ID = ScoresContract.ScoresEntry.MATCH_ID + " = ?";
    private static final String SCORES_MOST_RECENT =
            ScoresContract.ScoresEntry.DATE_COL + " <= ? AND "+
            ScoresContract.ScoresEntry.HOME_GOALS_COL + " != 'null' AND "+
            ScoresContract.ScoresEntry.AWAY_GOALS_COL + " != 'null'";

    // to identify the broadcast message that data has been updated
    public static final String ACTION_DATA_UPDATED = ScoresContract.CONTENT_AUTHORITY + ".ACTION_DATA_UPDATED";

    /**
     * Create a uri-matcher object by adding the available uris
     * @return UriMatcher
     */
    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ScoresContract.BASE_CONTENT_URI.toString();

        matcher.addURI(authority, null, MATCHES);
        matcher.addURI(authority, ScoresContract.ScoresEntry.LEAGUE_COL, MATCHES_WITH_LEAGUE);
        matcher.addURI(authority, ScoresContract.ScoresEntry.DATE_COL, MATCHES_WITH_DATE);
        matcher.addURI(authority, ScoresContract.ScoresEntry.MATCH_ID, MATCHES_WITH_ID);
        matcher.addURI(authority, ScoresContract.PATH_RECENT, MATCHES_MOST_RECENT);

        return matcher;
    }

    /**
     * Match a given uri to a constant int
     * @param uri Uri
     * @return int
     */
    private int matchUri(Uri uri) {
        String link = uri.toString();

        if(link.contentEquals(ScoresContract.BASE_CONTENT_URI.toString())) {
            return MATCHES;
        } else if(link.contentEquals(ScoresContract.ScoresEntry.buildScoreWithDate().toString())) {
            return MATCHES_WITH_DATE;
        } else if(link.contentEquals(ScoresContract.ScoresEntry.buildScoreWithLeague().toString())) {
            return MATCHES_WITH_LEAGUE;
        } else if(link.contentEquals(ScoresContract.ScoresEntry.buildScoreWithId().toString())) {
            return MATCHES_WITH_ID;
        } else if(link.contentEquals(ScoresContract.ScoresEntry.buildScoreMostRecent().toString())) {
            return MATCHES_MOST_RECENT;
        }

        return -1;
    }

    /**
     * Instantiate a new db helper
     * @return boolean
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new ScoresDbHelper(getContext());
        return false;
    }

    /**
     * Get the type of data
     * @param uri Uri
     * @return String
     */
    @Override
    public String getType(Uri uri) {

        final int matcher = mUriMatcher.match(uri);

        switch (matcher) {
            case MATCHES:
                return ScoresContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_LEAGUE:
                return ScoresContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_DATE:
                return ScoresContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return ScoresContract.ScoresEntry.CONTENT_ITEM_TYPE;
            case MATCHES_MOST_RECENT:
                return ScoresContract.ScoresEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Query the database
     * @param uri Uri
     * @param projection String[]
     * @param selection String
     * @param selectionArgs String[]
     * @param sortOrder String
     * @return Cursor
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;

        int match = matchUri(uri);

        switch (match) {
            case MATCHES:

                // all matches
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ScoresContract.SCORES_TABLE,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder);
                break;
            case MATCHES_WITH_DATE:

                // matches by date
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ScoresContract.SCORES_TABLE,
                        projection,
                        SCORES_BY_DATE,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MATCHES_WITH_LEAGUE:

                // matches by league
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ScoresContract.SCORES_TABLE,
                        projection,
                        SCORES_BY_LEAGUE,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MATCHES_WITH_ID:

                // matches by id
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ScoresContract.SCORES_TABLE,
                        projection,
                        SCORES_BY_ID,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MATCHES_MOST_RECENT:

                // most recent matches
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ScoresContract.SCORES_TABLE,
                        projection,
                        SCORES_MOST_RECENT,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }

        if (getContext() != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return retCursor;
    }

    /**
     * Insert scores in bulk
     * @param uri Uri
     * @param values ContentValues[]
     * @return int
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (matchUri(uri)) {

            // insert matches
            case MATCHES:
                db.beginTransaction();

                int returncount = 0;

                try {
                    for(ContentValues value : values) {
                        long _id = db.insertWithOnConflict(
                                ScoresContract.SCORES_TABLE,
                                null,
                                value,
                                SQLiteDatabase.CONFLICT_REPLACE
                        );

                        if (_id != -1) {
                            returncount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } catch (SQLiteException e) {
                    Log.d(LOG_TAG, "");
                } finally {
                    db.endTransaction();
                }

                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);

                    // tell broadcast receivers that data was updated (widgets)
                    Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(getContext().getPackageName());
                    getContext().sendBroadcast(dataUpdatedIntent);
                }

                return returncount;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
