package com.micronanobio.elisapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

public class ChartSelect4Wells extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_select4_wells);

        //Gets file name from intent's extras
        String fileName = "";
        Bundle b = getIntent().getExtras();
        if(b!=null){
            fileName = (String) b.get("fileName");
        }

        //Draws line graph
        ImageButton chartButton = (ImageButton) findViewById(R.id.chartBtn);
        String finalFileName = fileName;
        chartButton.setOnClickListener((v) -> {
            Intent chIntent = new Intent(getApplicationContext(), Chart4Wells.class);
            chIntent.putExtra("fileName", finalFileName);
            startActivity(chIntent);
        });

        //Draws heatmap
        ImageButton heatButton = (ImageButton) findViewById(R.id.heatBtn);
        heatButton.setOnClickListener((v) -> {
            Intent hIntent = new Intent(getApplicationContext(), Heatmap4Wells.class);
            hIntent.putExtra("fileName", finalFileName);
            startActivity(hIntent);
        });
    }
}