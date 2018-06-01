package projet_e3.esiee.com.projet_e3.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import projet_e3.esiee.com.projet_e3.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SavedMusicsFragment extends Fragment {


    public static SavedMusicsFragment newInstance() {
        return (new SavedMusicsFragment());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_saved_musics, container, false);

        final RecyclerView rv = view.findViewById(R.id.list);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new MyAdapter());

        // Inflate the layout for this fragment
        return view;
    }

}


