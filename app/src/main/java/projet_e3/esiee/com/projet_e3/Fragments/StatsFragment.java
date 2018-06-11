package projet_e3.esiee.com.projet_e3.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import projet_e3.esiee.com.projet_e3.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment {


    public static StatsFragment newInstance() {
        return (new StatsFragment());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

}
