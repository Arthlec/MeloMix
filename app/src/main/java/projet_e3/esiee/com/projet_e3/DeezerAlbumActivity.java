package projet_e3.esiee.com.projet_e3;


import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Album;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.player.AlbumPlayer;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DeezerAlbumActivity extends DeezerPlayerActivity {


    /** The list of albums of displayed by this activity. */
    private List<Album> mAlbumsList = new ArrayList<>();

    private ArrayAdapter<Album> mAlbumsAdapter;

    private AlbumPlayer mAlbumPlayer;

    private void getAlbums() {


        DeezerRequest request = DeezerRequestFactory.requestCurrentUserAlbums();
        mDeezerConnect.requestAsync(request, new JsonRequestListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void onResult(final Object result, final Object requestId) {

                mAlbumsList.clear();

                try {
                    mAlbumsList.addAll((List<Album>) result);
                }
                catch (ClassCastException e) {
                    handleError(e);
                }

                if (mAlbumsList.isEmpty()) {
                    Toast.makeText(DeezerAlbumActivity.this, "You don't have album", Toast.LENGTH_LONG).show();
                }
                Log.d("list", mAlbumsList.toString());
            }

            @Override
            public void onUnparsedResult(final String response, final Object requestId) {
                handleError(new DeezerError("Unparsed reponse"));
            }


            @Override
            public void onException(final Exception exception,
                                    final Object requestId) {
                handleError(exception);
            }


        });
    }

    /**
     * Creates the PlaylistPlayer
     */
    private void createPlayer() {
        try {
            mAlbumPlayer = new AlbumPlayer(getApplication(), mDeezerConnect,
                    new WifiAndMobileNetworkStateChecker());
            //mAlbumPlayer.addPlayerListener(this /*il faut mettre un listener*/);
            setAttachedPlayer(mAlbumPlayer);
        }
       /*catch (OAuthException e) {
            handleError(e);
        }*/
        catch (TooManyPlayersExceptions e) {
            handleError(e);
        }
        catch (DeezerError e) {
            handleError(e);
        }
    }

    public void setUpAlbums() {
        mAlbumsAdapter = new ArrayAdapter<Album>(this, R.layout.deezer_album_activity, mAlbumsList) {

            @Override
            public View getView(final int position, final View convertView, final ViewGroup parent) {
                Album album = getItem(position);

                View view = convertView;
                if (view == null) {
                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = vi.inflate(R.layout.deezer_album_activity, null);
                }


                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(album.getTitle());

                ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
                Picasso.with(DeezerAlbumActivity.this).load(album.getImageUrl(AImageOwner.ImageSize.small))
                        .into(imageView);

                return view;
            }
        };
        ListView listview = (ListView) findViewById(android.R.id.list);
        listview.setAdapter(mAlbumsAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                                    final int position, final long id) {
                Album album = mAlbumsList.get(position);
                mAlbumPlayer.playAlbum(album.getId(), 104);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deezer_album_activity);
        new SessionStore().restore(mDeezerConnect, this);
        createPlayer();

        //fetch albulm list
        getAlbums();

        setUpAlbums();
    }
}
