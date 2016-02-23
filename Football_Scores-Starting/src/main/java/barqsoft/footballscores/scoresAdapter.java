package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import barqsoft.footballscores.data.ScoresContract;

class ScoresAdapter extends CursorAdapter
{
    /**
     * Constructor
     * @param context Context
     * @param cursor Cursor
     * @param flags int
     */
    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    /**
     * Inflate the scores listitem and set the matchholder
     * @param context Context
     * @param cursor Cursor
     * @param parent ViewGroup
     * @return View
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // get the listitem layout
        View matchListItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);

        // add the matchholder
        ScoreHolder scoreHolder = new ScoreHolder(matchListItem);
        matchListItem.setTag(scoreHolder);

        return matchListItem;
    }

    /**
     * Populate the scores listitem elements
     * @param view View
     * @param context Context
     * @param cursor Cursor
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ScoreHolder scoreHolder = (ScoreHolder) view.getTag();

        // home club name
        String homeTeamName = cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.HOME_COL));
        scoreHolder.mHomeName.setText(homeTeamName);

        // home club logo
        Glide.with(context.getApplicationContext()).load(
                cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.HOME_LOGO_COL)))
                .error(R.drawable.football)
                .into(scoreHolder.mHomeCrest);
        scoreHolder.mHomeCrest.setContentDescription(homeTeamName);

        // match score
        scoreHolder.mScore.setText(Utility.getScores(
                cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.HOME_GOALS_COL)),
                cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.AWAY_GOALS_COL))));

        // match time
        scoreHolder.mDate.setText(
                cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.TIME_COL)));

        // away club name
        String awayTeamName = cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.AWAY_COL));
        scoreHolder.mAwayName.setText(awayTeamName);

        // away club logo
        Glide.with(context.getApplicationContext()).load(
                cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.AWAY_LOGO_COL)))
                .error(R.drawable.football)
                .into(scoreHolder.mAwayCrest);
        scoreHolder.mAwayCrest.setContentDescription(awayTeamName);

        // detail: league
        TextView league = (TextView) view.findViewById(R.id.league_textview);
        league.setText(Utility.getLeague(context,
                cursor.getInt(cursor.getColumnIndex(ScoresContract.ScoresEntry.LEAGUE_COL))));

        // detail: match day
        TextView match_day = (TextView) view.findViewById(R.id.matchday_textview);
        match_day.setText(Utility.getMatchDay(context,
                cursor.getInt(cursor.getColumnIndex(ScoresContract.ScoresEntry.MATCH_DAY)),
                cursor.getInt(cursor.getColumnIndex(ScoresContract.ScoresEntry.LEAGUE_COL))));

        // detail: share button
        Button share_button = (Button) view.findViewById(R.id.share_button);
        share_button.setOnClickListener(new View.OnClickListener() {
            /**
             * Add share action onclick
             * @param v View
             */
            @Override
            public void onClick(View v) {
                context.startActivity(createShareScoreIntent(scoreHolder.mHomeName.getText() + " " +
                                scoreHolder.mScore.getText() + " " +
                                scoreHolder.mAwayName.getText() + " " +
                                context.getString(R.string.share_hashtag)
                ));
            }
        });
    }

    /**
     * Create a share intent to share a match
     * @param shareText String
     * @return Intent
     */
    public Intent createShareScoreIntent(String shareText) {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        return shareIntent;
    }
}