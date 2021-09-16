package com.micronanobio.elisapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Chart4Wells extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart4_wells);

        //Gets file name from intent's extra
        String fileName = "";
        Bundle b = getIntent().getExtras();
        if(b!=null){
            fileName = (String) b.get("fileName");
        }

        String dateSubstring = fileName.substring(0,2) + "/" + fileName.substring(3,5);
        dateSubstring = dateSubstring + "/" + fileName.substring(6,8);
        String timeSubstring = fileName.substring(9,11) + ":" + fileName.substring(12,14);
        timeSubstring = timeSubstring + ":" + fileName.substring(15,17);
        String actualText = dateSubstring + "\t" + timeSubstring;
        EditText title = (EditText) findViewById(R.id.file_title);
        title.setText(actualText);
        title.setEnabled(false);

        //Takes to Main Activity to start over
        ImageButton homeButton = (ImageButton) findViewById(R.id.homeBtn);
        homeButton.setOnClickListener((v) -> {
            Intent hIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(hIntent);
        });

        //Takes to file selection activity to choose different file
        ImageButton fileButton = (ImageButton) findViewById(R.id.fileBtn);
        fileButton.setOnClickListener((v) -> {
            Intent fIntent = new Intent(getApplicationContext(), FileSelect.class);
            startActivity(fIntent);
        });

        //Draws heatmap with same data
        ImageButton heatButton = (ImageButton) findViewById(R.id.heatBtn);
        String finalFileName = fileName;
        heatButton.setOnClickListener((v) -> {
            Intent fIntent = new Intent(getApplicationContext(), Heatmap4Wells.class);
            fIntent.putExtra("fileName", finalFileName);
            startActivity(fIntent);
        });

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
        float max = 0;//keeps track of max value to use when drawing graph (setting up Y axis)
        for (String a : arrOfStr) {
            if (ind % 4 == 2) {//can be ind % 4 == 1,2,3 depending on the variable you want to graph
                Float adding = Float.parseFloat(a);
                sampleData[(ind-2)/4] = adding;
                if (adding > max) {
                    max = adding;
                }
            }
            ind++;
        }

        //Starts drawing line graph. Each row is a arraylist
        //KEEP IN MIND THAT CODE BELOW THIS LINE IS NOT THE CLEANEST/PRETTIEST CODE
        LineChart line = findViewById(R.id.line);
        ArrayList<Entry> row1 = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            row1.add(new Entry(i + 1, sampleData[i]));
        }


        LineDataSet r1 = new LineDataSet(row1, "A");
        r1.setDrawCircles(true);
        r1.setDrawCircleHole(false);
        r1.setDrawValues(false);
        r1.setCircleRadius(5);
        r1.setLineWidth(10);
        r1.setCircleColor(Color.parseColor("#ef6f6c"));
        r1.setColor(Color.parseColor("#ef6f6c")); //red

        ArrayList<ILineDataSet> rows = new ArrayList<>();
        rows.add(r1);

        LineData lineData = new LineData(rows);

        line.setData(lineData);
        line.getXAxis().setAxisMinimum(1);
        line.getXAxis().setAxisMaximum(4.1f); //that's because the graph renders choppy with 4
        line.getXAxis().setLabelCount(3);
        line.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        line.getAxisLeft().setAxisMinimum(0);
        line.getAxisLeft().setAxisMaximum(max+2000);//same reason as above
        line.getAxisLeft().setLabelCount(10);
        line.getAxisRight().setEnabled(false);
        line.getDescription().setText("");
        line.getLegend().setEnabled(false);
        line.animateX(1000);
    }
}
