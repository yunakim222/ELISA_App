package com.micronanobio.elisapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothSocket btSocket = null;

        //Setting up Toggle Switch for confirming 94 or 4 wells
        Switch toggle = (Switch) findViewById(R.id.toggle);

        /*toggle.setOnClickListener((v) -> {
            Boolean wells = true; //true = 96 wells, false = 4 wells
            wells = toggle.isChecked();
        });*/

        //Setting up Receive Data button
        ImageButton receiveButton = (ImageButton) findViewById(R.id.receiveBtn);
        receiveButton.setOnClickListener((v) -> {
            this.handleBluetooth(btSocket, toggle.isChecked());
        });

        //For opening up existing files
        ImageButton fileButton = (ImageButton) findViewById(R.id.fileBtn);
        fileButton.setOnClickListener((v) -> {
            Intent fIntent = new Intent(getApplicationContext(), FileSelect.class);
            startActivity(fIntent);
        });

        //Setting up the secret sample data generator button
        Button secretButton = (Button) findViewById(R.id.secret);
        secretButton.setVisibility(View.VISIBLE);
        secretButton.setBackgroundColor(Color.TRANSPARENT);
        //On click listener for secret button (generates data and takes you to FileSelect
        secretButton.setOnClickListener((v) -> {
            Random random = new Random();
            String data = "";

            //Generating sample random data
            //first 48 data points (=4 rows) look like S curve
            //second 48 data points are purely random between 0-100000
            float sampleData[] = new float[96];
            for(int i=0; i<48;i++) {
                if (i % 12 < 2) {
                    float adding = (float) (90000 + random.nextDouble() * (100000-90000));
                    data += String.valueOf(i+1) + "," + "-1" + "," + String.valueOf(adding) + "," + "-1" + ",";
                } else if (i % 12 == 2) {
                    float adding = (float) (50000 + random.nextDouble() * (90000-50000));
                    data += String.valueOf(i+1) + "," + "-1" + "," + String.valueOf(adding) + "," + "-1" + ",";
                } else if (i % 12 < 4) {
                    float adding = (float) (10000 + random.nextDouble() * (50000-10000));
                    data += String.valueOf(i+1) + "," + "-1" + "," + String.valueOf(adding) + "," + "-1" + ",";
                } else if (i % 12 < 6) {
                    float adding = (float) (5000 + random.nextDouble() * (5000-1000));
                    data += String.valueOf(i+1) + "," + "-1" + "," + String.valueOf(adding) + "," + "-1" + ",";
                } else if (i % 12 < 9) {
                    float adding = (float) (1000 + random.nextDouble() * (1000-100));
                    data += String.valueOf(i+1) + "," + "-1" + "," + String.valueOf(adding) + "," + "-1" + ",";
                } else {
                    float adding = (float) (random.nextDouble() * 100);
                    data += String.valueOf(i+1) + "," + "-1" + "," + String.valueOf(adding) + "," + "-1" + ",";
                }
            }
            for(int i=48; i<96; i++){
                float adding = (float) ( Math.pow(10,random.nextDouble() * 5));
                data += String.valueOf(i+1) + "," + "-1" + "," + String.valueOf(adding) + "," + "-1" + ",";
            }

            data = data.substring(0,data.length()-1);
            File docPath = new File(getApplicationContext().getFilesDir(), "text");
            if (!docPath.exists()) {
                docPath.mkdir();
            }

            String currentDate = new SimpleDateFormat("MM_dd_yy_", Locale.getDefault()).format(new Date());
            String currentTime = new SimpleDateFormat("HH_mm_ss", Locale.getDefault()).format(new Date());
            String title = currentDate + currentTime + ".txt";
            File writing =new File(docPath, title);
            FileWriter writer = null;

            try {
                writer = new FileWriter(writing);
                writer.append(data);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (toggle.isChecked()) { //96-wells
                Intent csIntent = new Intent(getApplicationContext(), ChartSelect.class);
                csIntent.putExtra("fileName", title);
                startActivity(csIntent);
            } else {
                Intent csIntent = new Intent(getApplicationContext(), ChartSelect4Wells.class);
                csIntent.putExtra("fileName", title);
                startActivity(csIntent);
            }

        });
    }

    //REFERENCED: https://www.youtube.com/watch?v=TLXpDY1pItQ&t=373s&ab_channel=BranislavStanojkovic
    public void handleBluetooth(BluetoothSocket btSocket, Boolean toggle){
        final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Object macAddress = btAdapter.getBondedDevices();
        String address = macAddress.toString();
        address = address.replace("[", "");
        address = address.replace("]", "");
        System.out.println(address); //should be 00:14:03:06:32:BE
        BluetoothDevice hc05 = btAdapter.getRemoteDevice(address);
        System.out.println(hc05.getName()); //should be DSD TECH HC-05

        //tries to connect to bluetooth for 10 times before it gives up
        int count = 0;
        do{
            try {
                btSocket = hc05.createRfcommSocketToServiceRecord(mUUID);
            } catch (Exception e) {
                try{
                    btSocket.close();
                } catch (IOException e1){
                    e1.printStackTrace();
                }
            }
            try{
                btSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;
        } while(!btSocket.isConnected()||(count > 10));

        //calls the helper method that does actual work
        this.communicate(btSocket, toggle);
    }

    //Receives data from bluetooth, saves it into txt file, and opens ChartSelect activity
    public void communicate(BluetoothSocket btSocket, Boolean toggle) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        //for some reason, it only works on second try, so I'm running it twice
        for(int i=0; i<2;i++) {
            try {
                outputStream = btSocket.getOutputStream();
                inputStream = btSocket.getInputStream();
                String out = "";
                outputStream.write(1);
                boolean reading = true;
                boolean wr = false;
                while (reading) {
                    byte b = (byte) inputStream.read();
                    char c = (char) b;
                    if (c == '[') wr = true;
                    if (wr) out += c + "";
                    if (c == ']') break;
                }

                System.out.println("output is: " + out);
                out = out.substring(1, out.length() - 2); //gets rid of initial [ and ,] at the end

                //manipulation due to different well number references in arduino and app code
                /*In Arduino |     In our app
                * 2  3       |     1   2
                * 1  4       |     3   4
                * */
                if (!toggle) {
                    String[] values = out.split(",");
                    out = "";
                    for (int ind = 1; ind <=4; ind++){
                        out += ind;
                        out += ",";
                        if (ind == 1) {
                            out += values[5];
                            out += ",";
                            out += values[6];
                            out += ",";
                            out += values[7];
                            out += ",";
                        } else if (ind == 2) {
                            out += values[9];
                            out += ",";
                            out += values[10];
                            out += ",";
                            out += values[11];
                            out += ",";
                        } else if (ind == 3) {
                            out += values[1];
                            out += ",";
                            out += values[2];
                            out += ",";
                            out += values[3];
                            out += ",";
                        } else {
                            out += values[13];
                            out += ",";
                            out += values[14];
                            out += ",";
                            out += values[15];
                        }
                    }
                    System.out.println("output after 4 well processing is: " + out);
                }

                //Saving into txt file
                File docPath = new File(getApplicationContext().getFilesDir(), "text");
                if (!docPath.exists()) {
                    docPath.mkdir();
                }

                String currentDate = new SimpleDateFormat("MM_dd_yy_", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH_mm_ss", Locale.getDefault()).format(new Date());
                String title = currentDate + currentTime + ".txt";
                File writing =new File(docPath, title);
                FileWriter writer = new FileWriter(writing);
                writer.append(out);
                writer.flush();
                writer.close();
                btSocket.close();

                //Opening ChartSelect activity
                if (toggle) { //96-wells
                    Intent csIntent = new Intent(getApplicationContext(), ChartSelect.class);
                    csIntent.putExtra("fileName", title);
                    startActivity(csIntent);
                } else {
                    Intent csIntent = new Intent(getApplicationContext(), ChartSelect4Wells.class);
                    csIntent.putExtra("fileName", title);
                    startActivity(csIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}