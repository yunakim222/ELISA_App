package com.micronanobio.elisapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileWriter;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileSelect extends AppCompatActivity {

    MyRecyclerViewAdapter adapter;
    String[] title = new String[1]; //this is the current working file name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);

        //Adds all file names into arraylist
        File docPath = new File(getApplicationContext().getFilesDir(), "text");
        ArrayList<String> fileNames = new ArrayList<>();
        File[] directoryListing = docPath.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                String fname = child.getName();
                fileNames.add(fname);
            }
        }

        ImageButton lineBtn = (ImageButton) findViewById(R.id.lineBtn);
        ImageButton shareBtn = (ImageButton) findViewById(R.id.shareBtn);
        ImageButton deleteBtn = (ImageButton) findViewById(R.id.deleteBtn);

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Collections.sort(fileNames);
        adapter = new MyRecyclerViewAdapter(this, fileNames);
        adapter.setClickListener(this::onItemClick);
        recyclerView.setAdapter(adapter);

        //Draws line graph with selected data file
        lineBtn.setOnClickListener((v) -> {
            Intent csIntent = new Intent(getApplicationContext(), ChartSelect.class);
            csIntent.putExtra("fileName", title[0]);
            startActivity(csIntent);
            Collections.sort(fileNames);
            MyRecyclerViewAdapter adapter2 = new MyRecyclerViewAdapter(this, fileNames);
            adapter2.setClickListener(this::onItemClick);
            recyclerView.setAdapter(adapter2);
        });

        //Shares selected file
        //Note: Sharing it to Google Drive worked best
        shareBtn.setOnClickListener((v) -> {
            File finalDatafile = new File(docPath, title[0]);
            if(finalDatafile.exists()) {
                Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                intentShareFile.setType(URLConnection.guessContentTypeFromName(finalDatafile.getName()));
                Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.micronanobio.provider.elisapp", finalDatafile);
                intentShareFile.putExtra(Intent.EXTRA_STREAM, uri);
                intentShareFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent chooser = Intent.createChooser(intentShareFile, "Share File");

                List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivity(chooser);
                Collections.sort(fileNames);
                MyRecyclerViewAdapter adapter2 = new MyRecyclerViewAdapter(this, fileNames);
                adapter2.setClickListener(this::onItemClick);
                recyclerView.setAdapter(adapter2);
            }
        });

        //Delete selected data file
        deleteBtn.setOnClickListener((v) -> {
            File finalDatafile = new File(docPath, title[0]);
            if(finalDatafile.exists()) {
                finalDatafile.delete();
                fileNames.remove(title[0]);
            }
            Collections.sort(fileNames);
            MyRecyclerViewAdapter adapter2 = new MyRecyclerViewAdapter(this, fileNames);
            adapter2.setClickListener(this::onItemClick);
            recyclerView.setAdapter(adapter2);
        });
    }

    //On click listener for each file (recycler row) - changes current working file name
    public void onItemClick(View view, int position) {
        title[0]=adapter.getItem(position);
    }

}