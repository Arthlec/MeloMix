package projet_e3.esiee.com.projet_e3.Fragments;

import android.graphics.Bitmap;
import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import projet_e3.esiee.com.projet_e3.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private ArrayList<Pair<Bitmap, String>> characters = new ArrayList<>();

    public MyAdapter(ArrayList<Bitmap> savedTrackCovers, ArrayList<String> savedTrackNames) {
        for (int i=0; i<savedTrackCovers.size(); i++) {
            characters.add(Pair.create(savedTrackCovers.get(i), savedTrackNames.get(i)));
        }
    }

    @Override
    public int getItemCount() {
        return characters.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_cell, parent, false);

        Button buttonAdd = view.findViewById(R.id.savedMusicMoreButton);
        buttonAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //ajoute la musique actuelle aux musiques personnelles
                Log.i("Salut", "salut");
            }
        });
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Pair<Bitmap, String> pair = characters.get(position);
        holder.display(pair);
    }

    public void removeItem(int position) {
        characters.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView savedTrackCover;
        private final TextView savedTrackName;
        public RelativeLayout viewBackground, viewForeground;

        private Pair<Bitmap, String> currentPair;

        public MyViewHolder(final View itemView) {
            super(itemView);

            savedTrackCover = itemView.findViewById(R.id.savedTrackCover);
            savedTrackName = itemView.findViewById(R.id.savedTrackName);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
        }

        public void display(Pair<Bitmap, String> pair) {
            currentPair = pair;
            Log.i("Paire", pair.toString());
            Log.i("Liste", characters.toString());
            savedTrackCover.setImageBitmap(pair.first);
            savedTrackName.setText(pair.second);
        }
    }
}