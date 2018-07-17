package com.polito.did2017.lampup.utilities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.polito.did2017.lampup.R;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by marco on 01/07/2018.
 */

public class LampAdapter extends RecyclerView.Adapter<LampAdapter.LampViewHolder> {

    List<Lamp> lamps;
    UDPAsyncTask udpAsyncTask;

    public LampAdapter(List<Lamp> lamps, UDPAsyncTask udpAsyncTask){
        this.lamps = lamps;
        this.udpAsyncTask = udpAsyncTask;
    }

    @Override
    public LampViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lamp_card, parent, false);
        LampViewHolder lvh = new LampViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(final LampViewHolder holder, final int position) {
        holder.lampName.setText(lamps.get(position).getLampName());
        holder.lampPhoto.setImageResource(lamps.get(position).getLampImage());
        holder.lampSwitch.setChecked( lamps.get( position ).isOn() );

        holder.lampSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                lamps.get(position).setState(holder.lampSwitch.isChecked());

                try {
                    udpAsyncTask.sendUDPdatagram( InetAddress.getByName( lamps.get( position ).getLampIP() ), lamps.get( position ).isOn() );
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return lamps.size();
    }


    public class LampViewHolder extends RecyclerView.ViewHolder {
        TextView lampName;
        ImageView lampPhoto;
        Switch lampSwitch;

        LampViewHolder(View itemView) {
            super(itemView);
            lampName = itemView.findViewById(R.id.lamp_name);
            lampPhoto = itemView.findViewById(R.id.lamp_photo);
            lampSwitch = itemView.findViewById(R.id.lamp_switch);

        }
    }
}
