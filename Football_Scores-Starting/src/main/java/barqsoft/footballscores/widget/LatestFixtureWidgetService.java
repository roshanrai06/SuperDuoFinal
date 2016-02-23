package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;
import barqsoft.footballscores.data.ScoresContract;

public class LatestFixtureWidgetService extends IntentService {

    // tag logging with classname
    private static final String LOG_TAG = LatestFixtureWidgetService.class.getSimpleName();

    /**
     * Constructor
     */
    public LatestFixtureWidgetService() {
        super("LatestFixtureWidgetService");
    }

    /**
     * Handle the given intent when starting the service
     * @param intent Intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // find the active instances of our widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, LatestFixtureWidgetProvider.class));

        // create the date and time of now
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // get most recent fixture from the contentprovider
        Uri scoreMostRecentUri = ScoresContract.ScoresEntry.buildScoreMostRecent();
        Cursor cursor = getContentResolver().query(
                scoreMostRecentUri,
                null,
                null,
                new String[] { simpleDateFormat.format(date) },
                ScoresContract.ScoresEntry.DATE_COL +" DESC, "+ ScoresContract.ScoresEntry.TIME_COL +" DESC");

        // manage the cursor
        if (cursor == null) {
            return;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        // loop through all our active widget instances
        for (int appWidgetId : appWidgetIds) {

            // get our layout
            final RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_latest_fixture);

            // home team logo
            setImageViewBitmapFromUrl(
                    views,
                    R.id.home_crest,
                    cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.HOME_LOGO_COL)));

            // home team name
            String homeTeamName = cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.HOME_COL));
            views.setTextViewText(R.id.home_name, homeTeamName);
            views.setTextColor(R.id.home_name, ContextCompat.getColor(getApplicationContext(), R.color.secondary_text));

            // set content description on home team logo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, R.id.home_crest, homeTeamName);
            }

            // score
            views.setTextViewText(R.id.score_textview, Utility.getScores(
                    cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.HOME_GOALS_COL)),
                    cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.AWAY_GOALS_COL))));
            views.setTextColor(R.id.score_textview, ContextCompat.getColor(getApplicationContext(), R.color.secondary_text));

            // match time
            views.setTextViewText(R.id.date_textview, cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.TIME_COL)));
            views.setTextColor(R.id.date_textview, ContextCompat.getColor(getApplicationContext(), R.color.secondary_text));

            // away team logo
            setImageViewBitmapFromUrl(
                    views,
                    R.id.away_crest,
                    cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.AWAY_LOGO_COL)));

            // away team name
            String awayTeamName = cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.AWAY_COL));
            views.setTextViewText(R.id.away_name, awayTeamName);
            views.setTextColor(R.id.away_name, ContextCompat.getColor(getApplicationContext(), R.color.secondary_text));

            // set content description on away team logo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, R.id.away_crest, awayTeamName);
            }

            // launch the app onclick on the widget and pass the fixture details for date and position selection
            Intent launchIntent = new Intent(this, MainActivity.class);
            final Bundle extras = new Bundle();
            extras.putString(LatestFixtureWidgetProvider.SCORES_DATE,
                    cursor.getString(cursor.getColumnIndex(ScoresContract.ScoresEntry.DATE_COL)));
            extras.putInt(TodaysFixturesWidgetProvider.SCORES_MATCH_ID,
                    cursor.getInt(cursor.getColumnIndex(ScoresContract.ScoresEntry.MATCH_ID)));
            launchIntent.putExtras(extras);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, Intent.FILL_IN_ACTION);
            views.setOnClickPendingIntent(R.id.widget_latest_fixtures, pendingIntent);

            // update the widget with the set views
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        // close the cursor when we're done
        cursor.close();
    }

    /**
     * Load an image from a url using glide int a bitmap and set it as the image of a remote imageview
     * @param views RemoteViews
     * @param viewId int
     * @param imageUrl String
     */
    private void setImageViewBitmapFromUrl(RemoteViews views, int viewId, String imageUrl) {

        Bitmap bitmap = null;

        // try to load the image into a bitmap from given url
        try {
            bitmap = Glide.with(LatestFixtureWidgetService.this)
                    .load(imageUrl)
                    .asBitmap()
                    .error(R.drawable.football)
                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            Log.d(LOG_TAG, "Error retrieving image from url: "+ imageUrl, e);
        }

        // if bitmap loaded update the given imageview
        if (bitmap != null) {

            // scale the bitmap down because of the binder limit
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                bitmap = Utility.scaleBitmap(getApplicationContext(), bitmap, 150);
            }

            // set the bitmap image to the view
            views.setImageViewBitmap(viewId, bitmap);
        }
    }

    /**
     * Set the contentdescription on a remote view
     * @param views RemoteViews
     * @param viewId int
     * @param description String
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, int viewId, String description) {
        views.setContentDescription(viewId, description);
    }
}