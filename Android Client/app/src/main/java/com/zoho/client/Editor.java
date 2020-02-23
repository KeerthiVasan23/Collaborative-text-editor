package com.zoho.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;


public class Editor extends AppCompatActivity {

    EditText editText;
    
    //FUD (Frequently Used Devices)
    String AVD = "10.0.2.2";
    String MacBook = "172.22.114.61";
    String Pixel = "127.0.0.1";
    String Note5 = "172.22.101.44";
    String Nexus6="172.22.140.197";
    
    
    String Server_IP = AVD;
    String Client_IP = Pixel;
    String urlAddr = "http://" + Server_IP + ":8080/Sheets/files";
    volatile boolean flag = true;
    volatile boolean lastSave;
    String FileName;

    //Send data to server on text change
    class Write extends Thread implements Runnable {
        String text;
        int startIdx;
        int endIdx;

        public Write(String text, int startIdx, int endIdx) {
            this.text = text;
            this.startIdx = startIdx;
            this.endIdx = endIdx;
        }

        public void run() {
            try {
                Log.d("Log", "SaveD!");

                URL urlPost = new URL(urlAddr);
                HttpURLConnection conn = (HttpURLConnection) urlPost.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String POST_PARAMS = "filename=" + FileName + "&edited=" + editText.getText().toString() + "&text=" + text + "&start=" + startIdx + "&end=" + endIdx + "&ip=" + Client_IP;
                OutputStream os = conn.getOutputStream();
                os.write(POST_PARAMS.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("Log", "Code: " + responseCode);
                conn.disconnect();
            } catch (Exception e) {
                Log.d("Error", "Exception: " + e);
            }
        }
    }


    //Listen for text change
    TextViewListener watch = new TextViewListener() {

        @Override
        protected void onTextChanged(String aNew, int start, int end) {
            Write write = new Write(aNew, start, end);
            write.start();
        }
    };

    public abstract class TextViewListener implements TextWatcher {
        private String _before;
        private String _old;
        private String _new;
        private String _after;
        private int _start;
        private int _end;
        private boolean _ignore = false;

        @Override
        public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {
            _before = sequence.subSequence(0, start).toString();
            _old = sequence.subSequence(start, start + count).toString();
            _after = sequence.subSequence(start + count, sequence.length()).toString();
            _start = start;
            _end = start + count;
        }

        @Override
        public void onTextChanged(CharSequence sequence, int start, int before, int count) {
            _new = sequence.subSequence(start, start + count).toString();
        }

        @Override
        public void afterTextChanged(Editable sequence) {
            if (_ignore)
                return;

            onTextChanged(_new, _start, _end);
        }

        protected abstract void onTextChanged(String aNew, int start, int end);

        protected void startUpdates() {
            _ignore = true;
        }

        protected void endUpdates() {
            _ignore = false;
        }
    }

    //Receive text when changes are made in other clients
    public class reLoad implements Runnable {
        @Override
        public void run() {
            try {
                while (flag) {
                    Socket client = new Socket(Server_IP, 4444);
                    Log.d("Log", "Socket Connected!");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    StringBuilder lines = new StringBuilder();
                    String newLine = "";
                    String line;
                    Log.d("Log", "Socket Read!");
                    String rcvFile = reader.readLine();
                    final int start = Integer.parseInt(reader.readLine());
                    final int end = Integer.parseInt(reader.readLine());

                    while ((line = reader.readLine()) != null) {
                        Log.d("Log", "inLoop: " + line);
                        lines.append(newLine);
                        newLine = "\n";
                        lines.append(line);
                    }
                    final StringBuilder text = lines;
                    Log.d("Log", "Socket Success!");


                    if (FileName.equals(rcvFile)) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                int currLen=editText.getText().length();
                                int currPos=editText.getSelectionStart();
                                watch.startUpdates();
                                editText.setText(text);
                                watch.endUpdates();

                                Log.d("Log","end: "+end+" currPos: "+currPos);
                                if(end<=currPos)
                                    editText.setSelection(currPos+text.length()-currLen);
                                else
                                    editText.setSelection(currPos);

                            }
                        });
                    }

                    Log.d("Log", "Socket Update!");
                }

            } catch (Exception e) {
                Log.d("Error", "Socket Exception----------> " + e);
            }

        }
    }


    //Fetch data on file open
    class Read extends Thread implements Runnable {
        @Override
        public void run() {

            try {

                URL url = new URL(urlAddr + "?fileName=" + URLEncoder.encode(FileName, "UTF-8"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder lines = new StringBuilder();
                String newLine = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.append(newLine);
                    newLine = "\n";
                    lines.append(line);
                }
                final StringBuilder text = lines;

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        watch.startUpdates();
                        editText.setText(text);
                        watch.endUpdates();
                        editText.setSelection(editText.getText().length());
                    }
                });

                connection.disconnect();
                Log.d("Log", "UpdateD!!!");

            } catch (Exception e) {
                Log.d("Error", "Exception----------> " + e);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        editText = (EditText) findViewById(R.id.editText);
        FileName = getIntent().getStringExtra("filename");

        Read get = new Read();
        get.start();

        Thread reload = new Thread(new reLoad());
        reload.start();

        editText.addTextChangedListener(watch);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

