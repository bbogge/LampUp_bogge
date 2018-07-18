package com.polito.did2017.lampup.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.polito.did2017.lampup.R;
import com.polito.did2017.lampup.utilities.ItemClickSupport;
import com.polito.did2017.lampup.utilities.LampAdapter;
import com.polito.did2017.lampup.utilities.LampManager;
import com.polito.did2017.lampup.utilities.UDPAsyncTask;

public class MainActivity extends AppCompatActivity {

    Context context = this;
    private Button ok;
    private RecyclerView rv;
    private LampManager lampManager = LampManager.getInstance();
    private LampAdapter adapter;

    UDPAsyncTask udpAsyncTask = new UDPAsyncTask(new Runnable() {
        @Override
        public void run() {
            (rv.getAdapter()).notifyDataSetChanged();
        }
    }, context, lampManager);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = findViewById(R.id.lamp_list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        ItemClickSupport.addTo(rv).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {

                Intent detailIntent = new Intent( MainActivity.this, LampDetailActivity.class );
                detailIntent.putExtra( "lamp_ip", lampManager.getLamps().get( position ).getLampIP() );
                detailIntent.putExtra( "lamp_name", lampManager.getLamps().get( position ).getLampName() );
                detailIntent.putExtra( "lamp_photo_ID", lampManager.getLamps().get( position ).getLampImage() );
                startActivity( detailIntent );
            }
        });

        lampManager.discover(udpAsyncTask);

        adapter = new LampAdapter(lampManager.getLamps(), udpAsyncTask, context);
        rv.setAdapter(adapter);

//        ok = findViewById(R.id.button);
//
//        ok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(MainActivity.this, LampDetailActivity.class);
//                i.putExtra("lamp", 0);
//                startActivity(i);
//            }
//        });
    }

    @Override
    protected void onResume() {
        // per avere la lista aggiornata con solo le lampade presenti
        rv.getAdapter().notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        // per avere la lista aggiornata con solo le lampade presenti
        rv.getAdapter().notifyDataSetChanged();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        udpAsyncTask.stopListening();
        super.onDestroy();
    }
}
