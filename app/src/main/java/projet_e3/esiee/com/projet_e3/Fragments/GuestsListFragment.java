package projet_e3.esiee.com.projet_e3.Fragments;


import android.net.wifi.p2p.WifiP2pGroup;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import projet_e3.esiee.com.projet_e3.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GuestsListFragment extends Fragment {

    WifiP2pGroup wifiP2pGroup;


    public static GuestsListFragment newInstance() {
        return (new GuestsListFragment());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_guests_list, container, false);

        RecyclerView rv = view.findViewById(R.id.guest_list);

        return view;
    }

    public void setWifiP2PGroup(WifiP2pGroup group) {
        wifiP2pGroup = group;
    }
}
