package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TodaysFixturesWidgetService extends RemoteViewsService {

    /**
     * Create the TodaysFixturesRemoteViewsFactory
     * @param intent Intent
     * @return TodaysFixturesRemoteViewsFactory
     */
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodaysFixturesRemoteViewsFactory(this.getApplicationContext());
    }
}