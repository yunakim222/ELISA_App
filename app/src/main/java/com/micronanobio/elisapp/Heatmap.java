package com.micronanobio.elisapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.util.Scanner;

public class Heatmap extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap);

        //Gets file name from intent's extra
        String fileName = "";
        Bundle b = getIntent().getExtras();
        if(b!=null){
            fileName = (String) b.get("fileName");
        }

        //Takes to Main Activity to start over
        ImageButton homeButton = (ImageButton) findViewById(R.id.homeBtn);
        homeButton.setOnClickListener((v) -> {
            Intent fIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(fIntent);
        });

        //Takes to file selection activity to choose different file
        ImageButton fileButton = (ImageButton) findViewById(R.id.fileBtn);
        fileButton.setOnClickListener((v) -> {
            Intent fIntent = new Intent(getApplicationContext(), FileSelect.class);
            startActivity(fIntent);
        });

        //Draws line graph with same data
        ImageButton lineButton = (ImageButton) findViewById(R.id.lineBtn);
        String finalFileName = fileName;
        lineButton.setOnClickListener((v) -> {
            Intent fIntent = new Intent(getApplicationContext(), Chart.class);
            fIntent.putExtra("fileName", finalFileName);
            startActivity(fIntent);
        });

        String dateSubstring = fileName.substring(0,2) + "/" + fileName.substring(3,5);
        dateSubstring = dateSubstring + "/" + fileName.substring(6,8);
        String timeSubstring = fileName.substring(9,11) + ":" + fileName.substring(12,14);
        timeSubstring = timeSubstring + ":" + fileName.substring(15,17);
        String actualText = dateSubstring + "\t" + timeSubstring;
        EditText title = (EditText) findViewById(R.id.file_title);
        title.setText(actualText);
        title.setEnabled(false);

        //Opens and reads the txt file
        File docPath = new File(getApplicationContext().getFilesDir(), "text");
        File file = new File(docPath, fileName);
        String response ="";
        try {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                response += scanner.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] arrOfStr = response.split(",");

        //Converts string arrays into float
        float sampleData[] = new float[96];
        int ind = 0;
        for (String a : arrOfStr) {
            if (ind % 4 == 2) {//can be ind % 4 == 1,2,3 depending on the variable you want to graph
                Float adding = Float.parseFloat(a);
                sampleData[(ind-2)/4] = adding;
            }
            ind++;
        }

        //Heatmap is consisted of 96 buttons in table layout. For loop goes over one by one to set it up
        Button[] buttons = new Button[96];
        for(int i=1; i<97; i++) {
            String buttonID = "h" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i-1] = ((Button) findViewById(resID));
            setHeatmapColor(buttons[i-1], sampleData[i-1]);
        }
    }

    //Sets the color for each cell of heatmap depending on the value
    public void setHeatmapColor(Button btn, float data) {
        //RANGES ARE ARBITRARY
        if(data > 100000) {
            btn.setBackgroundColor(Color.parseColor("#ec4f43"));
        } else if (data > 10000){
            btn.setBackgroundColor(Color.parseColor("#fe7968"));
        } else if (data > 5000){
            btn.setBackgroundColor(Color.parseColor("#fe948d"));
        } else if (data > 1000){
            btn.setBackgroundColor(Color.parseColor("#ffbdb3"));
        } else{
            btn.setBackgroundColor(Color.parseColor("#ffe0db"));
        }
    }
}