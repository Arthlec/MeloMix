package projet_e3.esiee.com.projet_e3.Fragments;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import projet_e3.esiee.com.projet_e3.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    public static HistoryFragment newInstance() {
        return (new HistoryFragment());
    }

    public static ArrayList<Bitmap> trackCoverList = new ArrayList<>();
    public static ArrayList<String> trackNameList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        RecyclerView rv = view.findViewById(R.id.history_list);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new MyAdapter(trackCoverList, trackNameList));

        // Inflate the layout for this fragment
        return view;
    }

}
