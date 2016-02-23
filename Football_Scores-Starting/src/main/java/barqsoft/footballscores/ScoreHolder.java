package barqsoft.footballscores;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScoreHolder
{
    // match
    public TextView mHomeName;
    public TextView mAwayName;
    public TextView mScore;
    public TextView mDate;
    public ImageView mHomeCrest;
    public ImageView mAwayCrest;

    // details and sharing
    public LinearLayout mMatchDetails;
    public TextView mLeague;
    public TextView mMatchDay;
    public Button mShareButton;

    public ScoreHolder(View view)
    {
        mHomeName = (TextView) view.findViewById(R.id.home_name);
        mAwayName = (TextView) view.findViewById(R.id.away_name);
        mScore = (TextView) view.findViewById(R.id.score_textview);
        mDate = (TextView) view.findViewById(R.id.date_textview);
        mHomeCrest = (ImageView) view.findViewById(R.id.home_crest);
        mAwayCrest = (ImageView) view.findViewById(R.id.away_crest);

        mMatchDetails = (LinearLayout) view.findViewById(R.id.match_details);
        mLeague = (TextView) view.findViewById(R.id.league_textview);
        mMatchDay = (TextView) view.findViewById(R.id.matchday_textview);
        mShareButton = (Button) view.findViewById(R.id.share_button);
    }
}