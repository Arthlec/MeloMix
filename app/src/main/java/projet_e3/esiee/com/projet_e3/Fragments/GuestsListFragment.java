package projet_e3.esiee.com.projet_e3.Fragments;


import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.gsm.GsmCellLocation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import projet_e3.esiee.com.projet_e3.Activities.LoadingHostActivity;
import projet_e3.esiee.com.projet_e3.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GuestsListFragment extends Fragment {

    public static GuestsListFragment newInstance() {
        return (new GuestsListFragment());
    }

    private WifiP2pGroup wifiP2pGroup;
    private WifiP2pManager aManager;
    private WifiP2pManager.Channel aChannel;
    private WifiP2pManager.GroupInfoListener groupInfoListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_guests_list, container, false);

        final RecyclerView rv = view.findViewById(R.id.list);
        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        final GuestListAdapter adapter = new GuestListAdapter(wifiP2pGroup);
        rv.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                aManager.requestGroupInfo(aChannel,groupInfoListener);
                adapter.clearList();
                adapter.refreshList(wifiP2pGroup);
                adapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    public void setWifiP2PGroup(WifiP2pGroup group) {
        wifiP2pGroup = group;
    }

    public void setManager(WifiP2pManager manager) {
        aManager = manager;
    }

    public void setChannel(WifiP2pManager.Channel channel){
        aChannel = channel;
    }

    public void setGroupInfoListener(WifiP2pManager.GroupInfoListener groupInfo){
        groupInfoListener = groupInfo;
    }
}
