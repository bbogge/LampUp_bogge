package com.polito.did2017.lampup.utilities;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.polito.did2017.lampup.R;

import java.util.ArrayList;

import static com.polito.did2017.lampup.activities.LampDetailActivity.HSVtoRGBConvertFactor;
import static com.polito.did2017.lampup.activities.LampDetailActivity.colorRemoval;
import static com.polito.did2017.lampup.activities.LampDetailActivity.colorSelection;
import static java.lang.Thread.sleep;

/**
 * Created by marco on 20/06/2018.
 */

public class ColorGridAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Integer> colors;
    private TCPClient tcpClient;
    private Lamp selectedLamp;
    private Switch switchOnOff;
    private SeekBar brigthness;
    private float[] hsv = new float[3];

    public ColorGridAdapter(Context context, ArrayList<Integer> colors, TCPClient tcpClient, Lamp selectedLamp, Switch switchOnOff, SeekBar brigthness) {
        this.context = context;
        this.colors = colors;
        this.tcpClient = tcpClient;
        this.selectedLamp = selectedLamp;
        this.switchOnOff = switchOnOff;
        this.brigthness = brigthness;
    }

    @Override
    public int getCount() {
        return colors.size();
    }

    @Override
    public Object getItem(int i) {
        return colors.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.item_color, null);
        }

        ImageButton circle = view.findViewById(R.id.color);
        circle.setBackgroundColor(colors.get(i));

        final int color = colors.get(i);
        final int index = i;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorSelection(index);
                Color.colorToHSV(color, hsv);
                float Hue = hsv[0]*HSVtoRGBConvertFactor;
                float Saturation = hsv[1]*255.0f;
                //Toast.makeText(context, String.valueOf("setHue" + "$" + Hue), Toast.LENGTH_SHORT).show();
                // aggiorno i dati della lampada e aggiorno la vista
                selectedLamp.turnOn();
                switchOnOff.setChecked( selectedLamp.isOn() );
               /* if(selectedLamp.getBrightness() == 0) {
                    selectedLamp.setBrightness( 10 );
                }
                brigthness.setProgress( selectedLamp.getBrightness() );
                tcpClient.setMessage("setLum" + "$" + selectedLamp.getBrightness());

                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
*/
                tcpClient.setMessage("setHueSat" + "$" + Hue + "$" + Saturation);

            }
        });

        view.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                colorRemoval(index, context);
                return false;
            }
        } );

//        if(view==null) {
//            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            view = li.inflate(R.layout.item_light_color, null);
//        }
//
//        View circle = view.findViewById(R.id.light_color);
//        GradientDrawable light = (GradientDrawable) context.getResources().getDrawable(R.drawable.shape_background_light_color);
//        light.setColor(colors.get(i));
//        circle.setBackground(light);

        return view;
    }
}
