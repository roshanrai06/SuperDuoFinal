package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;
import barqsoft.footballscores.data.ScoresContract;

public class FootballDataService extends IntentService {

    // tag logging with classname
    private static final String LOG_TAG = FootballDataService.class.getSimpleName();

    // leagues we want to include
    private static int[] LEAGUE_CODES;

    private Vector<ContentValues> mTeamsVector;

    // football-data.org api key
    private String mApiKey;

    /**
     * Constructor
     */
    public FootballDataService() {
        super(LOG_TAG);
    }

    /**
     * Handle the given intent
     * @param intent Intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // get selected leagues from resources array
        LEAGUE_CODES = getResources().getIntArray(R.array.leagues_selected);

        // keep the teams details (logo urls)
        mTeamsVector = new Vector <ContentValues> ();

        // get the given extra football-data.org api key
        mApiKey = intent.getStringExtra(getString(R.string.pref_apikey_key));

        // check if we have a network connection
        if (Utility.isNetworkAvailable(getApplicationContext())) {

            // load the team logos
            loadTeams();

            // load fixtures for last- and next 2 days
            loadFixtures("p2");
            loadFixtures("n3");
        }
    }

    /**
     * Load the team details for selected leagues and store the team id with the logo url as a list
     */
    private void loadTeams() {

        // for each league get the teams
        for (final int code : LEAGUE_CODES) {
            try {
                // construct api teams query url by adding the soccerseasons, leaguecode, and teams path
                URL queryTeamsUrl = new URL(Uri.parse(getString(R.string.config_footballdata_api_base_url))
                        .buildUpon()
                        .appendPath(getString(R.string.config_footballdata_api_seasons))
                        .appendPath(Integer.toString(code))
                        .appendPath(getString(R.string.config_footballdata_api_teams))
                        .build()
                        .toString());

                // query the api and get the teams
                String teams = queryFootballDataApi(queryTeamsUrl);

                // process the returned json data
                if (teams != null) {
                    processTeams(teams);
                } else {
                    Log.d(LOG_TAG, getString(R.string.failed_loading_teams) +": "+ code);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception here in loadTeams: " + e.getMessage());
            }
        }
    }

    /**
     * Convert loaded json teams data to list of team id and team logo url
     * @param teamsString String
     */
    private void processTeams(String teamsString) {

        // json element names
        final String TEAMS = getString(R.string.config_footballdata_api_teams);
        final String LINKS = "_links";
        final String SELF = "self";
        final String CREST_URL = "crestUrl";

        final String SELF_LINK = getString(R.string.config_footballdata_api_base_url) +"/"+ getString(R.string.config_footballdata_api_teams) +"/";

        // get teams and add the id and logo url to the vector
        try {
            JSONArray teams = new JSONObject(teamsString).getJSONArray(TEAMS);

            if (teams.length() > 0) {
                for(int i = 0;i < teams.length();i++) {

                    // get the team
                    JSONObject team = teams.getJSONObject(i);

                    // extract the team id from href in links.self
                    String teamId = team.getJSONObject(LINKS).getJSONObject(SELF).getString("href");
                    teamId = teamId.replace(SELF_LINK, "");

                    // get the cresturl
                    String teamLogoUrl = team.getString(CREST_URL);

                    // optionally convert .svg urls to .png urls
                    //  android does not work well with .svg graphics (at least a library is needed)
                    //  luckily wikimedia has an option to serve alternative versions of an image
                    //  the algorithm is to add 'thumb/' in the path and
                    //  add the required filetype plus the px resolution as a pre-fix of the filename:
                    //  SVG original:   https://upload.wikimedia.org/wikipedia/de/d/d8/Heracles_Almelo.svg
                    //  PNG 200px:	    https://upload.wikimedia.org/wikipedia/de/thumb/d/d8/Heracles_Almelo.svg/200px-Heracles_Almelo.svg.png
                    if (teamLogoUrl != null && teamLogoUrl.endsWith(".svg")) {
                        String svgLogoUrl = teamLogoUrl;
                        String filename = svgLogoUrl.substring(svgLogoUrl.lastIndexOf("/") + 1);
                        int wikipediaPathEndPos = svgLogoUrl.indexOf("/wikipedia/") + 11;
                        String afterWikipediaPath = svgLogoUrl.substring(wikipediaPathEndPos);
                        int thumbInsertPos = wikipediaPathEndPos + afterWikipediaPath.indexOf("/") + 1;
                        String afterLanguageCodePath = svgLogoUrl.substring(thumbInsertPos);
                        teamLogoUrl = svgLogoUrl.substring(0, thumbInsertPos);
                        teamLogoUrl += "thumb/" + afterLanguageCodePath;
                        teamLogoUrl += "/200px-" + filename + ".png";
                    }

                    // create contentvalues object containing the team details
                    ContentValues teamValues = new ContentValues();
                    teamValues.put(ScoresContract.ScoresEntry.HOME_ID_COL, Integer.parseInt(teamId));
                    teamValues.put(ScoresContract.ScoresEntry.HOME_LOGO_COL, teamLogoUrl);

                    // add team to the vector
                    mTeamsVector.add(teamValues);
                }
            } else {
                Log.e(LOG_TAG, "No teams found");
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Query the football-data api for matches in given timeframe, process the result json, and
     *  store them in the database
     * @param timeFrame String
     */
    private void loadFixtures(String timeFrame) {

        try {
            // construct api fixtures query url by adding the fixtures path and timeframe param to the base url
            URL queryFixturesUrl = new URL(Uri.parse(getString(R.string.config_footballdata_api_base_url))
                    .buildUpon()
                    .appendPath(getString(R.string.config_footballdata_api_fixtures))
                    .appendQueryParameter(getString(R.string.config_footballdata_api_param_timeframe), timeFrame)
                    .build()
                    .toString());

            // get match data for the next 2 days from api
            String fixtures = queryFootballDataApi(queryFixturesUrl);

            // process the returned json data and insert found matches into the database
            if (fixtures != null) {
                getApplicationContext().getContentResolver().bulkInsert(
                        ScoresContract.BASE_CONTENT_URI,
                        processFixtures(fixtures));
            } else {
                Log.d(LOG_TAG, getString(R.string.failed_loading_teams));
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception here in loadFixtures: " + e.getMessage());
        }
    }

    /**
     * Convert api fixtures json data to array of matches
     * @param fixturesString String
     */
    private ContentValues[] processFixtures(String fixturesString) {

        ContentValues[] fixturesArray = null;

        // indicator for real or dummy data
        boolean isReal = true;

        // json element names
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";
        final String HOME_TEAM_ID = "homeTeam";
        final String AWAY_TEAM_ID = "awayTeam";

        final String SEASON_LINK = getString(R.string.config_footballdata_api_base_url) +"/"+
                getString(R.string.config_footballdata_api_seasons) +"/";
        final String MATCH_LINK = getString(R.string.config_footballdata_api_base_url) +"/"+
                getString(R.string.config_footballdata_api_fixtures) +"/";
        final String TEAM_LINK = getString(R.string.config_footballdata_api_base_url) +"/"+
                getString(R.string.config_footballdata_api_teams) +"/";

        // get matches and convert them to an array
        try {
            JSONArray fixtures = new JSONObject(fixturesString).getJSONArray(FIXTURES);

            // load dummy data if no matches found
            if (fixtures.length() == 0) {
                fixturesString = getString(R.string.dummy_data);
                fixtures = new JSONObject(fixturesString).getJSONArray(FIXTURES);
                isReal = false;
            }

            // create contentvalues vector with length of amount of matches
            Vector<ContentValues> fixturesVector = new Vector <ContentValues> (fixtures.length());

            for(int i = 0;i < fixtures.length(); i++) {

                // get the match
                JSONObject fixture = fixtures.getJSONObject(i);

                // extract league from href in links.soccerseason
                String leagueValue = fixture.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).getString("href");
                leagueValue = leagueValue.replace(SEASON_LINK, "");
                int league = Integer.parseInt(leagueValue);

                // only include matches from selected leagues
                if (Utility.contains(LEAGUE_CODES, league)) {

                    // extract the match id from href in links.self
                    String matchId = fixture.getJSONObject(LINKS).getJSONObject(SELF).getString("href");
                    matchId = matchId.replace(MATCH_LINK, "");

                    // extract the home team id from href in links.homeTeam
                    String homeTeamIdString = fixture.getJSONObject(LINKS).getJSONObject(HOME_TEAM_ID).getString("href");
                    homeTeamIdString = homeTeamIdString.replace(TEAM_LINK, "");
                    int homeTeamId = Integer.parseInt(homeTeamIdString);

                    // extract the away team id from href in links.awayTeam
                    String awayTeamIdString = fixture.getJSONObject(LINKS).getJSONObject(AWAY_TEAM_ID).getString("href");
                    awayTeamIdString = awayTeamIdString.replace(TEAM_LINK, "");
                    int awayTeamId = Integer.parseInt(awayTeamIdString);

                    // increment the match id of the dummy data (makes it unique)
                    if (!isReal) {
                        matchId = matchId + Integer.toString(i);
                    }

                    // get the date and time from match date field
                    String date = fixture.getString(MATCH_DATE);
                    String time = date.substring(date.indexOf("T") + 1, date.indexOf("Z"));
                    date = date.substring(0, date.indexOf("T"));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.US);
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    // convert date and time to local datetime and extract date and time again
                    try {
                        Date parsedDate = simpleDateFormat.parse(date + time);
                        SimpleDateFormat newDate = new SimpleDateFormat("yyyy-MM-dd:HH:mm", Locale.US);
                        newDate.setTimeZone(TimeZone.getDefault());
                        date = newDate.format(parsedDate);
                        time = date.substring(date.indexOf(":") + 1);
                        date = date.substring(0, date.indexOf(":"));

                        // change the dummy data's date to match current date range
                        if(!isReal) {
                            Date dummyDate = new Date(System.currentTimeMillis() + ((i-2)*86400000));
                            SimpleDateFormat dummyDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            date = dummyDateFormat.format(dummyDate);
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    // create contentvalues object containing the match details
                    ContentValues fixtureValues = new ContentValues();
                    fixtureValues.put(ScoresContract.ScoresEntry.MATCH_ID, matchId);
                    fixtureValues.put(ScoresContract.ScoresEntry.DATE_COL, date);
                    fixtureValues.put(ScoresContract.ScoresEntry.TIME_COL, time);
                    fixtureValues.put(ScoresContract.ScoresEntry.HOME_COL, fixture.getString(HOME_TEAM));
                    fixtureValues.put(ScoresContract.ScoresEntry.HOME_ID_COL, homeTeamId);
                    fixtureValues.put(ScoresContract.ScoresEntry.HOME_LOGO_COL, getTeamLogoById(homeTeamId));
                    fixtureValues.put(ScoresContract.ScoresEntry.HOME_GOALS_COL, fixture.getJSONObject(RESULT).getString(HOME_GOALS));
                    fixtureValues.put(ScoresContract.ScoresEntry.AWAY_COL, fixture.getString(AWAY_TEAM));
                    fixtureValues.put(ScoresContract.ScoresEntry.AWAY_ID_COL, awayTeamId);
                    fixtureValues.put(ScoresContract.ScoresEntry.AWAY_LOGO_COL, getTeamLogoById(awayTeamId));
                    fixtureValues.put(ScoresContract.ScoresEntry.AWAY_GOALS_COL, fixture.getJSONObject(RESULT).getString(AWAY_GOALS));
                    fixtureValues.put(ScoresContract.ScoresEntry.LEAGUE_COL, league);
                    fixtureValues.put(ScoresContract.ScoresEntry.MATCH_DAY, fixture.getString(MATCH_DAY));

                    // add match to the vector
                    fixturesVector.add(fixtureValues);
                }
            }

            // convert vector to array
            fixturesArray = new ContentValues[fixturesVector.size()];
            fixturesVector.toArray(fixturesArray);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Exception here in processFixtures: " + e.getMessage());
        }

        return fixturesArray;
    }

    /**
     * Get the team logo url from the teams vector by given team id
     * @param teamId int
     * @return String
     */
    private String getTeamLogoById(int teamId) {

        // loop through the teams and return logo url when team id found
        for (ContentValues team: mTeamsVector) {
            if (team.getAsInteger(ScoresContract.ScoresEntry.HOME_ID_COL).equals(teamId)) {
                return team.getAsString(ScoresContract.ScoresEntry.HOME_LOGO_COL);
            }
        }

        return "";
    }

    /**
     * Query the football-data.org api with given url and return the result as a string
     * @param apiUrl URL
     */
    private String queryFootballDataApi(URL apiUrl) {

        String apiResult = null;

        // check if we have a network connection
        if (Utility.isNetworkAvailable(getApplicationContext())) {

            HttpURLConnection apiConnection = null;
            BufferedReader apiReader = null;

            if (apiUrl != null) {
                try {

                    Log.d(LOG_TAG, "=============> querying api: " + apiUrl.toString());

                    // open connection
                    apiConnection = (HttpURLConnection) apiUrl.openConnection();
                    apiConnection.setRequestMethod("GET");
                    apiConnection.addRequestProperty("X-Auth-Token", mApiKey);
                    apiConnection.connect();

                    // read the input stream into a string
                    InputStream inputStream = apiConnection.getInputStream();
                    if (inputStream == null) {
                        return null;
                    }

                    // read the result from the inputstream into a buffer
                    apiReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder buffer = new StringBuilder();
                    String line;
                    while ((line = apiReader.readLine()) != null) {
                        buffer.append(line);
                    }

                    // get the result as a string
                    if (buffer.length() > 0) {
                        apiResult = buffer.toString();
                    } else {
                        return null;
                    }

                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception here in queryFootballDataApi: " + e.getMessage());
                } finally {

                    // disconnect the api connection and close the reader
                    if (apiConnection != null) {
                        apiConnection.disconnect();
                    }
                    if (apiReader != null) {
                        try {
                            apiReader.close();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Error Closing Stream");
                        }
                    }
                }
            }
        } else {
            return null;
        }

        return apiResult;
    }
}