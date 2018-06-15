package projet_e3.esiee.com.projet_e3.Fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import java.util.ArrayList;

import projet_e3.esiee.com.projet_e3.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    public static HistoryFragment newInstance() {
        return (new HistoryFragment());
    }

    public static MenuInflater menuInflater;
    public static ArrayList<Bitmap> trackCoverList = new ArrayList<>();
    public static ArrayList<String> trackNameList = new ArrayList<>();
    public static Bitmap trackCoverSelected;
    public static String trackNameSelected;
    private OnSaveMusicHistorySelectedListener mSaveMusicHistoryListener;

    public interface OnSaveMusicHistorySelectedListener {
        void onSaveMusicHistorySelected(Bitmap trackCover, String trackName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        RecyclerView rv = view.findViewById(R.id.history_list);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new MyAdapter(getContext(), trackCoverList, trackNameList));

        registerForContextMenu(rv);

        menuInflater = getActivity().getMenuInflater();

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_like:
                //Log.i("Action", "Like");
                return true;
            case R.id.nav_dislike:
                //Log.i("Action", "Dislike");
                return true;
            case R.id.nav_save_music:
                mSaveMusicHistoryListener.onSaveMusicHistorySelected(trackCoverSelected, trackNameSelected);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mSaveMusicHistoryListener = (OnSaveMusicHistorySelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + e.getMessage());
        }
    }
}
