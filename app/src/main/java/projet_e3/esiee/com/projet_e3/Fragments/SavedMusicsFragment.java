package projet_e3.esiee.com.projet_e3.Fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.net.URL;
import java.util.ArrayList;

import projet_e3.esiee.com.projet_e3.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SavedMusicsFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {


    public static SavedMusicsFragment newInstance() {
        return (new SavedMusicsFragment());
    }

    public static ArrayList<Bitmap> trackCoverList = new ArrayList<>();
    public static ArrayList<String> trackNameList = new ArrayList<>();
    private MyAdapter myAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_saved_musics, container, false);

        RecyclerView rv = view.findViewById(R.id.saved_music_list);
        myAdapter = new MyAdapter(trackCoverList, trackNameList);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(myAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rv);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof MyAdapter.MyViewHolder) {
            // remove the item from recycler view
            myAdapter.removeItem(viewHolder.getAdapterPosition());
            // remove the item from the lists
            trackCoverList.remove(position);
            trackNameList.remove(position);
        }
    }

}


