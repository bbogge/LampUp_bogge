package com.polito.did2017.lampup.utilities;

import android.util.Log;

import com.polito.did2017.lampup.R;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by matil on 30/11/2017.
 */

public class LampManager {

    private static final LampManager instance = new LampManager();
    private List<Lamp> lampList;

    // private constructor to avoid client applications to use constructor
    private LampManager() {
        lampList = Collections.synchronizedList( new ArrayList<Lamp>());
    }

    public static LampManager getInstance() {
        return instance;
    }

    public List<Lamp> getLamps() {
        return lampList;
    }

    public void addLamp(String lamp_ip, String lamp_name) {
        for (int i = 0; i < lampList.size(); i++) {
            if(lampList.get( i ).getLampIP().equals( lamp_ip )) {
                return;
            }
        }
        lampList.add( new Lamp(lamp_ip, lamp_name,  getImgURL( lamp_name ) ));
    }

    public String getImgURL(String lampName) {
        switch (lampName) {
            case "gyro_lamp":
                //return "https://i.imgur.com/L4nA9nS.jpg?1";
                return "https://i.imgur.com/684ZzZt.jpg?3";
            default:
                return "default";
        }
    }

    public void discover(final UDPAsyncTask udpAsyncTask) {

        Log.d("!!!debug", "sono in discover");
        udpAsyncTask.execute();

    }

    public CharSequence convertName(String lampName) {

        String[] parts = lampName.split("_");
        StringBuilder newLampName = new StringBuilder();
        for (String part : parts) {
            newLampName.append(StringUtils.capitalize(part));
            newLampName.append(" ");
        }

        return newLampName;
    }

    /*public  void sendUDP_switchState(final UDPAsyncTask udpAsyncTask) {

    }*/

}
