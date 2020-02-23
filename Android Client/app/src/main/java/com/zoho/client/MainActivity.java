package com.zoho.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {

    Button b;
    EditText e;

    public class Connect implements Runnable{
            String msg;
            String[] msgs;

            Handler show = new Handler();
            public void run() {
                try {
                    String AVD_IP="10.0.2.2";
                    String MacBook="172.22.114.61";
                    //Get files list from SERVER
                    URL url = new URL("http://"+ AVD_IP +":8080/Sheets/files");
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    Log.d("Log","Connected Main!");
                    msg=new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                    connection.disconnect();

                    msgs=msg.split(",");

                    //Display in List View
                    show.post(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_listview, msgs);

                            ListView listView = (ListView) findViewById(R.id.data);
                            listView.setAdapter(adapter);

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                            {

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view,
                                                        int position, long id) {

                                    String FileName = msgs[position];

                                    Intent intent = new Intent(MainActivity.this, Editor.class);
                                    intent.putExtra("filename", FileName);
                                    startActivity(intent);

                                }
                            });
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }

    Thread thread = new Thread(new Connect());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thread.start();

        //Create New File
        b = (Button) findViewById(R.id.connect);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Handler show = new Handler();
                e=(EditText) findViewById(R.id.createName);
                String FileName=e.getText().toString();
                if(FileName.isEmpty()){
                    show.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Filename Can't be empty!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Intent intent = new Intent(MainActivity.this, Editor.class);
                    intent.putExtra("filename", FileName);
                    startActivity(intent);
                }
            }

        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Thread reload = new Thread(new Connect());
        reload.start();
    }
}

