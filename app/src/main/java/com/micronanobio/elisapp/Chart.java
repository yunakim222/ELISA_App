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


public class Chart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

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
            Intent fIntent = new Intent(getApplicationContext(), Heatmap.class);
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
        ArrayList<Entry> row2 = new ArrayList<>();
        ArrayList<Entry> row3 = new ArrayList<>();
        ArrayList<Entry> row4 = new ArrayList<>();
        ArrayList<Entry> row5 = new ArrayList<>();
        ArrayList<Entry> row6 = new ArrayList<>();
        ArrayList<Entry> row7 = new ArrayList<>();
        ArrayList<Entry> row8 = new ArrayList<>();

        for(int i=0; i<12;i++) {
            row1.add(new Entry(i+1, sampleData[i]));
        }
        for(int i=0; i<12;i++) {
            row2.add(new Entry(i+1, sampleData[i+12]));
        }
        for(int i=0; i<12;i++) {
            row3.add(new Entry(i+1, sampleData[i+24]));
        }
        for(int i=0; i<12;i++) {
            row4.add(new Entry(i+1, sampleData[i+36]));
        }
        for(int i=0; i<12;i++) {
            row5.add(new Entry(i+1, sampleData[i+48]));
        }
        for(int i=0; i<12;i++) {
            row6.add(new Entry(i+1, sampleData[i+60]));
        }
        for(int i=0; i<12;i++) {
            row7.add(new Entry(i+1, sampleData[i+72]));
        }
        for(int i=0; i<12;i++) {
            row8.add(new Entry(i+1, sampleData[i+84]));
            System.out.println(sampleData[i+84]);
        }

        LineDataSet r1 = new LineDataSet(row1, "A");
        LineDataSet r2 = new LineDataSet(row2, "B");
        LineDataSet r3 = new LineDataSet(row3, "C");
        LineDataSet r4 = new LineDataSet(row4, "D");
        LineDataSet r5 = new LineDataSet(row5, "E");
        LineDataSet r6 = new LineDataSet(row6, "F");
        LineDataSet r7 = new LineDataSet(row7, "G");
        LineDataSet r8 = new LineDataSet(row8, "H");

        r1.setColor(Color.parseColor("#ef6f6c")); //red
        r2.setColor(Color.parseColor("#f2c57c")); //orange
        r3.setColor(Color.parseColor("#ffd571")); //yellow
        r4.setColor(Color.parseColor("#7fb685")); //green
        r5.setColor(Color.parseColor("#82b3cf")); //light blue
        r6.setColor(Color.parseColor("#2c7199")); //navy
        r7.setColor(Color.parseColor("#756378")); //purple
        r8.setColor(Color.parseColor("#334438")); //black

        ArrayList<ILineDataSet> rows = new ArrayList<>();
        this.rowHelper(r1, rows);
        this.rowHelper(r2, rows);
        this.rowHelper(r3, rows);
        this.rowHelper(r4, rows);
        this.rowHelper(r5, rows);
        this.rowHelper(r6, rows);
        this.rowHelper(r7, rows);
        this.rowHelper(r8, rows);
        LineData lineData = new LineData(rows);

        line.setData(lineData);
        line.getXAxis().setAxisMinimum(1);
        line.getXAxis().setAxisMaximum(12);
        line.getXAxis().setLabelCount(11);
        line.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        line.getAxisLeft().setAxisMinimum(0);
        line.getAxisLeft().setAxisMaximum(max);
        line.getAxisLeft().setLabelCount(10);
        line.getAxisRight().setEnabled(false);
        line.getDescription().setText("");
        line.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        line.animateX(2000);
    }
    public void rowHelper(LineDataSet r, ArrayList<ILineDataSet> rows){
        r.setDrawValues(false);
        r.setDrawCircles(false);
        rows.add(r);
    }
}