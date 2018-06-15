package projet_e3.esiee.com.projet_e3.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.provider.Telephony;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import projet_e3.esiee.com.projet_e3.Activities.HostActivity;
import projet_e3.esiee.com.projet_e3.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

    private ArrayList<Pair<Bitmap, String>> characters = new ArrayList<>();
    private Context mCtx;

    public MyAdapter(Context ctx, ArrayList<Bitmap> savedTrackCovers, ArrayList<String> savedTrackNames) {
        mCtx = ctx;
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
        boolean isHistoryList = parent.toString().contains("history_list");

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_cell, parent, false);

        if (!isHistoryList) {
            Button buttonMore = view.findViewById(R.id.list_cellMoreButton);
            buttonMore.setVisibility(View.GONE);
            buttonMore.setActivated(false);
        }

        final MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.listCellMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(mCtx, holder.listCellMoreButton);
                //inflating menu from xml resource
                popup.inflate(R.menu.history_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.nav_like:
                                Toast.makeText(mCtx, "Votre avis a été pris en compte", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.nav_dislike:
                                Toast.makeText(mCtx, "Votre avis a été pris en compte", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.nav_save_music:
                                final Bitmap cover = holder.currentPair.first;
                                final String name = holder.currentPair.second;
                                if (SavedMusicsFragment.trackNameList.contains(name) && SavedMusicsFragment.trackCoverList.contains(cover)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
                                    builder.setTitle("Ajouter à mes musiques")
                                            .setMessage("Vous avez déjà enregistré cette musique, voulez-vous l'enregistrer à nouveau ?")
                                            .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    SavedMusicsFragment.trackCoverList.add(0, cover);
                                                    SavedMusicsFragment.trackNameList.add(0, name);
                                                    Toast.makeText(mCtx, "Musique ajoutée à vos musiques", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // do nothing
                                                }
                                            })
                                            .show();
                                }
                                else {
                                    SavedMusicsFragment.trackCoverList.add(0, cover);
                                    SavedMusicsFragment.trackNameList.add(0, name);
                                    Toast.makeText(mCtx, "Musique ajoutée à vos musiques", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                //displaying the popup
                popup.show();

            }
        });

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

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private final ImageView savedTrackCover;
        private final TextView savedTrackName;
        private final Button listCellMoreButton;

        public RelativeLayout viewBackground, viewForeground;

        private Pair<Bitmap, String> currentPair;

        public MyViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnCreateContextMenuListener(this);

            savedTrackCover = itemView.findViewById(R.id.savedTrackCover);
            savedTrackName = itemView.findViewById(R.id.savedTrackName);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            listCellMoreButton = itemView.findViewById(R.id.list_cellMoreButton);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            //menu.add(0, v.getId(), 0, "Like");
            //menu.add(0, v.getId(), 0, "Dislike");
            //menu.add(0, v.getId(), 0, "Ajouter à mes musiques");
            HistoryFragment.trackCoverSelected = currentPair.first;
            HistoryFragment.trackNameSelected = currentPair.second;
            HistoryFragment.menuInflater.inflate(R.menu.history_menu, menu);
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