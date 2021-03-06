package com.polito.did2017.lampup.utilities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;

import com.polito.did2017.lampup.R;
import com.squareup.picasso.Picasso;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by marco on 01/07/2018.
 */

public class LampAdapter extends RecyclerView.Adapter<LampAdapter.LampViewHolder> {

    private Context context;
    private List<Lamp> lamps;
    private UDPAsyncTask udpAsyncTask;
    private static final String SWITCH_PREF = "LastSwitchState";
    private Dictionary<InetAddress, Boolean> lampPreviousState;
    private LampManager lm = LampManager.getInstance();

    public LampAdapter(List<Lamp> lamps, UDPAsyncTask udpAsyncTask, Context context) {
        this.context = context;
        this.lamps = lamps;
        this.udpAsyncTask = udpAsyncTask;
        lampPreviousState = new Hashtable<>();
    }

    @Override
    public LampViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_lamp_card, parent, false );
        LampViewHolder lvh = new LampViewHolder( v );

        for (int i = 0; i < lamps.size(); i++) {
            if (lamps.get( i ).getLampName().equals( "gyro_lamp" )) {
                lamps.get( i ).setState( PreferenceManager.getDefaultSharedPreferences( context ).getBoolean( SWITCH_PREF, false ) );
            }
        }

        return lvh;
    }

    @Override
    public void onBindViewHolder(final LampViewHolder holder, final int position) {

        holder.lampName.setText( lm.convertName( lamps.get( position ).getLampName() ) );

        if(lamps.get( position ).getLampImage() == "default") {
            holder.lampPhoto.setImageResource( R.drawable.ic_lightbulb_outline_black_24dp);
        } else {
            Picasso.get().load(lamps.get( position ).getLampImage()).into(holder.lampPhoto);
        }

        holder.lampSwitch.setChecked( lamps.get( position ).isOn() );
        holder.lampSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                lamps.get( position ).setState( holder.lampSwitch.isChecked() );

                Log.d( "UDP SENDER", "Sono al listener del check" );
                try {
                    udpAsyncTask.sendUDPdatagram( InetAddress.getByName( lamps.get( position ).getLampIP() ), lamps.get( position ).isOn() );
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        } );
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
            super( itemView );
            lampName = itemView.findViewById( R.id.lamp_name );
            lampPhoto = itemView.findViewById( R.id.lamp_photo );
            lampSwitch = itemView.findViewById( R.id.lamp_switch );

        }
    }
}
