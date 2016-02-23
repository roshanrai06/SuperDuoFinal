package barqsoft.footballscores;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends Fragment {

    /**
     * Constructor
     */
    public AboutFragment() {
    }

    /**
     * Inflate the about_fragment layout containing the about text from strings resource
     * @param inflater LayoutInflator
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // get the about fragment
        return inflater.inflate(R.layout.about_fragment, container, false);
    }
}
