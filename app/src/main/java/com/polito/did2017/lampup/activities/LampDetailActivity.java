package com.polito.did2017.lampup.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.support.v7.widget.Toolbar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.polito.did2017.lampup.fragments.GyroLampFragment;
import com.polito.did2017.lampup.R;
import com.polito.did2017.lampup.utilities.ColorGridAdapter;
import com.polito.did2017.lampup.utilities.ConnectTask;
import com.polito.did2017.lampup.utilities.Lamp;
import com.polito.did2017.lampup.utilities.LampManager;
import com.polito.did2017.lampup.utilities.TCPClient;
import com.truizlop.fabreveallayout.FABRevealLayout;
import com.truizlop.fabreveallayout.OnRevealChangeListener;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

import static java.lang.Thread.sleep;

public class LampDetailActivity extends AppCompatActivity implements GyroLampFragment.OnGyroLampFragmentInteractionListener {

    private Context context = this;
    private final static String COLORS_PREFS = "MyColors";
    private final static String ANGLE_PREF = "LastAngle";
    private final static String SWITCH_PREF = "LastSwitchState";
    private final static String LUM_PREF = "LastLum";
    private final static String LAST_COLOR = "LastColor";
    private final static int MIN_LUM = 5;
    private FABRevealLayout fabRevealLayout;
    private static GridView color_grid;
    private static ColorGridAdapter cga;
    private LinearLayout color_picker;
    private LinearLayout color;
    private SeekBar hue, saturation, brightness;
    private Switch switchOnOff;
    private TextView textLampName;
    private Fragment fragment;
    private float[] hsv = new float[3];
    public static float HSVtoRGBConvertFactor = 255.0f/360.0f;
    private static ArrayList<Integer> colors = new ArrayList<>();
    private int lastSize = 0;
    private static int lastSelectedColor;

    private LampManager lampManager = LampManager.getInstance();
    private String lampIP = "";
    private Lamp selectedLamp;
    private TCPClient tcpClient;
    private ConnectTask connectTask;

    //default commands
    private final String turnOn = "turnOn";
    private final String turnOff = "turnOff";
    private final String switchState = "switchState";
    private final String setLum = "setLum";
    private final String setMainServo = "setMainServo";
    private final String setSecondaryServo = "setSecondaryServo";
    private final String setHueSat = "setHueSat";

    //private final int MIN_LUM = 5;
    //private final int MAX_LUM = 255;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_lamp_detail );

        textLampName = findViewById( R.id.textLampName );
        switchOnOff = findViewById( R.id.switchOnOff );
        color_picker = findViewById( R.id.sliders );
        color = findViewById( R.id.secondary_view );
        hue = findViewById( R.id.hue_slider );
        saturation = findViewById( R.id.saturation_slider );
        brightness = findViewById( R.id.brightness_slider );

        final Intent i = getIntent();

        if (i.hasExtra( "lamp_ip" )) {
            lampIP = i.getStringExtra( "lamp_ip" );
        } else {
            System.out.println( "no lamp_ip" );
        }

        //trova la lampada che si vuole utilizzare
        for (Lamp lamp : lampManager.getLamps()) {
            if (lamp.getLampIP().equals( lampIP )) {
                selectedLamp = lamp;
                break;
            }
        }

        initLamp( selectedLamp );

        // CONNECTION TCP
        tcpClient = new TCPClient( new TCPClient.OnMessageReceived() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceived(final String message) {
                //this method calls the onProgressUpdate
                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler( getApplicationContext().getMainLooper() );
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {

                        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                        String[] cmd_rcv = message.split( Pattern.quote( "$" ) ); // "updateState$State[0|1]$Lum[0-255])$Hue[0-255]$Saturation[0-255]);
                        Log.e( "message string", message );

                        if (cmd_rcv[0].equals( "updateState" )) {
                            switch (cmd_rcv[1]) {
                                case "0": // la lamp è stata spenta manualmente dal sensore
                                    Log.e( "CMD_RCVD", "CASE 0, lamp spenta" + cmd_rcv[1] );
                                    selectedLamp.setBrightness( MIN_LUM );
                                    selectedLamp.turnOff(); // setta lo stato della lampada a spento
                                    switchOnOff.setChecked( selectedLamp.isOn() ); // setta a false lo switch
                                    break;
                                case "1": // è cambiata la luminosità o è stata accesa la lamp (da sensore)
                                    Log.e( "CMD_RCVD", "CASE 1, lamp accesa" + cmd_rcv[1] );
                                    int rcvd_lum = Integer.parseInt( cmd_rcv[2] );
                                    selectedLamp.setBrightness( rcvd_lum );
                                    brightness.setProgress( selectedLamp.getBrightness() );

                                    if (!selectedLamp.isOn()) { // se era spenta

                                        // update di luminosità, hue e saturation
                                        selectedLamp.turnOn(); // setta lo stato della lampada a true
                                        switchOnOff.setChecked( selectedLamp.isOn() ); // setta a true lo switch
                                    }

                                    // potrebbe non essere necessario
                                    //selectedLamp.setHueSat( Integer.parseInt( cmd_rcv[3] ), Integer.parseInt( cmd_rcv[4] ) );
                                    break;

                                default:
                                    Log.e( "CMD_RCVD", "Il secondo parametro del pacchetto deve essere lo stato ON(1)/OFF(0) della lampada" );
                                    break;
                            }
                        } else if (cmd_rcv[0].equals( "" )) {
                            // tutto regolare
                            //Log.e( "CMD_RCVD", "Tutto regolare" );
                        } else {
                            Log.e( "CMD_RCVD", "Comando ricevuto da arduino SCONOSCIUTO!" );
                        }
                    }
                };
                mainHandler.post( myRunnable );
            }
        }, selectedLamp.getLampIP() );

        // CONNECT TASK, non bloccante
        connectTask = new ConnectTask( getApplicationContext(), this );
        connectTask.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR, tcpClient );

        // SWITCH ON-OFF
        //switchOnOff.setChecked( selectedLamp.isOn() );
        switchOnOff.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                selectedLamp.setState( switchOnOff.isChecked() );
                if (switchOnOff.isChecked()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        switchOnOff.setTrackTintMode( PorterDuff.Mode.SCREEN );
                    }

                    selectedLamp.turnOn();
                    brightness.getProgressDrawable().setAlpha(255);
                    brightness.getThumb().setAlpha(255);
                    brightness.setEnabled( true );
                    brightness.setProgress( selectedLamp.getBrightness() );

                    tcpClient.setMessage( turnOn );
                    try {
                        sleep( 200 );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tcpClient.setMessage( setLum + "$" + selectedLamp.getBrightness() );
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        switchOnOff.setTrackTintMode( PorterDuff.Mode.MULTIPLY );
                    }

                    selectedLamp.turnOff();
                    tcpClient.setMessage( turnOff );
                    brightness.setEnabled( false );
                    brightness.getProgressDrawable().setAlpha(100);
                    brightness.getThumb().setAlpha(100);
                }
            }
        } );

        if (getColors( COLORS_PREFS ) != null) {
            colors = getColors( COLORS_PREFS );
            lastSize = colors.size();
        }

        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        //int fragId = i.getIntExtra("lamp", 0);
        String fragId = i.getStringExtra( "lamp_name" );

        checkLampId( fragId );

        if (fragId.equals( "gyro_lamp" )) {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace( R.id.fragment_container, fragment, "gyrolamp" );
            fragmentTransaction.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN );
            fragmentTransaction.commit();

            ((GyroLampFragment) fragment).setAngle( PreferenceManager.getDefaultSharedPreferences( context ).getInt( ANGLE_PREF, 80 ) );

            fabRevealLayout = findViewById( R.id.fab_reveal_layout );
            configureFABReveal( fabRevealLayout );
            color_grid = findViewById( R.id.color_grid );
            color_grid.addOnLayoutChangeListener( new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                    animateColorElement();
                }
            } );
            cga = new ColorGridAdapter( context, colors, tcpClient, selectedLamp, switchOnOff, brightness );
            color_grid.setAdapter( cga );

            brightness.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //setta la luminosità ad un minimo sindacale

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    Log.d( "Brightness", "Seekbar luminosità cambiata" );
                    if (seekBar.getProgress() == 0)
                        seekBar.setProgress( MIN_LUM );
                    selectedLamp.setBrightness( seekBar.getProgress() );

                    if (selectedLamp.isOn())
                        tcpClient.setMessage( setLum + "$" + selectedLamp.getBrightness() );
                }
            } );
            resetColor( getResources().getColor( R.color.colorAccent ) );
            initHS();
            ImageButton cancel = color_picker.findViewById( R.id.btn_cancel );
            cancel.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fabRevealLayout.revealMainView();
                }
            } );
            ImageButton confirm = color_picker.findViewById( R.id.btn_done );
            confirm.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    colors.add( Color.HSVToColor( hsv ) );
                    cga.notifyDataSetChanged();
                    fabRevealLayout.revealMainView();
                    saveColors( colors, COLORS_PREFS );
                    float Hue = hsv[0] * HSVtoRGBConvertFactor;
                    float Saturation = hsv[1] * 255.0f;
                    // aggiorna i dati di lampada e aggiorna la vista
                    selectedLamp.turnOn();
                    selectedLamp.setHueSat( Hue, Saturation );
                    switchOnOff.setChecked( selectedLamp.isOn() );
                    tcpClient.setMessage( setHueSat + "$" + Hue + "$" + Saturation );
                }
            } );
        }
    }

    private void initLamp(Lamp selectedLamp) {
        switchOnOff.setChecked( selectedLamp.isOn() );
        //selectedLamp.setState(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SWITCH_PREF, false));
        selectedLamp.setBrightness(PreferenceManager.getDefaultSharedPreferences(context).getInt(LUM_PREF, 128));

        textLampName.setText(lampManager.convertName(selectedLamp.getLampName()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && selectedLamp.isOn()) {
            switchOnOff.setTrackTintMode( PorterDuff.Mode.SCREEN );
        }

        // Si può modificare la brigthness solo se lo switch è attivo
        brightness.setEnabled( selectedLamp.isOn() );
        if(selectedLamp.getBrightness() == 0) {
            selectedLamp.setBrightness( MIN_LUM );
        }
        brightness.setProgress( selectedLamp.getBrightness() );
        if(selectedLamp.isOn()) {
            brightness.getProgressDrawable().setAlpha( 255 );
            brightness.getThumb().setAlpha( 255 );
        } else {
            brightness.getProgressDrawable().setAlpha(100);
            brightness.getThumb().setAlpha(100);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveSwitchState();
        saveLum();
        saveLastColor();

        if (tcpClient != null) {
            tcpClient.stopClient();
            tcpClient = null;
        }
        if(connectTask != null && tcpClient == null){
            connectTask.cancel(true);
            connectTask = null;
        }


        saveColors( colors, COLORS_PREFS );
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        saveSwitchState();
        saveLum();
        saveLastColor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSwitchState();
        saveLum();
        saveLastColor();
    }

    private void checkLampId(String fragId) {
        switch (fragId) {
            case "gyro_lamp":
                fragment = new GyroLampFragment();
                break;
            default:
                fragment = new Fragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initHS() {

        hue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                hsv[0] = progress;
                color.setBackgroundColor(Color.HSVToColor(hsv));
                GradientDrawable sat = (GradientDrawable) getResources().getDrawable(R.drawable.shape_saturation_gradient);
                sat.setColors(new int[]{getResources().getColor(R.color.white), Color.HSVToColor(new float[]{hsv[0], 1, 1})});
                color_picker.findViewById(R.id.saturation_gradient).setBackground(sat);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        saturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                hsv[1] = (float) progress/100;
                color.setBackgroundColor(Color.HSVToColor(hsv));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void configureFABReveal(FABRevealLayout fabRevealLayout) {
        fabRevealLayout.setOnRevealChangeListener(new OnRevealChangeListener() {
            @Override
            public void onMainViewAppeared(FABRevealLayout fabRevealLayout, View mainView) {
                resetColor(getResources().getColor(R.color.colorAccent));
                brightness.setEnabled(true);
                brightness.animate().alpha(1).setDuration(1000).setInterpolator(new OvershootInterpolator()).start();
                color_grid.animate().alpha(1).setDuration(1000).setInterpolator(new OvershootInterpolator()).start();
            }

            @Override
            public void onSecondaryViewAppeared(final FABRevealLayout fabRevealLayout, View secondaryView) {
                animateSlidersView(color_picker);
                brightness.animate().alpha(0).setDuration(1000).setInterpolator(new OvershootInterpolator()).start();
                brightness.setEnabled(false);
                color_grid.animate().alpha(0).setDuration(1000).setInterpolator(new OvershootInterpolator()).start();
            }
        });
    }

    private void animateColorElement() {
        if (colors.size() > lastSize) {
            scale(color_grid.getChildAt(color_grid.getLastVisiblePosition()), 0);
            lastSize = colors.size();
        }
    }

    private void resetColor(int color) {
        Color.colorToHSV(color, hsv);
        hue.setProgress((int) hsv[0]);
        float tmp = hsv[1]*100;
        saturation.setProgress((int) tmp);
    }

    private void animateSlidersView(LinearLayout c) {
        c.setTranslationY(700);
        c.animate()
                .translationY(0)
                .setDuration(500)
                .setStartDelay(100)
                .setInterpolator(new OvershootInterpolator())
                .start();
        scale(c.findViewById(R.id.hue_gradient), 100);
        scale(c.findViewById(R.id.hue_slider), 100);
        scale(c.findViewById(R.id.saturation_gradient), 150);
        scale(c.findViewById(R.id.saturation_slider), 150);
        scale(c.findViewById(R.id.btn_cancel), 100);
        scale(c.findViewById(R.id.btn_done), 150);
    }

    private void scale(View view, long delay) {
        view.setScaleX(0);
        view.setScaleY(0);
        view.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(500)
                .setStartDelay(delay+600)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void sendAngleMessage(int progress) {
        if(tcpClient != null) {
            tcpClient.setMessage( setMainServo + "$" + progress );
            saveAngle( progress );
        }
    }

    @Override
    public void sendDiscoMessage(boolean checked) {
        if(checked) {
            if(tcpClient != null) {
                tcpClient.setMessage( setSecondaryServo + "$1" );
            }
        }
        else {
            if(tcpClient != null) {
                tcpClient.setMessage( setSecondaryServo + "$0" );
            }
        }
    }

    public void saveColors(ArrayList<Integer> colors, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(colors);
        editor.putString(key, json);
        editor.apply();
    }

    public ArrayList<Integer> getColors(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Integer>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveAngle(int angle) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ANGLE_PREF, angle);
        editor.commit();
    }

    public void saveSwitchState() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SWITCH_PREF, selectedLamp.isOn());
        editor.commit();
    }

    public void saveLum() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(LUM_PREF, selectedLamp.getBrightness());
        editor.commit();
    }

    public void saveLastColor() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(LAST_COLOR, lastSelectedColor);
        editor.commit();
    }

    public static void colorSelection(int i) {
        color_grid.getChildAt(lastSelectedColor).setSelected(false);
        color_grid.getChildAt(i).setSelected(true);
        lastSelectedColor = i;
    }

    public static void colorRemoval(final int i, Context context) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
//        final View dialogView = inflater.inflate(R.layout.isbn_istruction_fragment, null);
//        dialogBuilder.setView(dialogView);

        dialogBuilder.setMessage( "Vuoi eliminare questo colore?" );
        dialogBuilder.setCancelable( true );

        dialogBuilder.setPositiveButton("Sì", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                colors.remove( i );
                cga.notifyDataSetChanged();
            }
        });

        dialogBuilder.setNegativeButton( "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        } );

        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}
