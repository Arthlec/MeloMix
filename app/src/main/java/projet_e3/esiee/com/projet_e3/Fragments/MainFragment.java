package projet_e3.esiee.com.projet_e3.Fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Scene;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import projet_e3.esiee.com.projet_e3.Activities.MainActivity;
import projet_e3.esiee.com.projet_e3.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private Scene basicScene;
    private Scene dislikeScene;
    private Scene likeScene;
    private static View view;
    private static Bitmap bmpCover;
    private static Bitmap bmpNextCover;
    private static String name;
    private static String nextName;
    private static boolean songLiked = false;
    private OnSavedMusicSelectedListener mSavedMusicListener;
    private OnArrowListener mArrowListener;

    public static MainFragment newInstance() {
        return (new MainFragment());
    }

    public interface OnSavedMusicSelectedListener {
        void onSavedMusicSelected();
    }

    public interface OnArrowListener {
        void onArrowSelected(String direction);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        final ViewGroup mSceneRoot = view.findViewById(R.id.like_dislike_root);
        basicScene = Scene.getSceneForLayout(mSceneRoot, R.layout.basic_like_dislike, getContext());
        dislikeScene = Scene.getSceneForLayout(mSceneRoot, R.layout.final_dislike, getContext());
        likeScene = Scene.getSceneForLayout(mSceneRoot, R.layout.final_like, getContext());

        ImageView trackCover = view.findViewById(R.id.trackCover);
        trackCover.setImageBitmap(bmpCover);
        TextView trackNameField = view.findViewById(R.id.trackName);
        trackNameField.setText(name);
        ImageView nextTrackCover = view.findViewById(R.id.nextTrackCover);
        nextTrackCover.setImageBitmap(bmpNextCover);
        TextView nextTrackNameField = view.findViewById(R.id.nextTrackName);
        nextTrackNameField.setText(nextName);
        ProgressBar nextTrackLoading = view.findViewById(R.id.nextTrackLoading);
        nextTrackLoading.setVisibility(View.INVISIBLE);
        nextTrackLoading.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);

        if(songLiked){
            mSceneRoot.findViewById(R.id.likeButton).setVisibility(View.INVISIBLE);
            mSceneRoot.findViewById(R.id.dislikeButton).setVisibility(View.INVISIBLE);
        }

        setListeners();

        return view;
    }

    public void setListeners() {
        final ViewGroup mSceneRoot = view.findViewById(R.id.like_dislike_root);
        final android.transition.Transition mLikeDislikeTransition =
                TransitionInflater.from(getContext()).
                        inflateTransition(R.transition.like_dislike_transition);
        mLikeDislikeTransition.setDuration(1500);
        final android.transition.Transition mFadeTransition =
                TransitionInflater.from(getContext()).
                        inflateTransition(R.transition.fade_transition);
        mLikeDislikeTransition.setDuration(800);

        int host = getActivity().getIntent().getIntExtra("host", 0);

        Button buttonAdd = view.findViewById(R.id.moreButton);
        buttonAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //ajoute la musique actuelle aux musiques personnelles
                mSavedMusicListener.onSavedMusicSelected();
            }
        });

        final Button buttonDislike = mSceneRoot.findViewById(R.id.dislikeButton);
        buttonDislike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //dislike la musique actuelle
                TransitionManager.go(dislikeScene, mLikeDislikeTransition);
                Toast.makeText(getActivity().getApplicationContext(), "Votre avis a été pris en compte", Toast.LENGTH_SHORT).show();
                songLiked = true;
                setListeners();
            }
        });
        Button buttonLike = mSceneRoot.findViewById(R.id.likeButton);
        buttonLike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //like la musique actuelle
                TransitionManager.go(likeScene, mLikeDislikeTransition);
                Toast.makeText(getActivity().getApplicationContext(), "Votre avis a été pris en compte", Toast.LENGTH_SHORT).show();
                songLiked = true;
                setListeners();
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
                    mArrowListener.onArrowSelected("next");
                    TransitionManager.go(basicScene, mFadeTransition);
                    songLiked = false;
                    setListeners();
                }
            });

            buttonBack.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //revient a la musique precedente
                    mArrowListener.onArrowSelected("back");
                    TransitionManager.go(basicScene, mFadeTransition);
                    songLiked = false;
                    setListeners();
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mSavedMusicListener = (OnSavedMusicSelectedListener) context;
            mArrowListener = (OnArrowListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + e.getMessage());
        }
    }

    public static void updateCovers(Bitmap bmpTrackCover, String trackName, Bitmap bmpNextTrackCover, String nextTrackName) {
        ImageView trackCover = view.findViewById(R.id.trackCover);
        trackCover.setImageBitmap(bmpTrackCover);
        bmpCover = bmpTrackCover;
        TextView trackNameField = view.findViewById(R.id.trackName);
        trackNameField.setText(trackName);
        name = trackName;
        ImageView nextTrackCover = view.findViewById(R.id.nextTrackCover);
        TextView nextTrackNameField = view.findViewById(R.id.nextTrackName);
        if ((bmpNextTrackCover != null) && (nextTrackName != null)) {
            view.findViewById(R.id.nextTrackLoading).setVisibility(View.GONE);
            nextTrackCover.setImageBitmap(bmpNextTrackCover);
            bmpNextCover = bmpNextTrackCover;
            nextTrackNameField.setText(nextTrackName);
            nextName = nextTrackName;
        }
        else {
            view.findViewById(R.id.nextTrackLoading).setVisibility(View.VISIBLE);
            nextTrackCover.setImageBitmap(bmpNextTrackCover);
            nextTrackNameField.setText(nextTrackName);
        }
    }
}
