package projet_e3.esiee.com.projet_e3.Fragments;

import android.graphics.Bitmap;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import projet_e3.esiee.com.projet_e3.Activities.HostActivity;
import projet_e3.esiee.com.projet_e3.R;

public class GuestListAdapter extends RecyclerView.Adapter<GuestListAdapter.MyViewHolder> {

    private ArrayList<WifiP2pDevice> list = new ArrayList<>();

    public GuestListAdapter(WifiP2pGroup guestGroup) {
        Collection<WifiP2pDevice> guestList = guestGroup.getClientList();
        Iterator<WifiP2pDevice> iterator = guestList.iterator();
        while(iterator.hasNext()) {
            list.add(iterator.next());
        }
    }

    @NonNull
    @Override
    public GuestListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.guest, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuestListAdapter.MyViewHolder holder, int position) {
        WifiP2pDevice guest = list.get(position);
        holder.display(guest);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView guestName;

        public MyViewHolder(final View itemView) {
            super(itemView);

            guestName = itemView.findViewById(R.id.guest_name);
        }

        public void display(WifiP2pDevice device) {
            guestName.setText(device.deviceName);
        }

    }
}
