package barqsoft.footballscores.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ScoresContract
{
    // tag logging with classname
    private static final String LOG_TAG = ScoresContract.class.getSimpleName();

    // uri segments
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+ CONTENT_AUTHORITY);
    public static final String PATH = "scores";
    public static final String PATH_RECENT = "recent";

    // table name
    public static final String SCORES_TABLE = "scores_table";

    /**
     * Scores definition
     */
    public static final class ScoresEntry implements BaseColumns
    {
        // column names
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String HOME_ID_COL = "home_id";
        public static final String HOME_LOGO_COL = "home_logo";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_COL = "away";
        public static final String AWAY_ID_COL = "away_id";
        public static final String AWAY_LOGO_COL = "away_logo";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_DAY = "match_day";
        public static final String MATCH_ID = "match_id";

        // types
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        /**
         * Build uri for scores by league
         * @return Uri
         */
        public static Uri buildScoreWithLeague() {
            return BASE_CONTENT_URI.buildUpon().appendPath(LEAGUE_COL).build();
        }

        /**
         * Build uri for scores by id
         * @return Uri
         */
        public static Uri buildScoreWithId() {
            return BASE_CONTENT_URI.buildUpon().appendPath(MATCH_ID).build();
        }

        /**
         * Build uri for scores by date
         * @return Uri
         */
        public static Uri buildScoreWithDate() {
            return BASE_CONTENT_URI.buildUpon().appendPath(DATE_COL).build();
        }

        /**
         * Build uri to get the most recent matches
         * @return Uri
         */
        public static Uri buildScoreMostRecent() {
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECENT).build();
        }
    }
}
