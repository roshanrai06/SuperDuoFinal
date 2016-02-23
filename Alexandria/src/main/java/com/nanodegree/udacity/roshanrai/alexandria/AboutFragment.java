package com.nanodegree.udacity.roshanrai.alexandria;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by roshan.rai on 2/9/2016.
 */
public class AboutFragment extends Fragment {
    /**
     * Constructor
     */
    public AboutFragment(){
    }

    /**
     * Set the layout fragment
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // set the about fragment as the root view
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        // set the title of the mainactivity actionbar
        getActivity().setTitle(R.string.about);

        return rootView;
    }
}
