package projet_e3.esiee.com.projet_e3.Fragments;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import projet_e3.esiee.com.projet_e3.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private static View view;
    private static Bitmap bmpCover;
    private static Bitmap bmpNextCover;
    private static String name;
    private static String nextName;

    public static MainFragment newInstance() {
        return (new MainFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        ImageView trackCover = view.findViewById(R.id.trackCover);
        trackCover.setImageBitmap(bmpCover);
        TextView trackNameField = view.findViewById(R.id.trackName);
        trackNameField.setText(name);
        ImageView nextTrackCover = view.findViewById(R.id.nextTrackCover);
        nextTrackCover.setImageBitmap(bmpNextCover);
        TextView nextTrackNameField = view.findViewById(R.id.nextTrackName);
        nextTrackNameField.setText(nextName);

        int host = getActivity().getIntent().getIntExtra("host", 0);

        Button buttonAdd = view.findViewById(R.id.moreButton);
        buttonAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //ajoute la musique actuelle aux musiques personnel
            }
        });

        Button buttonDislike = view.findViewById(R.id.dislikeButton);
        buttonDislike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //dislike la musique actuelle
            }
        });

        Button buttonLike = view.findViewById(R.id.likeButton);
        buttonLike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //like la musique actuelle
            }
        });


        Button buttonNext = view.findViewById(R.id.buttonNext);
        Button buttonBack = view.findViewById(R.id.buttonBack);
        if (/*Si c'est un guest*/ host == 0) {
            buttonNext.setVisibility(View.GONE);
            buttonNext.setActivated(false);
            buttonBack.setVisibility(View.GONE);
            buttonBack.setActivated(false);

        } else if (/*Si c'est un host*/host == 1) {
            buttonNext.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //passe a la musique suivante
                }
            });

            buttonBack.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //revient a la musique precedente
                }
            });
        }

        return view;
    }

    public static void updateCovers(Bitmap bmpTrackCover, String trackName, Bitmap bmpNextTrackCover, String nextTrackName) {
        ImageView trackCover = view.findViewById(R.id.trackCover);
        trackCover.setImageBitmap(bmpTrackCover);
        bmpCover = bmpTrackCover;
        TextView trackNameField = view.findViewById(R.id.trackName);
        trackNameField.setText(trackName);
        name = trackName;
        ImageView nextTrackCover = view.findViewById(R.id.nextTrackCover);
        nextTrackCover.setImageBitmap(bmpNextTrackCover);
        bmpNextCover = bmpNextTrackCover;
        TextView nextTrackNameField = view.findViewById(R.id.nextTrackName);
        nextTrackNameField.setText(nextTrackName);
        nextName = nextTrackName;
    }
}
